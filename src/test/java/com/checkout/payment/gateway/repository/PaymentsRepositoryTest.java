package com.checkout.payment.gateway.repository;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PaymentResponse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentsRepositoryTest {

  private PaymentsRepository repository;

  @BeforeEach
  void setUp() {
    repository = new PaymentsRepository();
  }

  @Test
  void TestAddWithValidPayment_ShouldStorePayment() {

    PaymentResponse payment = createPaymentResponse(UUID.randomUUID(), PaymentStatus.AUTHORIZED);

    repository.add(payment);

    Optional<PaymentResponse> retrieved = repository.get(payment.getId());
    assertTrue(retrieved.isPresent());
    assertEquals(payment.getId(), retrieved.get().getId());
    assertEquals(payment.getStatus(), retrieved.get().getStatus());
  }

  @Test
  void testGetWithExistingId_ShouldReturnPayment() {

    UUID paymentId = UUID.randomUUID();
    PaymentResponse payment = createPaymentResponse(paymentId, PaymentStatus.AUTHORIZED);
    repository.add(payment);

    Optional<PaymentResponse> result = repository.get(paymentId);

    assertTrue(result.isPresent());
    assertEquals(paymentId, result.get().getId());
    assertEquals(payment.getStatus(), result.get().getStatus());
    assertEquals(payment.getAmount(), result.get().getAmount());
    assertEquals(payment.getCurrency(), result.get().getCurrency());
  }

  @Test
  void testGetWithNonExistentId_ShouldReturnEmptyOptional() {

    UUID nonExistentId = UUID.randomUUID();
    Optional<PaymentResponse> result = repository.get(nonExistentId);

    assertFalse(result.isPresent());
  }

  @Test
  void testAddWithSameId_shouldOverwritePreviousPayment() {

    UUID paymentId = UUID.randomUUID();
    PaymentResponse firstPayment = createPaymentResponse(paymentId, PaymentStatus.AUTHORIZED);
    firstPayment.setAmount(1000);
    firstPayment.setCurrency("USD");

    PaymentResponse secondPayment = createPaymentResponse(paymentId, PaymentStatus.DECLINED);
    secondPayment.setAmount(2000);
    secondPayment.setCurrency("GBP");

    repository.add(firstPayment);
    repository.add(secondPayment);

    Optional<PaymentResponse> result = repository.get(paymentId);
    assertTrue(result.isPresent());
    assertEquals(PaymentStatus.DECLINED, result.get().getStatus());
    assertEquals(2000, result.get().getAmount());
    assertEquals("GBP", result.get().getCurrency());
  }

  @Test
  void testAddWithMultiplePayments_ShouldStoreThemAll() {

    UUID paymentId1 = UUID.randomUUID();
    UUID paymentId2 = UUID.randomUUID();
    UUID paymentId3 = UUID.randomUUID();

    PaymentResponse payment1 = createPaymentResponse(paymentId1, PaymentStatus.AUTHORIZED);
    PaymentResponse payment2 = createPaymentResponse(paymentId2, PaymentStatus.DECLINED);
    PaymentResponse payment3 = createPaymentResponse(paymentId3, PaymentStatus.AUTHORIZED);

    repository.add(payment1);
    repository.add(payment2);
    repository.add(payment3);

    assertTrue(repository.get(paymentId1).isPresent());
    assertTrue(repository.get(paymentId2).isPresent());
    assertTrue(repository.get(paymentId3).isPresent());
    assertEquals(PaymentStatus.AUTHORIZED, repository.get(paymentId1).get().getStatus());
    assertEquals(PaymentStatus.DECLINED, repository.get(paymentId2).get().getStatus());
    assertEquals(PaymentStatus.AUTHORIZED, repository.get(paymentId3).get().getStatus());
  }

  private PaymentResponse createPaymentResponse(UUID id, PaymentStatus status) {
    PaymentResponse payment = new PaymentResponse();
    payment.setId(id);
    payment.setStatus(status);
    payment.setAmount(1000);
    payment.setCurrency("USD");
    payment.setLastFourDigits("1234");
    payment.setCardExpiryDate("12/25");
    payment.setExpiryMonth(12);
    payment.setExpiryYear(2025);
    return payment;
  }
}

