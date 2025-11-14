package com.checkout.payment.gateway.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.YearMonth;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    PaymentsRepository paymentsRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetEndPointWithExistingId() throws Exception {
        PaymentResponse payment = new PaymentResponse();
        payment.setId(UUID.randomUUID());
        payment.setAmount(10);
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setExpiryMonth(12);
        payment.setExpiryYear(2024);
        payment.setLastFourDigits("4321");
        payment.setCardExpiryDate(String.format("%02d/%02d", payment.getExpiryMonth(), payment.getExpiryYear() % 100));

        paymentsRepository.add(payment);

        mvc.perform(MockMvcRequestBuilders.get("/api/v1/payment/" + payment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId().toString()))
                .andExpect(jsonPath("$.status").value(payment.getStatus().getName()))
                .andExpect(jsonPath("$.lastFourDigits").value(payment.getLastFourDigits()))
                .andExpect(jsonPath("$.cardExpiryDate").doesNotExist())
                .andExpect(jsonPath("$.expiryMonth").value(payment.getExpiryMonth()))
                .andExpect(jsonPath("$.expiryYear").value(payment.getExpiryYear()))
                .andExpect(jsonPath("$.currency").value(payment.getCurrency()))
                .andExpect(jsonPath("$.amount").value(payment.getAmount()));
    }

    @Test
    void testGetEndpointWithNonExistingId_404Returned() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/api/v1/payment/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Payment not found"));
    }

    @Test
    void testGetEndPointForRejectedPayment_404Returned() throws Exception {

        PaymentResponse rejectedPayment = new PaymentResponse();
        rejectedPayment.setId(UUID.randomUUID());
        rejectedPayment.setStatus(PaymentStatus.REJECTED);
        rejectedPayment.setAmount(1000);
        rejectedPayment.setCurrency("GBP");
        rejectedPayment.setLastFourDigits("1234");

        paymentsRepository.add(rejectedPayment);

        mvc.perform(MockMvcRequestBuilders.get("/api/v1/payment/" + rejectedPayment.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Payment not found"));
    }

    @Test
    void testPostEndPointwithValidRequest_ReturnValidPaymentResponse() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setCardNumber("1234567890123451"); // valid card details
        request.setExpiryMonth(12);
        request.setExpiryYear(YearMonth.now().getYear() + 1);
        request.setCurrency("GBP");
        request.setAmount(1000);
        request.setCvv("123");

        int currentYear = YearMonth.now().getYear();
        int nextYear = currentYear + 1;

        mvc.perform(MockMvcRequestBuilders.post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.lastFourDigits").value("3451"))
                .andExpect(jsonPath("$.cardExpiryDate").doesNotExist())
                .andExpect(jsonPath("$.expiryMonth").value(12))
                .andExpect(jsonPath("$.expiryYear").value(nextYear))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.amount").value(1000));
    }

    @Test
    void testPOSTEndPointWithInvalidPaymentRequest_ReturnsRejectedStatus() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setCardNumber("1234567890123"); // Invalid card number (13dig)
        request.setExpiryMonth(12);
        request.setExpiryYear(YearMonth.now().getYear() + 1);
        request.setCurrency("GBP");
        request.setAmount(1000);
        request.setCvv("123");

        mvc.perform(MockMvcRequestBuilders.post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // should return 400 Bad Request
                .andExpect(jsonPath("$.status").value("Rejected"));
    }

    @Test
    void testPOSTEndPointWithNullRequest_ReturnedRejectedStatus() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("Rejected"));
    }
}
