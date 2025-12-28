package com.jokers.file_managment.mapper;

import com.jokers.file_managment.dto.FileEntityDto;
import com.jokers.file_managment.model.FileEntity;

public class FileEntityMapper {

    public static FileEntityDto toDto(FileEntity fileEntity) {
        return FileEntityDto.builder()
                .id(fileEntity.getId())
                .fileName(fileEntity.getFileName())
                .originalFileName(fileEntity.getOriginalFileName())
                .fileSize(fileEntity.getFileSize())
                .fileType(fileEntity.getFileType())
                .filePath(fileEntity.getFilePath())
                .deleted(fileEntity.isDeleted())
                .uploadTime(fileEntity.getUploadTime())
                .description(fileEntity.getDescription())
                .build();
    }

    public static FileEntity toEntity(FileEntityDto fileEntityDto) {
        return FileEntity.builder()
                .build();
    }
}
