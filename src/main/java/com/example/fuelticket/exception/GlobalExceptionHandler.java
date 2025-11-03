package com.example.fuelticket.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", "");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        // Check if it's a validation error that should be 400 instead of 500
        String message = ex.getMessage();
        if (message != null && (
            message.contains("dépasse la capacité maximale") ||
            message.contains("Quantité insuffisante") ||
            message.contains("existe déjà") ||
            message.contains("non trouvé") ||
            message.contains("not found")
        )) {
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("error", "Bad Request");
            body.put("message", message);
            body.put("path", "");
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
        
        // For other runtime exceptions, return 500
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());
        body.put("path", "");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

