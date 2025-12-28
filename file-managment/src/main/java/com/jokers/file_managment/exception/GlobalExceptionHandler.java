package com.jokers.file_managment.exception;

import com.jokers.file_managment.dto.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.MalformedURLException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponses> handleFileStorageException(FileStorageException e) {
        return ResponseEntity
                .badRequest()
                .body(new ApiResponses(false, e.getMessage()));
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponses> handleException(Exception e) {
        return ResponseEntity.internalServerError().body(new ApiResponses(false, "Internal Server Error"));
    }
}
