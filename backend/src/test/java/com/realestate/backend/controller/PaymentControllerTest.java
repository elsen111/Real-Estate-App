package com.realestate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realestate.backend.dto.response.PaymentCheckoutResponse;
import com.realestate.backend.dto.response.PaymentResponse;
import com.realestate.backend.security.CustomUserDetails;
import com.realestate.backend.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private CustomUserDetails currentUser;

    @Mock
    private PaymentCheckoutResponse checkoutResponse;

    @Mock
    private PaymentResponse paymentResponse;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UUID userId = UUID.randomUUID();
    private final UUID planId = UUID.randomUUID();
    private final UUID paymentId = UUID.randomUUID();

    private static final String CHECKOUT_SESSION_ID = "cs_test_123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PaymentController(paymentService))
                .setCustomArgumentResolvers(
                        new CurrentUserArgumentResolver(currentUser)
                )
                .build();
    }

    @Test
    void createCheckout_shouldReturnCreatedCheckout() throws Exception {
        mockCurrentUser();

        when(paymentService.createCheckout(
                userId,
                planId,
                "test-idempotency-key"
        )).thenReturn(checkoutResponse);

        mockMvc.perform(
                        post("/payments/checkout")
                                .param("planId", planId.toString())
                                .header("Idempotency-Key", "test-idempotency-key")
                )
                .andExpect(status().isOk());

        verify(paymentService).createCheckout(
                userId,
                planId,
                "test-idempotency-key"
        );
    }

    @Test
    void getPayment_shouldReturnPayment() throws Exception {
        mockCurrentUser();

        when(paymentService.getPayment(
                userId,
                paymentId
        )).thenReturn(paymentResponse);

        mockMvc.perform(
                        get("/payments/{paymentId}", paymentId)
                )
                .andExpect(status().isOk());

        verify(paymentService).getPayment(
                userId,
                paymentId
        );
    }

    @Test
    void getPaymentBySession_shouldReturnPayment() throws Exception {
        mockCurrentUser();

        when(paymentService.getPaymentBySession(
                userId,
                CHECKOUT_SESSION_ID
        )).thenReturn(paymentResponse);

        mockMvc.perform(
                        get("/payments/session/{checkoutSessionId}", CHECKOUT_SESSION_ID)
                )
                .andExpect(status().isOk());

        verify(paymentService).getPaymentBySession(
                userId,
                CHECKOUT_SESSION_ID
        );
    }

    @Test
    void handleWebhook_shouldReturnOk() throws Exception {
        String payload = """
                {
                    "id": "evt_test_123",
                    "type": "checkout.session.completed"
                }
                """;

        mockMvc.perform(
                        post("/payments/webhook")
                                .contentType("application/json")
                                .content(payload)
                                .header("Stripe-Signature", "test-signature")
                )
                .andExpect(status().isOk());

        verify(paymentService).handleWebhook(
                payload,
                "test-signature"
        );
    }

    private void mockCurrentUser() {
        when(currentUser.getId()).thenReturn(userId);
    }

    private static class CurrentUserArgumentResolver
            implements HandlerMethodArgumentResolver {

        private final CustomUserDetails currentUser;

        private CurrentUserArgumentResolver(CustomUserDetails currentUser) {
            this.currentUser = currentUser;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                    && parameter.getParameterType().equals(CustomUserDetails.class);
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            return currentUser;
        }
    }
}