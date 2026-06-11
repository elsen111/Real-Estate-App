package com.realestate.backend.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private boolean success;
    private String message;
    private int status;
    private String path;
    private LocalDateTime timestamp;
    private Map<String, String> errors;

    public static ErrorResponse of(String message, int status, String path) {
        return ErrorResponse.builder().
                success(false).
                message(message).
                status(status).
                path(path).
                timestamp(LocalDateTime.now()).
                build();
    }

    public static ErrorResponse ofValidation(String message, int status, String path, Map<String, String> errors) {
        return ErrorResponse.builder().
                success(false).
                message(message).
                status(status).
                path(path).
                timestamp(LocalDateTime.now()).
                errors(errors).
                build();
    }
}
