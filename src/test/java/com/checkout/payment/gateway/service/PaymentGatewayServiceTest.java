package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.AcquiringBankClient;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.exception.BankServiceException;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.model.AcquiringBankPaymentRequest;
import com.checkout.payment.gateway.model.AcquiringBankPaymentResponse;
import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.checkout.payment.gateway.validation.PaymentRequestValidator;
import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

  @Mock
  private AcquiringBankClient acquiringBankClient;

  @Mock
  private PaymentsRepository paymentsRepository;

  @Mock
  private PaymentRequestValidator paymentRequestValidator;

  @InjectMocks
  private PaymentGatewayService paymentGatewayService;

  private PaymentRequest validPaymentRequest;

  @BeforeEach
  void setUp() {
    validPaymentRequest = new PaymentRequest();
    validPaymentRequest.setCardNumber("1234567890123456");
    validPaymentRequest.setExpiryMonth(12);
    validPaymentRequest.setExpiryYear(YearMonth.now().getYear() + 1);
    validPaymentRequest.setCurrency("USD");
    validPaymentRequest.setAmount(1000);
    validPaymentRequest.setCvv("123");
  }

  @Test
  void testProcessPaymentWithValidRequest_ShouldReturnsAuthorized() {

    when(paymentRequestValidator.validatePaymentRequest(any())).thenReturn(null);
    AcquiringBankPaymentResponse bankResponse = new AcquiringBankPaymentResponse();
    bankResponse.setAuthorized(true);
    when(acquiringBankClient.processPayment(any())).thenReturn(Optional.of(bankResponse));

    PaymentResponse response = paymentGatewayService.processPayment(validPaymentRequest);

    assertNotNull(response);
    assertNotNull(response.getId());
    assertEquals(PaymentStatus.AUTHORIZED, response.getStatus());
    assertEquals("USD", response.getCurrency());
    assertEquals(1000, response.getAmount());
    verify(paymentsRepository).add(response);
  }

  @Test
  void testProcessPaymentWithValidRequest_ShouldReturnsDeclined() {
    when(paymentRequestValidator.validatePaymentRequest(any())).thenReturn(null);
    AcquiringBankPaymentResponse bankResponse = new AcquiringBankPaymentResponse();
    bankResponse.setAuthorized(false);
    when(acquiringBankClient.processPayment(any())).thenReturn(Optional.of(bankResponse));

    PaymentResponse response = paymentGatewayService.processPayment(validPaymentRequest);

    assertNotNull(response);
    assertNotNull(response.getId());
    assertEquals(PaymentStatus.DECLINED, response.getStatus());
    verify(paymentsRepository).add(response);
  }

  @Test
  void testProcessPaymentWithInvalidRequest_ShouldReturnRejectedResponse() {

    String validationError = "Card number is required but was null or empty";
    when(paymentRequestValidator.validatePaymentRequest(any())).thenReturn(validationError);

    PaymentResponse response = paymentGatewayService.processPayment(validPaymentRequest);

    assertNotNull(response);
    assertNotNull(response.getId());
    assertEquals(PaymentStatus.REJECTED, response.getStatus());
    verify(acquiringBankClient, never()).processPayment(any());
    verify(paymentsRepository, never()).add(any());
  }

  @Test
  void testProcessPaymentWhenBankCallFails_ShouldThrowBankServiceException() {

    when(paymentRequestValidator.validatePaymentRequest(any())).thenReturn(null);
    when(acquiringBankClient.processPayment(any())).thenReturn(Optional.empty());

    BankServiceException exception = assertThrows(BankServiceException.class,
        () -> paymentGatewayService.processPayment(validPaymentRequest));
    
    assertEquals(503, exception.getHttpStatus());
    assertTrue(exception.getMessage().contains("unavailable"));
    verify(paymentsRepository, never()).add(any());
  }

  @Test
  void testProcessPayment_ShouldNotStoreRejectedPayment() {
    when(paymentRequestValidator.validatePaymentRequest(any())).thenReturn("Invalid request");

    PaymentResponse response = paymentGatewayService.processPayment(validPaymentRequest);

    assertEquals(PaymentStatus.REJECTED, response.getStatus());
    verify(paymentsRepository, never()).add(any());
  }

  @Test
  void testProcessPayment_ShouldStoreAuthorizedPayment() {

    when(paymentRequestValidator.validatePaymentRequest(any())).thenReturn(null);
    AcquiringBankPaymentResponse bankResponse = new AcquiringBankPaymentResponse();
    bankResponse.setAuthorized(true);
    when(acquiringBankClient.processPayment(any())).thenReturn(Optional.of(bankResponse));

    PaymentResponse response = paymentGatewayService.processPayment(validPaymentRequest);

    assertEquals(PaymentStatus.AUTHORIZED, response.getStatus());
    verify(paymentsRepository).add(response);
  }

  @Test
  void testProcessPayment_ShouldStoreDeclinePayments() {

    when(paymentRequestValidator.validatePaymentRequest(any())).thenReturn(null);
    AcquiringBankPaymentResponse bankResponse = new AcquiringBankPaymentResponse();
    bankResponse.setAuthorized(false);
    when(acquiringBankClient.processPayment(any())).thenReturn(Optional.of(bankResponse));

    PaymentResponse response = paymentGatewayService.processPayment(validPaymentRequest);

    assertEquals(PaymentStatus.DECLINED, response.getStatus());
    verify(paymentsRepository).add(response);
  }

  @Test
  void testGetPaymentByIdForValidId_ShouldReturnResponse() {

    UUID paymentId = UUID.randomUUID();
    PaymentResponse expectedPayment = new PaymentResponse();
    expectedPayment.setId(paymentId);
    expectedPayment.setStatus(PaymentStatus.AUTHORIZED);
    when(paymentsRepository.get(paymentId)).thenReturn(Optional.of(expectedPayment));

    PaymentResponse result = paymentGatewayService.getPaymentById(paymentId);

    assertNotNull(result);
    assertEquals(paymentId, result.getId());
    assertEquals(PaymentStatus.AUTHORIZED, result.getStatus());
  }

  @Test
  void testGetPaymentByIdWithNonExistingId_ShouldThrowException() {
    UUID nonExistentId = UUID.randomUUID();
    when(paymentsRepository.get(nonExistentId)).thenReturn(Optional.empty());

    EventProcessingException exception = assertThrows(EventProcessingException.class,
        () -> paymentGatewayService.getPaymentById(nonExistentId));
    assertTrue(exception.getMessage().contains("Invalid ID"));
    assertTrue(exception.getMessage().contains(nonExistentId.toString()));
  }

  @Test
  void testGetPaymentByIdWithRejectedStatus_ShouldThrowException() {
    UUID paymentId = UUID.randomUUID();
    PaymentResponse rejectedPayment = new PaymentResponse();
    rejectedPayment.setId(paymentId);
    rejectedPayment.setStatus(PaymentStatus.REJECTED);
    when(paymentsRepository.get(paymentId)).thenReturn(Optional.of(rejectedPayment));

    EventProcessingException exception = assertThrows(EventProcessingException.class,
        () -> paymentGatewayService.getPaymentById(paymentId));
    assertTrue(exception.getMessage().contains("Invalid ID"));
    assertTrue(exception.getMessage().contains(paymentId.toString()));
  }

  @Test
  void testProcessPayment_CurrencyShouldHaveBeenNormalizedToUppercase() {
    validPaymentRequest.setCurrency("usd");
    when(paymentRequestValidator.validatePaymentRequest(any())).thenReturn(null);
    AcquiringBankPaymentResponse bankResponse = new AcquiringBankPaymentResponse();
    bankResponse.setAuthorized(true);
    when(acquiringBankClient.processPayment(any())).thenReturn(Optional.of(bankResponse));

    PaymentResponse response = paymentGatewayService.processPayment(validPaymentRequest);

    assertEquals("USD", response.getCurrency());
    ArgumentCaptor<AcquiringBankPaymentRequest> captor = ArgumentCaptor.forClass(AcquiringBankPaymentRequest.class);
    verify(acquiringBankClient).processPayment(captor.capture());
    assertEquals("USD", captor.getValue().getCurrency());
  }

  @Test
  void testProcessPayment_ResponseShouldContainLastFourDigitsNotFullCardNumber() {
    validPaymentRequest.setCardNumber("1234567890123456");

    AcquiringBankPaymentResponse bankResponse = new AcquiringBankPaymentResponse();
    bankResponse.setAuthorized(true);

    when(paymentRequestValidator.validatePaymentRequest(any())).thenReturn(null);
    when(acquiringBankClient.processPayment(any())).thenReturn(Optional.of(bankResponse));
    PaymentResponse response = paymentGatewayService.processPayment(validPaymentRequest);

    assertEquals("3456", response.getLastFourDigits());
  }
}

