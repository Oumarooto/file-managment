package com.jokers.file_managment.controller;

import com.jokers.file_managment.dto.FileEntityDto;
import com.jokers.file_managment.payload.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileController {

    ResponseEntity<ApiResponse> hello();

    ResponseEntity<FileEntityDto> saveFile(MultipartFile file, String description);
    ResponseEntity<Resource> loadFileAsResource(String fileName, HttpServletRequest request);
    ResponseEntity<List<FileEntityDto>> getAllFiles();
    ResponseEntity<FileEntityDto> getFileById(Long id);
    ResponseEntity<ApiResponse> deleteFileById(Long id);
    ResponseEntity<List<FileEntityDto>> searchFiles (String fileName);
}
