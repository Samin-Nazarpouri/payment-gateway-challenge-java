package com.checkout.payment.gateway.validation;

import com.checkout.payment.gateway.model.PaymentRequest;
import java.time.YearMonth;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestValidator {

  // Validating on 3 currencies
  private static final Set<String> VALID_CURRENCIES = Set.of("USD", "GBP", "EUR");

  /**
   * Validates the payment request and returns a rejection reason if invalid, or
   * null if valid.
   *
   * @param request the payment request to validate
   * @return rejection reason string if invalid, null if valid
   */
  public String validatePaymentRequest(PaymentRequest request) {
    if (request == null) {
      return "Payment request is null";
    }

    String cardNumberValidation = validateCardNumber(request.getCardNumber());
    if (cardNumberValidation != null) {
      return cardNumberValidation;
    }

    // Expiry month: 1-12
    String expiryMonthValidation = validateExpiryMonth(request.getExpiryMonth());
    if (expiryMonthValidation != null) {
      return expiryMonthValidation;
    }

    // Expiry year + month combo to be in the future
    String expiryDateValidation = validateExpiryDate(request.getExpiryMonth(),
        request.getExpiryYear());
    if (expiryDateValidation != null) {
      return expiryDateValidation;
    }
    // validating currency against the 3 currencies set for the system
    String currencyValidation = validateCurrency(request.getCurrency());

    if (currencyValidation != null) {
      return currencyValidation;
    }

    // Amount: must be positive integer
    String amountValidation = validateAmount(request.getAmount());

    if (amountValidation != null) {
      return amountValidation;
    }

    // CVV: must be 3-4 characters long, numeric only
    String cvvValidation = validateCvv(request.getCvv());

    if (cvvValidation != null) {
      return cvvValidation;
    }

    return null;
  }

  /**
   * Validates card number according to its validation rule
   *
   * @param cardNumber
   * @return String reason if invalid or null if valid
   */
  private String validateCardNumber(String cardNumber) {
    if (cardNumber == null || cardNumber.trim().isEmpty()) {
      return "Card number is required but was null or empty";
    }

    String trimmed = cardNumber.trim();
    int length = trimmed.length();

    if (length < 14 || length > 19) {
      return "Card number length is invalid: " + length + " characters (This must be between 14–19 digits)";
    }

    if (!trimmed.matches("[0-9]+")) {
      String masked = "****" + trimmed.substring(length - 4);
      return "Card number format is invalid: " + masked + " (This must contain only numeric digits)";
    }

    return null;
  }

  private String validateExpiryMonth(int expiryMonth) {
    if (expiryMonth < 1) {
      return "Expiry month is invalid: " + expiryMonth + " (This must be between 1-12)";
    }
    if (expiryMonth > 12) {
      return "Expiry month is invalid: " + expiryMonth + " (This must be between 1-12)";
    }
    return null;
  }

  private String validateExpiryDate(int expiryMonth, int expiryYear) {
    int currentYear = YearMonth.now().getYear();
    int currentMonth = YearMonth.now().getMonthValue();
    if (expiryYear < currentYear) {
      return "Expiry year" + expiryYear
          + " is in the past; This must be a future year (current year: " + currentYear + ")";
    }
    if (expiryYear == currentYear && expiryMonth <= currentMonth) {
      return "Expiry date is not in the future: " + expiryMonth + "/" + expiryYear + " (current date: "
          + currentMonth + "/" + currentYear + ")";
    }
    return null;
  }

  private String validateCurrency(String currency) {
    if (currency == null || currency.isBlank()) {
      return "Currency is required but was null/empty";
    }
    String currencyCode = currency.trim().toUpperCase(Locale.ROOT);

    // Currency must be exactly 3 characters
    if (currencyCode.length() != 3) {
      return "Currency length is invalid: " + currencyCode.length()
          + " characters (Currency must be exactly 3 characters)";
    }

    if (!VALID_CURRENCIES.contains(currencyCode)) {
      return "Currency is not supported: " + currencyCode
          + " (supported currencies are: " + String.join(", ", VALID_CURRENCIES) + ")";
    }
    return null;
  }

  private String validateAmount(int amount) {
    if (amount <= 0) {
      return "Amount is invalid: " + amount + " ( This must be a positive integer)";
    }
    return null;
  }

  private String validateCvv(String cvv) {

    if (cvv == null || cvv.isEmpty()) {
      return "CVV is required but was null or empty";
    }

    String trimmedCvv = cvv.trim();
    // Checks for numeric digits only and length between 3-4 digits
    if (!trimmedCvv.matches("^[0-9]{3,4}$")) {
      return "CVV format is invalid. CVV must contain 3-4 digits";
    }
    return null;
  }
}