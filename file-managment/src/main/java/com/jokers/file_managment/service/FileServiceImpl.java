package com.jokers.file_managment.service;

import com.jokers.file_managment.config.FileStorageProperties;
import com.jokers.file_managment.dto.FileEntityDto;
import com.jokers.file_managment.exception.FileNotFoundException;
import com.jokers.file_managment.exception.FileStorageException;
import com.jokers.file_managment.mapper.FileEntityMapper;
import com.jokers.file_managment.model.FileEntity;
import com.jokers.file_managment.repository.FileRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
@Slf4j
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final Path fileStorageLocation;

    public FileServiceImpl(FileRepository fileRepository, FileStorageProperties fileStorageLocation) {
        this.fileRepository = fileRepository;
        this.fileStorageLocation = Paths.get(fileStorageLocation.uploadDir()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("CRITICAL: Could not create upload directory at {}", this.fileStorageLocation);
            throw new FileStorageException("Could not create upload directory", ex);
        }
    }


    @Override
    public FileEntityDto saveFile(MultipartFile file, String description) throws IOException{
        //log.info("Saving file {}", description);


        String hash = getFileChecksum(file);

        // --- Enrichissement MDC pour ELK ---
        MDC.put("file_original_name", file.getOriginalFilename());
        MDC.put("file_size", String.valueOf(file.getSize()));
        MDC.put("file_type", file.getContentType());
        MDC.put("file_checksum", hash);
        MDC.put("action", "FILE_UPLOAD");

        log.info("Starting file upload process: {}", description);

        try {
            // On cherche en BDD si un fichier possède déjà ce hash
            Optional<FileEntity> existingFile = fileRepository.findByChecksum(hash);

            if (existingFile.isPresent()) {
                // Option A : Lever une erreur
                log.warn("Upload aborted: File already exists with checksum {}", hash);
                throw new FileAlreadyExistsException("Ce fichier a déjà été uploadé.");

                // Option B : Retourner le nom du fichier existant sans uploader à nouveau
                // return existingFile.get().getStoredName();
            }

            if (file.isEmpty()) {
                throw new FileStorageException("Failed to store empty File !!!");
            }

            var originalFilename = file.getOriginalFilename();
            var cleanFileName = slugify(originalFilename);
            var fileName = UUID.randomUUID() + "-" + cleanFileName;


            if (fileName.contains("..")){
                throw new FileStorageException("Cannot store file with relative path outside current directory !!!" +  fileName);
            }

            var targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            var fileEntity = new FileEntity();
            fileEntity.setFileName(fileName);
            fileEntity.setOriginalFileName(originalFilename);
            fileEntity.setFileType(file.getContentType());
            fileEntity.setFileSize(String.valueOf(file.getSize()));
            fileEntity.setFilePath(targetLocation.toString());
            fileEntity.setDescription(description);
            fileEntity.setChecksum(hash);

            FileEntity saved = fileRepository.save(fileEntity);

            MDC.put("file_internal_name", fileName);
            log.info("File saved successfully. ID: {}", saved.getId());

            return FileEntityMapper.toDto(saved);

        }catch (IOException ex) {
            log.error("IO Exception during file storage", ex);
            throw new FileStorageException("Failed to store file !!!", ex);
        } finally {
            MDC.clear(); // Important : évite la pollution des logs des requêtes suivantes
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName){
        MDC.put("action", "FILE_DOWNLOAD");
        MDC.put("file_internal_name", fileName);
        log.info("Attempting to load resource");

        try {
            var filePath = this.fileStorageLocation.resolve(fileName).normalize();
            var resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            }else {
                log.error("Resource not found or not readable");
                throw new FileStorageException("Could not read file !!!" + fileName);
            }

        } catch (MalformedURLException ex) {
            log.error("URL Malformed for file: {}", fileName);
            throw new FileNotFoundException("File not found !!!" + fileName, ex);
        } finally {
            MDC.clear();
        }
    }

    @Override
    public List<FileEntityDto> getAllFiles(){
        MDC.put("action", "FILE_LIST_ALL");
        log.info("Fetching all non-deleted files from database");
        try{
            List<FileEntityDto> files = this.fileRepository.findAll().stream()
                    .filter(fileEntity -> !fileEntity.isDeleted())
                    .map(FileEntityMapper::toDto)
                    .toList();
            MDC.put("result_count", String.valueOf(files.size()));
            log.info("Successfully retrieved {} files", files.size());
            return files;
        } finally {
            MDC.clear();
        }
    }

    @Override
    public FileEntityDto getFileById(Long id){
        MDC.put("action", "FILE_GET_BY_ID");
        MDC.put("file_db_id", String.valueOf(id));
        log.info("Fetching details for file ID: {}", id);

        try{
            return fileRepository.findById(id)
                    .filter(fileEntity1 -> !fileEntity1.isDeleted())
                    .map(FileEntityMapper::toDto)
                    .orElseThrow(() -> {
                        log.warn("File with ID {} not found or is deleted", id);
                        return new FileNotFoundException("File not found !!!");
                    });
        } finally {
            MDC.clear();
        }
    }

    @Override
    public void deleteFileById(Long id){
        MDC.put("action", "FILE_DELETE");
        log.info("Deleting file By ID : {}", id);
        FileEntity fileEntity = fileRepository.findById(id)
                .filter(fileEntity1 -> !fileEntity1.isDeleted())
                .orElseThrow(() ->{
                    log.warn("Delete failed: File ID {} not found", id);
                    return new FileNotFoundException("File not found !!!");
                });
        fileEntity.setDeleted(true);
        log.info("File ID {} marked as deleted", id);
        MDC.clear();
    }

    @Override
    public List<FileEntityDto> searchFiles(String fileName){
        MDC.put("action", "FILE_SEARCH");
        MDC.put("search_query", fileName);
        log.info("Searching files containing: '{}'", fileName);
        try {
            List<FileEntityDto> results = fileRepository.findByFileNameContainsIgnoreCase(fileName).stream()
                    .filter(fileEntity1 -> !fileEntity1.isDeleted())
                    .map(FileEntityMapper::toDto)
                    .toList();
            MDC.put("result_count", String.valueOf(results.size()));
            log.info("Search completed with {} results", results.size());
            return results;
        } finally {
            MDC.clear();
        }
    }

    private String slugify(String input) {
        if (input == null) return "";

        // 1. Séparer le nom de l'extension
        int lastDot = input.lastIndexOf('.');
        String name = (lastDot != -1) ? input.substring(0, lastDot) : input;
        String extension = (lastDot != -1) ? input.substring(lastDot) : "";

        // 2. Normalisation (supprime les accents : é -> e)
        String nowhitespace = Pattern.compile("\\s+").matcher(name).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");

        // 3. Retourne le nom nettoyé en minuscule avec son extension
        return slug.toLowerCase(Locale.ENGLISH) + extension;
    }

    private String getFileChecksum(MultipartFile file) throws IOException{
        return DigestUtils.md5DigestAsHex(file.getInputStream());
    }
}
