package com.checkout.payment.gateway.service;

import com.checkout.payment.gateway.client.AcquiringBankClient;
import com.checkout.payment.gateway.model.AcquiringBankPaymentRequest;
import com.checkout.payment.gateway.model.AcquiringBankPaymentResponse;
import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.repository.PaymentsRepository;
import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.validation.PaymentRequestValidator;
import com.checkout.payment.gateway.exception.EventProcessingException;
import com.checkout.payment.gateway.exception.BankServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentGatewayService {

  private final AcquiringBankClient acquiringBankClient;
  private final PaymentsRepository paymentsRepository;
  private final PaymentRequestValidator paymentRequestValidator;

  public PaymentResponse processPayment(PaymentRequest paymentRequest) {
    UUID paymentId = UUID.randomUUID();

    // Validating request before calling Bank simulator
    String reason = paymentRequestValidator.validatePaymentRequest(paymentRequest);
    if (reason != null) {
      log.warn("Rejected (validation) paymentId={}, reason={}", paymentId, reason);
      return buildResponse(paymentRequest, paymentId, PaymentStatus.REJECTED);
    }

    // Build bank request for when request is valid
    AcquiringBankPaymentRequest bankReq = buildBankRequest(paymentRequest);

    // Call bank simulator
    Optional<AcquiringBankPaymentResponse> bankRes = acquiringBankClient.processPayment(bankReq);
    log.info("Calling bank for PaymentId {}", paymentId);

    // If bank call failed (e.g., 503, network error), throw exception
    // REJECTED status is ONLY for validation failures, not bank failures
    AcquiringBankPaymentResponse bankResponse = bankRes.orElseThrow(() -> {
      log.error("Bank service unavailable or failed for paymentId={}", paymentId);
      return new BankServiceException(
          "Acquiring bank service is currently unavailable. Please try again later.",
          503);
    });

    // Map bank response outcomes
    PaymentStatus status = bankResponse.isAuthorized() 
        ? PaymentStatus.AUTHORIZED 
        : PaymentStatus.DECLINED;

    // Build & store response (only AUTHORIZED and DECLINED are stored, not REJECTED)
    PaymentResponse response = buildResponse(paymentRequest, paymentId, status);
    paymentsRepository.add(response);
    return response;
  }

  public PaymentResponse getPaymentById(UUID id) {
    PaymentResponse found = paymentsRepository.get(id)
        .orElseThrow(() -> createInvalidIdException(id));
    if (found.getStatus() == PaymentStatus.REJECTED) {
      throw createInvalidIdException(id);
    }
    return found;
  }

  private EventProcessingException createInvalidIdException(UUID id) {
    return new EventProcessingException("Invalid ID, paymentId: " + id);
  }

  // helper to map API request -> bank request
  private AcquiringBankPaymentRequest buildBankRequest(PaymentRequest r) {
    AcquiringBankPaymentRequest req = new AcquiringBankPaymentRequest();
    req.setCardNumber(r.getCardNumber());
    req.setExpiryDate(r.getExpiryDate());  // bank simulator format -> "MM/YYYY"
    req.setCurrency(normalizeCurrency(r.getCurrency()));
    req.setAmount(r.getAmount());
    req.setCvv(r.getCvv());

    return req;
  }

  // helper to assemble masked API response
  private PaymentResponse buildResponse(PaymentRequest request, UUID id, PaymentStatus status) {
    PaymentResponse resp = new PaymentResponse();
    resp.setId(id);
    resp.setStatus(status);

    // Get last four digits
    String last4 = request.getCardNumberLastFour();

    resp.setLastFourDigits(last4);
    resp.setCardExpiryDate(request.getExpiryDateForResponse()); //  API format -> "MM/YY"
    resp.setExpiryMonth(request.getExpiryMonth());
    resp.setExpiryYear(request.getExpiryYear());
    resp.setCurrency(normalizeCurrency(request.getCurrency()));
    resp.setAmount(request.getAmount());

    return resp;
  }

  private String normalizeCurrency(String currency) {
    if (currency != null) {
      return currency.trim().toUpperCase(Locale.ROOT);
    }
    return null;
  }
}