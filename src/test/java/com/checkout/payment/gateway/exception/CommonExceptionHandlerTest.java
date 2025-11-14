package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.model.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommonExceptionHandlerTest {

  private CommonExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp() {
    exceptionHandler = new CommonExceptionHandler();
  }

  @Test
  void testEventProcessingException_ReturnsNotFoundWithCorrectMessage() {

    String errorMessage = "Invalid ID, paymentId: 123e4567-e89b-12d3-a456-426614174000";
    EventProcessingException exception = new EventProcessingException(errorMessage);

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleEventProcessingException(exception);

    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    ErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals("Payment not found", body.getMessage());
  }

  @Test
  void testEventProcessingException_WithNullMessage_ReturnsNotFound() {
    EventProcessingException exception = new EventProcessingException(null);
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleEventProcessingException(exception);

    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    ErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals("Payment not found", body.getMessage());
  }

  @Test
  void testEventProcessingException_WithEmptyMessage_ReturnsNotFound() {

    EventProcessingException exception = new EventProcessingException("");
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleEventProcessingException(exception);

    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    ErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals("Payment not found", body.getMessage());
  }

  @Test
  void testGeneric_WithRuntimeException_ReturnsInternalServerError() {

    RuntimeException exception = new RuntimeException("Something went wrong");
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneric(exception);

    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals("Internal server error occurred", body.getMessage());
  }

  @Test
  void testGeneric_WithNullPointerException_ReturnsInternalServerError() {
    NullPointerException exception = new NullPointerException("Null value encountered");
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneric(exception);

    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals("Internal server error occurred", body.getMessage());
  }

  @Test
  void testGeneric_WithIllegalArgumentException_ReturnsInternalServerError() {
    IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneric(exception);

    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals("Internal server error occurred", body.getMessage());
  }

  @Test
  void testGeneric_WithNullMessage_ReturnsInternalServerError() {

    Exception exception = new Exception((String) null);
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneric(exception);

    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals("Internal server error occurred", body.getMessage());
  }

  @Test
  void testGeneric_WithEmptyMessage_ReturnsInternalServerError() {

    Exception exception = new Exception("");
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneric(exception);

    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ErrorResponse body = response.getBody();
    assertNotNull(body);
    assertEquals("Internal server error occurred", body.getMessage());
  }
}

