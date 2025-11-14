package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.model.AcquiringBankPaymentRequest;
import com.checkout.payment.gateway.model.AcquiringBankPaymentResponse;
import java.util.Optional;

public interface AcquiringBankClient {

  /**
   * Calls the bank simulator to process a payment.
   *
   * @param request request payload (snake_case fields on wire)
   * @return Optional of BankPaymentResponse when the call succeeded with 2xx and a body,
   * Optional.empty() for any non-2xx or client/network error.
   */
  Optional<AcquiringBankPaymentResponse> processPayment(AcquiringBankPaymentRequest request);
}