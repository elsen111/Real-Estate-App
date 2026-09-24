package com.realestate.backend.dto.response;

import com.realestate.backend.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class PaymentCheckoutResponse {

    private UUID paymentId;
    private String checkoutUrl;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;

}