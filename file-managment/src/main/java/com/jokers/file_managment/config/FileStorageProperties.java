package com.jokers.file_managment.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "file")
@Validated
public record FileStorageProperties(

        @NotBlank(message = "Le répertoire de stockage doit être défini")
        String uploadDir
){}
