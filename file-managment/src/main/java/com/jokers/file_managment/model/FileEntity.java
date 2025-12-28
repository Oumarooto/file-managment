package com.jokers.file_managment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String fileSize;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private boolean deleted;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime uploadTime;

    private String description;
}
