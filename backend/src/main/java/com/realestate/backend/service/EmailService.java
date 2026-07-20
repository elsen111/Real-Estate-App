package com.realestate.backend.service;

public interface EmailService {

    void sendPasswordResetOtp(String toEmail, String otp);

}