package com.realestate.backend.exception;

public class DuplicateInquiryException extends RuntimeException {
    public DuplicateInquiryException(String message) {
        super(message);
    }
}
