package com.checkout.payment.gateway.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class PaymentRequest implements Serializable {

  @JsonProperty("card_number")
  private String cardNumber;
  @JsonProperty("expiry_month")
  private int expiryMonth;
  @JsonProperty("expiry_year")
  private int expiryYear;
  private String currency;
  private int amount;
  private String cvv;

  public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  @JsonIgnore
  public String getCardNumberLastFour() {
    if (cardNumber == null || cardNumber.trim().isEmpty()) {
      return "";
    }
    String pan = cardNumber.trim();
    return pan.length() >= 4 ? pan.substring(pan.length() - 4) : pan;
  }

  public int getExpiryMonth() {
    return expiryMonth;
  }

  public void setExpiryMonth(int expiryMonth) {
    this.expiryMonth = expiryMonth;
  }

  public int getExpiryYear() {
    return expiryYear;
  }

  public void setExpiryYear(int expiryYear) {
    this.expiryYear = expiryYear;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public String getCvv() {
    return cvv;
  }

  public void setCvv(String cvv) {
    this.cvv = cvv;
  }

  @JsonIgnore
  public String getExpiryDate() {
    // MM/YYYY Bank simulator format
    return String.format("%02d/%04d", expiryMonth, expiryYear);
  }

  @JsonIgnore
  public String getExpiryDateForResponse() {
    // MM/YY API format
    int yearLastTwo = expiryYear % 100;
    return String.format("%02d/%02d", expiryMonth, yearLastTwo);
  }

  @Override
  public String toString() {
    return "PaymentRequest{" +
        "cardNumber=****" +
        ", expiryMonth=" + expiryMonth +
        ", expiryYear=" + expiryYear +
        ", currency='" + currency + '\'' +
        ", amount=" + amount +
        ", cvv=****" +
        '}';
  }
}