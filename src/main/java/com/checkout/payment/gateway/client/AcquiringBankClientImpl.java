package com.checkout.payment.gateway.client;

import com.checkout.payment.gateway.model.AcquiringBankPaymentRequest;
import com.checkout.payment.gateway.model.AcquiringBankPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class AcquiringBankClientImpl implements AcquiringBankClient {

  private final RestTemplate restTemplate;
  private final String bankSimulatorUrl;

  @Override
  public Optional<AcquiringBankPaymentResponse> processPayment(AcquiringBankPaymentRequest request) {
    try {
      ResponseEntity<AcquiringBankPaymentResponse> response =
          restTemplate.postForEntity(bankSimulatorUrl, request, AcquiringBankPaymentResponse.class);

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        return Optional.of(response.getBody());
      } else {
        log.warn("Bank returned non-2xx status: {}", response.getStatusCode());
        return Optional.empty();
      }

    } catch (RestClientException ex) {
      log.warn("Bank call failed: {}", ex.getMessage());
      return Optional.empty();
    }
  }
}