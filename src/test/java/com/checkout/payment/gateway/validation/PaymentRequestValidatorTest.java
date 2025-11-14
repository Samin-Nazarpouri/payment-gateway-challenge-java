package com.checkout.payment.gateway.validation;

import com.checkout.payment.gateway.model.PaymentRequest;
import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentRequestValidatorTest {

  private PaymentRequestValidator validator;
  private PaymentRequest validRequest;

  @BeforeEach
  void setUp() {
    validator = new PaymentRequestValidator();
    validRequest = createValidRequest();
  }

  private PaymentRequest createValidRequest() {
    PaymentRequest request = new PaymentRequest();
    request.setCardNumber("1234567890123456");
    request.setExpiryMonth(12);
    request.setExpiryYear(YearMonth.now().getYear() + 1);
    request.setCurrency("USD");
    request.setAmount(1000);
    request.setCvv("123");
    return request;
  }

  @Test
  void testPaymentValidatorWithNullRequest_ShouldThrowError() {
    String result = validator.validatePaymentRequest(null);
    assertNotNull(result);
    assertTrue(result.contains("null"));
  }

  @Test
  void TestPaymentValidatorWithValidRequest_ShouldReturnNull() {
    String result = validator.validatePaymentRequest(validRequest);
    assertNull(result);
  }

  @Test
  void TestValidateCardNoWithNullCardNumber_ShouldReturnError() {
    validRequest.setCardNumber(null);
    String result = validator.validatePaymentRequest(validRequest);
    assertNotNull(result);
    assertTrue(result.contains("Card number"));
    assertTrue(result.contains("null"));
  }

  @Test
  void TestvalidateCardNoWithEmptyCardNumber_ShouldReturnError() {
    validRequest.setCardNumber("");

    String result = validator.validatePaymentRequest(validRequest);
    assertNotNull(result);
    assertTrue(result.contains("Card number"));
    assertTrue(result.contains("empty"));
  }

  @Test
  void testValidatingCardNoWithTooShortCardNumber_ShouldThrowError() {
    validRequest.setCardNumber("1234567890123"); // 13 digits
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("Card number length"));
    assertTrue(result.contains("14"));
  }

  @Test
  void testValidatingCardNumberWithTooLongCardNumber_ShouldThrowError() {
    validRequest.setCardNumber("12345678901234567890"); // 20 digits
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("Card number length"));
    assertTrue(result.contains("19"));
  }

  @Test
  void testValidatingCardNumberWithNonNumericCardNumber_ShouldReturnError() {
    validRequest.setCardNumber("12345678901234AB");
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("Card number format"));
    assertTrue(result.contains("numeric"));
  }

  @Test
  void testValidatingCardNumberWithValidCardNo_ShouldReturnNull() {
    validRequest.setCardNumber("12345678901234"); // 14 digits - minimum valid
    String result = validator.validatePaymentRequest(validRequest);
    assertNull(result);
  }

  @Test
  void testValidatingExpiryMonthWithInvalidMonth_ShouldReturnError() {
    validRequest.setExpiryMonth(0);
    String result = validator.validatePaymentRequest(validRequest);
    assertNotNull(result);
    assertTrue(result.contains("Expiry month"));
    assertTrue(result.contains("1-12"));
  }

  @Test
  void testValidatingExpiryMonthWithMonthGreaterThan12_ShouldReturnError() {
    validRequest.setExpiryMonth(13);
    String result = validator.validatePaymentRequest(validRequest);
    assertNotNull(result);
    assertTrue(result.contains("Expiry month"));
    assertTrue(result.contains("1-12"));
  }

  @Test
  void testValidatingExpiryMonthWithValidMonth_ShouldReturnNull() {
    validRequest.setExpiryMonth(6);
    String result = validator.validatePaymentRequest(validRequest);
    assertNull(result);
  }

  @Test
  void testValidatingExpiryDateWithPastYear_ShouldReturnError() {
    validRequest.setExpiryYear(YearMonth.now().getYear() - 1);
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("Expiry year"));
    assertTrue(result.contains("past"));
  }

  @Test
  void testValidateExpiryDateWithPastMonthInCurrentYear_ShouldReturnError() {
    int currentYear = YearMonth.now().getYear();
    int currentMonth = YearMonth.now().getMonthValue();
    validRequest.setExpiryYear(currentYear);
    validRequest.setExpiryMonth(currentMonth - 1);

    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("Expiry date"));
    assertTrue(result.contains("past"));
  }

  @Test
  void testValidatingExpiryDateWithFutureDate_ShouldReturnNull() {
    validRequest.setExpiryYear(YearMonth.now().getYear() + 2);
    validRequest.setExpiryMonth(6);

    String result = validator.validatePaymentRequest(validRequest);

    assertNull(result);
  }

  @Test
  void testValidatingCurrencyWithNullCurrency_ShouldReturnsError() {
    validRequest.setCurrency(null);
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("Currency"));
    assertTrue(result.contains("null"));
  }

  @Test
  void testVlidatingCurrencyWithEmptyCurrency_ShouldReturnError() {
    validRequest.setCurrency("");
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("Currency"));
    assertTrue(result.contains("empty"));
  }

  @Test
  void testValidatingCurrencyWithUnsupportedCurrency_ShouldReturnError() {
    validRequest.setCurrency("JPY");
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("Currency is not supported"));
    assertTrue(result.contains("JPY"));
  }

  @Test
  void testValidatingCurrencyWithTooShortCurrency_ShouldReturnError() {
    validRequest.setCurrency("US"); // 2 characters
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("Currency length is invalid"));
    assertTrue(result.contains("exactly 3 characters"));
  }

  @Test
  void testValidatingCurrencyWithTooLongCurrency_ShouldReturnError() {
    validRequest.setCurrency("USDD"); // 4 characters
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("Currency length is invalid"));
    assertTrue(result.contains("exactly 3 characters"));
  }

  @Test
  void testValidateCurrencyWithSingleCharacter_ShouldReturnError() {
    validRequest.setCurrency("U"); // 1 character
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("Currency length is invalid"));
    assertTrue(result.contains("exactly 3 characters"));
  }

  @Test
  void testValidatingCurrencyWithUSD_ShouldReturnsNull() {
    validRequest.setCurrency("USD");
    String result = validator.validatePaymentRequest(validRequest);
    assertNull(result);
  }

  @Test
  void testValidatingCurrency_WithGBP_ShouldReturnNull() {
    validRequest.setCurrency("GBP");
    String result = validator.validatePaymentRequest(validRequest);
    assertNull(result);
  }

  @Test
  void testValidatingCurrency_WithEUR_ShouldReturnNull() {
    validRequest.setCurrency("EUR");
    String result = validator.validatePaymentRequest(validRequest);
    assertNull(result);
  }

  @Test
  void testValidatingCurrencyWithLowerCaseSupportedCurrency_ShouldReturnNull() {
    validRequest.setCurrency("gbp");
    String result = validator.validatePaymentRequest(validRequest);
    assertNull(result);
  }

  @Test
  void testValidatingCurrencyWithLowerCaseUSD_ShouldReturnNull() {
    validRequest.setCurrency("usd");
    String result = validator.validatePaymentRequest(validRequest);
    assertNull(result);
  }

  @Test
  void testValidatingCurrencyWithLowerCaseEUR_ShouldReturnNull() {
    validRequest.setCurrency("eur");
    String result = validator.validatePaymentRequest(validRequest);
    assertNull(result);
  }

  @Test
  void testValidatingAmountWithZeroAmount_ShouldThrowError() {
    validRequest.setAmount(0);
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("Amount"));
    assertTrue(result.contains("positive"));
  }

  @Test
  void testValidatingAmountWithNegativeInt_ShouldThrowError() {
    validRequest.setAmount(-100);
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("Amount"));
    assertTrue(result.contains("positive"));
  }

  @Test
  void testValidatingAmountWithPositiveInt_ShouldReturnNull() {
    validRequest.setAmount(1);
    String result = validator.validatePaymentRequest(validRequest);
    assertNull(result);
  }

  @Test
  void testValidateCVVWithNullValue_ShouldThrowError() {
    validRequest.setCvv(null);
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("CVV"));
    assertTrue(result.contains("null"));
  }

  @Test
  void testValidatingCVVWithEmptyValye_ShouldReturnError() {
    validRequest.setCvv("");
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("CVV"));
    assertTrue(result.contains("empty"));
  }

  @Test
  void testValidateCVVWithTooShortValue_ShouldReturnError() {
    validRequest.setCvv("12"); // 2 digits
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("CVV"));
    assertTrue(result.contains("3-4"));
  }

  @Test
  void testValidateTooLongCVV_ShouldThrowError() {
    validRequest.setCvv("12345"); // 5 digits
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("CVV"));
    assertTrue(result.contains("3-4"));
  }

  @Test
  void testValidateCvv_WithNonNumericValue_ShouldThrowError() {
    validRequest.setCvv("12A");
    String result = validator.validatePaymentRequest(validRequest);

    assertNotNull(result);
    assertTrue(result.contains("CVV"));
    assertTrue(result.contains("3-4"));
  }

  @Test
  void testValidateValid3DigitCvv_ShouldReturnNull() {
    validRequest.setCvv("123");
    String result = validator.validatePaymentRequest(validRequest);
    assertNull(result);
  }

  @Test
  void testValidate4DigitCvv_ShouldReturnNull() {
    validRequest.setCvv("1234");
    String result = validator.validatePaymentRequest(validRequest);
    assertNull(result);
  }
}

