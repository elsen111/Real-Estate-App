package com.realestate.backend.service;

import com.realestate.backend.entity.UserEntity;

public interface OtpService {

    void generateAndSendOtp(UserEntity user);

    void verifyOtp(UserEntity user, String otp);

}
