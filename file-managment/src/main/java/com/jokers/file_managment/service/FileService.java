package com.jokers.file_managment.service;

import com.jokers.file_managment.dto.FileEntityDto;
import com.jokers.file_managment.model.FileEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {

    FileEntityDto saveFile(MultipartFile file, String description) throws IOException;
    Resource loadFileAsResource(String fileName);
    List<FileEntityDto> getAllFiles();
    FileEntityDto getFileById(Long id);
    void deleteFileById(Long id);
    List<FileEntityDto> searchFiles (String fileName);

}
