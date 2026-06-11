package com.realestate.backend.exception;

import com.realestate.backend.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidException(
        MethodArgumentNotValidException ex,
        HttpServletRequest req
    ) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse response = ErrorResponse.ofValidation(
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                req.getRequestURI(),
                errors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }



    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            org.springframework.web.multipart.MaxUploadSizeExceededException ex,
            HttpServletRequest req
    ) {
        ErrorResponse response = ErrorResponse.of(
                "File size exceeded",
                HttpStatus.CONTENT_TOO_LARGE.value(),
                req.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONTENT_TOO_LARGE)
                .body(response);
    }



    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest req
    ) {
        ErrorResponse response = ErrorResponse.of(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                req.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }



    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            BadRequestException ex,
            HttpServletRequest req
    ) {
        ErrorResponse response = ErrorResponse.of(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                req.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }



    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest req
    ) {
        ErrorResponse response = ErrorResponse.of(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                req.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }



    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException ex,
            HttpServletRequest req
    ) {
        ErrorResponse response = ErrorResponse.of(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                req.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }



    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageException(
            FileStorageException ex,
            HttpServletRequest req
    ) {
        HttpStatus status = ex.getStatus();

        ErrorResponse response = ErrorResponse.of(
                ex.getMessage(),
                status.value(),
                req.getRequestURI()
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }



    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            ForbiddenException ex,
            HttpServletRequest req
    ) {
        ErrorResponse response = ErrorResponse.of(
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value(),
                req.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }


    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            UnauthorizedException ex,
            HttpServletRequest req
    ) {
        ErrorResponse response = ErrorResponse.of(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                req.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception exception,
            HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(
                "Something went wrong",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
