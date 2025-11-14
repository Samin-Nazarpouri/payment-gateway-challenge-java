package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.model.AcquiringBankPaymentRequest;
import com.checkout.payment.gateway.model.AcquiringBankPaymentResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcquiringBankClientImplTest {

  @Mock
  private RestTemplate restTemplate;

  private AcquiringBankClientImpl acquiringBankClient;
  private static final String BANK_SIMULATOR_URL = "http://localhost:8080/bank/simulator";

  @BeforeEach
  void setUp() {
    acquiringBankClient = new AcquiringBankClientImpl(restTemplate, BANK_SIMULATOR_URL);
  }

  @Test
  void processPayment_WithSuccessfulResponse_ReturnsOptionalWithResponse() {

    AcquiringBankPaymentRequest request = createValidRequest();
    AcquiringBankPaymentResponse bankResponse = new AcquiringBankPaymentResponse();
    bankResponse.setAuthorized(true);
    bankResponse.setAuthorizationCode("AUTH123");

    ResponseEntity<AcquiringBankPaymentResponse> responseEntity =
        new ResponseEntity<>(bankResponse, HttpStatus.OK);

    when(restTemplate.postForEntity(
        eq(BANK_SIMULATOR_URL),
        any(AcquiringBankPaymentRequest.class),
        eq(AcquiringBankPaymentResponse.class)))
        .thenReturn(responseEntity);

    Optional<AcquiringBankPaymentResponse> result = acquiringBankClient.processPayment(request);

    assertTrue(result.isPresent());
    assertEquals(bankResponse, result.get());
    assertTrue(result.get().isAuthorized());
    assertEquals("AUTH123", result.get().getAuthorizationCode());

    ArgumentCaptor<AcquiringBankPaymentRequest> requestCaptor =
        ArgumentCaptor.forClass(AcquiringBankPaymentRequest.class);
    verify(restTemplate).postForEntity(
        eq(BANK_SIMULATOR_URL),
        requestCaptor.capture(),
        eq(AcquiringBankPaymentResponse.class));
    AcquiringBankPaymentRequest capturedRequest = requestCaptor.getValue();
    assertEquals(request.getCardNumber(), capturedRequest.getCardNumber());
    assertEquals(request.getCurrency(), capturedRequest.getCurrency());
    assertEquals(request.getAmount(), capturedRequest.getAmount());
  }

  @Test
  void processPayment_WithNon2xxResponse_ReturnsEmptyOptional() {

    AcquiringBankPaymentRequest request = createValidRequest();
    ResponseEntity<AcquiringBankPaymentResponse> responseEntity =
        new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

    when(restTemplate.postForEntity(
        eq(BANK_SIMULATOR_URL),
        any(AcquiringBankPaymentRequest.class),
        eq(AcquiringBankPaymentResponse.class)))
        .thenReturn(responseEntity);

    Optional<AcquiringBankPaymentResponse> result = acquiringBankClient.processPayment(request);

    assertFalse(result.isPresent());
  }

  @Test
  void processPayment_WithNullBody_ReturnsEmptyOptional() {

    AcquiringBankPaymentRequest request = createValidRequest();
    ResponseEntity<AcquiringBankPaymentResponse> responseEntity =
        new ResponseEntity<>(null, HttpStatus.OK);

    when(restTemplate.postForEntity(
        eq(BANK_SIMULATOR_URL),
        any(AcquiringBankPaymentRequest.class),
        eq(AcquiringBankPaymentResponse.class)))
        .thenReturn(responseEntity);

    Optional<AcquiringBankPaymentResponse> result = acquiringBankClient.processPayment(request);

    assertFalse(result.isPresent());
  }

  @Test
  void processPayment_WithRestClientException_ReturnsEmptyOptional() {

    AcquiringBankPaymentRequest request = createValidRequest();

    when(restTemplate.postForEntity(
        eq(BANK_SIMULATOR_URL),
        any(AcquiringBankPaymentRequest.class),
        eq(AcquiringBankPaymentResponse.class)))
        .thenThrow(new RestClientException("Connection timeout"));

    Optional<AcquiringBankPaymentResponse> result = acquiringBankClient.processPayment(request);

    assertFalse(result.isPresent());
  }

  @Test
  void processPayment_With5xxResponse_ReturnsEmptyOptional() {

    AcquiringBankPaymentRequest request = createValidRequest();
    ResponseEntity<AcquiringBankPaymentResponse> responseEntity =
        new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

    when(restTemplate.postForEntity(
        eq(BANK_SIMULATOR_URL),
        any(AcquiringBankPaymentRequest.class),
        eq(AcquiringBankPaymentResponse.class)))
        .thenReturn(responseEntity);

    Optional<AcquiringBankPaymentResponse> result = acquiringBankClient.processPayment(request);

    assertFalse(result.isPresent());
  }

  private AcquiringBankPaymentRequest createValidRequest() {
    AcquiringBankPaymentRequest request = new AcquiringBankPaymentRequest();
    request.setCardNumber("1234567890123456");
    request.setExpiryDate("12/2025");
    request.setCurrency("USD");
    request.setAmount(1000);
    request.setCvv("123");
    return request;
  }
}

