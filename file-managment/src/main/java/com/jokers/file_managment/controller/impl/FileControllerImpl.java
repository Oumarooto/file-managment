package com.jokers.file_managment.controller.impl;

import com.jokers.file_managment.controller.FileController;
import com.jokers.file_managment.dto.FileEntityDto;
import com.jokers.file_managment.mapper.FileEntityMapper;
import com.jokers.file_managment.payload.response.ApiResponse;
import com.jokers.file_managment.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file-management")
@Slf4j
public class FileControllerImpl implements FileController {

    private final FileService fileService;


    @Override
    @GetMapping("/hello")
    @Operation(summary = "Hello Test")
    public ResponseEntity<ApiResponse> hello() {
        ApiResponse response =ApiResponse.builder()
                .message("Hello World")
                .build();
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/upload")
    @Operation(summary = "Upload File")
    public ResponseEntity<FileEntityDto> saveFile(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("description") String description) {
        return ResponseEntity.ok(fileService.saveFile(file, description));
    }

    @Override
    @GetMapping("/download/{fileName:.+}")
    @Operation(summary = "Download File By Filename")
    public ResponseEntity<Resource> loadFileAsResource(@PathVariable String fileName, HttpServletRequest request) {

        var resource = fileService.loadFileAsResource(fileName);
        String contentType = null;

        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            log.error("Could not determine file type.", e);
        }

        if (Objects.isNull(contentType)) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Override
    @GetMapping()
    @Operation(summary = "Get All File")
    public ResponseEntity<List<FileEntityDto>> getAllFiles() {
        return ResponseEntity.ok(fileService.getAllFiles());
    }

    @Override
    @GetMapping("/file/{id}")
    @Operation(summary = "Get File By ID")
    public ResponseEntity<FileEntityDto> getFileById(@PathVariable Long id) {
        return ResponseEntity.ok(fileService.getFileById(id));
    }

    @Override
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete File By ID")
    public ResponseEntity<ApiResponse> deleteFileById(@PathVariable Long id) {
        fileService.deleteFileById(id);
        ApiResponse apiResponse = ApiResponse.builder()
                .message("File deleted")
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Override
    @GetMapping("/search")
    @Operation(summary = "Search file by Name")
    public ResponseEntity<List<FileEntityDto>> searchFiles(@RequestParam("keyword") String fileName) {
        return ResponseEntity.ok(fileService.searchFiles(fileName));
    }
}
