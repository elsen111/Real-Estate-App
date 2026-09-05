package com.realestate.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class FileStorageException
        extends RuntimeException {

    private final HttpStatus status;

    public FileStorageException(
            String message
    ) {
        this(
                message,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    public FileStorageException(
            String message,
            HttpStatus status
    ) {
        super(message);
        this.status = status;
    }

    public FileStorageException(
            String message,
            Throwable cause
    ) {
        this(
                message,
                cause,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    public FileStorageException(
            String message,
            Throwable cause,
            HttpStatus status
    ) {
        super(message, cause);
        this.status = status;
    }
}