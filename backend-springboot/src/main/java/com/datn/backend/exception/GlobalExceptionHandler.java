package com.datn.backend.exception;

import com.datn.backend.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.error("Resource not found exception: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceConflictException(
            ResourceConflictException ex, WebRequest request) {
        log.error("Resource conflict exception: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.CONFLICT.value(), ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation error: {}", errors);
        
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Validation failed");
        response.setData(errors);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex, WebRequest request) {
        log.error("Access denied exception: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Access is denied");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unhandled exception occurred: ", ex);
        
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                "Internal Server Error"
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
