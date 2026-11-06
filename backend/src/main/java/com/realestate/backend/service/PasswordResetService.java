package com.realestate.backend.service;

import com.realestate.backend.dto.request.ForgotPasswordRequest;
import com.realestate.backend.dto.request.VerifyResetOtpRequest;
import com.realestate.backend.dto.request.ResetPasswordRequest;
import com.realestate.backend.dto.response.VerifyResetOtpResponse;

public interface PasswordResetService {

    void forgotPassword(ForgotPasswordRequest request);

    VerifyResetOtpResponse verifyResetOtp(VerifyResetOtpRequest request);

    void resetPassword(ResetPasswordRequest request);
}