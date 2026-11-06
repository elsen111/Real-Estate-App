package com.realestate.backend.utils.otp;

import com.realestate.backend.config.properties.PasswordResetProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class OtpHashUtil {

    private final PasswordResetProperties properties;

    public String hash(String otp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec key = new SecretKeySpec(
                    properties.otpHmacSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            mac.init(key);

            byte[] hash = mac.doFinal(otp.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception exception) {
            throw new IllegalStateException("Could not hash OTP.", exception);
        }
    }
}