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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

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
            throw new FileStorageException("Could not create upload directory", ex);
        }
    }


    @Override
    public FileEntityDto saveFile(MultipartFile file, String description){
        log.info("Saving file {}", description);

        if (file.isEmpty()) {
            throw new FileStorageException("Failed to store empty File !!!");
        }

        var originalFilename = file.getOriginalFilename();
        var fileName = UUID.randomUUID() + " - " + originalFilename;

        try {
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

            return FileEntityMapper.toDto(fileRepository.save(fileEntity));

        }catch (IOException ex) {
            throw new FileStorageException("Failed to store file !!!" + fileName, ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName){
        log.info("Loading file as Resource {}", fileName);

        try {
            var filePath = this.fileStorageLocation.resolve(fileName).normalize();
            var resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            }else {
                throw new FileStorageException("Could not read file !!!" + fileName);
            }

        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("File not found !!!" + fileName, ex);
        }
    }

    @Override
    public List<FileEntityDto> getAllFiles(){
        log.info("Getting all files");
        return this.fileRepository.findAll().stream()
                .filter(fileEntity -> !fileEntity.isDeleted())
                .map(FileEntityMapper::toDto)
                .toList();
    }

    @Override
    public FileEntityDto getFileById(Long id){
        log.info("Getting file By ID : {}", id);
        return fileRepository.findById(id)
                .filter(fileEntity1 -> !fileEntity1.isDeleted())
                .map(FileEntityMapper::toDto)
                .orElseThrow(() -> new FileNotFoundException("File not found !!!"));
    }

    @Override
    public void deleteFileById(Long id){
        log.info("Deleting file By ID : {}", id);
        FileEntity fileEntity = fileRepository.findById(id)
                .filter(fileEntity1 -> !fileEntity1.isDeleted())
                .orElseThrow(() -> new FileNotFoundException("File not found !!!"));
        fileEntity.setDeleted(true);
    }

    @Override
    public List<FileEntityDto> searchFiles(String fileName){
        log.info("Searching files by name : {}", fileName);
        return fileRepository.findByFileNameContainsIgnoreCase(fileName).stream()
                .filter(fileEntity1 -> !fileEntity1.isDeleted())
                .map(FileEntityMapper::toDto)
                .toList();
    }
}
