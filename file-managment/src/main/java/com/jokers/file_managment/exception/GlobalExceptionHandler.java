package com.jokers.file_managment.exception;

import com.jokers.file_managment.dto.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponses> handleFileStorageException(FileStorageException e) {
        return ResponseEntity
                .badRequest()
                .body(new ApiResponses(false, "FILE NOT FOUND"));
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiResponses> handleFileNotFoundException(FileNotFoundException e) {
        return ResponseEntity
                .notFound()
                .build();
    }

    @ExceptionHandler(MalformedURLException.class)
    public ResponseEntity<ApiResponses> handleMalformedURLException(MalformedURLException e) {
        return ResponseEntity.badRequest().body(new ApiResponses(false, "File is too big !!!"));
    }



    @ExceptionHandler(FileAlreadyExistsException.class)
    public ResponseEntity<ApiResponses> handleFileAlreadyExistsException(Exception e) {
        return ResponseEntity.internalServerError().body(new ApiResponses(false, "File already exists !!!"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponses> handleException(Exception e) {
        return ResponseEntity.internalServerError().body(new ApiResponses(false, "Internal Server Error"));
    }
}
