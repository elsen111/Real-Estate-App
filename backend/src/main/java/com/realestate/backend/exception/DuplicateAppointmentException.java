package com.realestate.backend.exception;

public class DuplicateAppointmentException extends RuntimeException {
    public DuplicateAppointmentException(String message) {
        super(message);
    }
}
