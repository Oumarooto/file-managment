package com.jokers.file_managment.repository;

import com.jokers.file_managment.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    List<FileEntity> findByFileNameContainsIgnoreCase(String fileName);
    List<FileEntity> findByFileType(String fileType);
    Optional<FileEntity> findByFileName(String fileName);
    Optional<FileEntity> findByChecksum(String fileName);

}
