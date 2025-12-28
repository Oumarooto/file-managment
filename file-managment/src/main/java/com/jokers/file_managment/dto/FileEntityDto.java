package com.jokers.file_managment.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileEntityDto {

    private Long id;
    private String fileName;
    private String originalFileName;
    private String fileSize;
    private String fileType;
    private String filePath;
    private boolean deleted;
    private LocalDateTime uploadTime;
    private String description;
}
