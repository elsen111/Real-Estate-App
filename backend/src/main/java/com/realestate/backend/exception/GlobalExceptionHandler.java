package com.realestate.backend.exception;

import com.realestate.backend.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse response = ErrorResponse.ofValidation(
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                errors
        );

        log.warn(
                "Validation failed at {}: {} field error(s)",
                request.getRequestURI(),
                errors.size()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AuthorizationDeniedException ex,
            HttpServletRequest request
    ) {

        log.warn(
                "Access denied at {}: {}",
                request.getRequestURI(),
                ex.getMessage()
        );

        return error(
                "You do not have permission to access this resource",
                HttpStatus.FORBIDDEN,
                request
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "{} at {}: {}",
                ex.getClass().getSimpleName(),
                request.getRequestURI(),
                ex.getMessage()
        );
        return error(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex,
            HttpServletRequest request
    ) {

        log.warn(
                "Bad request at {}: {}",
                request.getRequestURI(),
                ex.getMessage()
        );

        return error(ex.getMessage(), HttpStatus.BAD_REQUEST, request);

    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {

        log.warn(
                "Business rule violated at {}: {}",
                request.getRequestURI(),
                ex.getMessage()
        );

        return error(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException ex,
            HttpServletRequest request
    ) {

        log.warn(
                "Conflict at {}: {}",
                request.getRequestURI(),
                ex.getMessage()
        );

        return error(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            ForbiddenException ex,
            HttpServletRequest request
    ) {

        log.warn(
                "Forbidden request at {}: {}",
                request.getRequestURI(),
                ex.getMessage()
        );

        return error(ex.getMessage(), HttpStatus.FORBIDDEN, request);

    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOtpException(
            InvalidOtpException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Invalid otp at {}: {}",
                request.getRequestURI(),
                ex.getMessage()
        );

        return error(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({
            UnauthorizedException.class,
            BadCredentialsException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        String message = ex instanceof UnauthorizedException
                ? ex.getMessage()
                : "Invalid email or password";

        log.warn(
                "Unauthorized access attempt at {}",
                request.getRequestURI()
        );

        return error(message, HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            org.springframework.web.multipart.MaxUploadSizeExceededException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Upload rejected at {}: file size exceeded",
                request.getRequestURI()
        );

        return error("File size exceeded", HttpStatus.CONTENT_TOO_LARGE, request);
    }

    @ExceptionHandler(DuplicateInquiryException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateInquiryException(
            DuplicateInquiryException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Duplicate inquiry attempt at {}: {}",
                request.getRequestURI(),
                ex.getMessage()
        );

        return error(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(DuplicateAppointmentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateAppointmentException(
            DuplicateAppointmentException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "Duplicate appointment attempt at {}: {}",
                request.getRequestURI(),
                ex.getMessage()
        );

        return error(ex.getMessage(), HttpStatus.CONFLICT, request);
    }


    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageException(
            FileStorageException ex,
            HttpServletRequest request
    ) {
        log.error(
                "File storage error at {}: {}",
                request.getRequestURI(),
                ex.getMessage(),
                ex
        );

        return error(ex.getMessage(), ex.getStatus(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error(
                "Unhandled exception at {}",
                request.getRequestURI(),
                ex
        );

        return error("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ErrorResponse> error(
            String message,
            HttpStatus status,
            HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(
                message,
                status.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }
}