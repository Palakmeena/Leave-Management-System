package com.example.Leave_management_system.exception;
// src/main/java/com/example/Leave_management_system/exception/GlobalExceptionHandler.java


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // BadRequestException -> 400
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequest(BadRequestException ex, WebRequest request){
        return buildResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    // ConflictException -> 409
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> handleConflict(ConflictException ex, WebRequest request){
        return buildResponse(ex, HttpStatus.CONFLICT, request);
    }

    // ForbiddenException -> 403
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<?> handleForbidden(ForbiddenException ex, WebRequest request){
        return buildResponse(ex, HttpStatus.FORBIDDEN, request);
    }

    // NotFoundException -> 404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex, WebRequest request){
        return buildResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    // Fallback for other exceptions -> 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex, WebRequest request){
        return buildResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // Common builder
    private ResponseEntity<Map<String,Object>> buildResponse(Exception ex, HttpStatus status, WebRequest request){
        Map<String,Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", ex.getMessage(),
                "path", request.getDescription(false).replace("uri=","")
        );
        return new ResponseEntity<>(body, status);
    }
}
