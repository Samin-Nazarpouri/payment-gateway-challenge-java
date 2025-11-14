package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class CommonExceptionHandler {

  @ExceptionHandler(EventProcessingException.class)
  public ResponseEntity<ErrorResponse> handleEventProcessingException(EventProcessingException ex) {
    log.error("EventProcessingException: {}", ex.getMessage(), ex);
    return new ResponseEntity<>(new ErrorResponse("Payment not found"),
        HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(BankServiceException.class)
  public ResponseEntity<ErrorResponse> handleBankServiceException(BankServiceException ex) {
    log.error("BankServiceException: {}", ex.getMessage(), ex);
    // Use the HTTP status from the exception (e.g., 503 for Service Unavailable)
    HttpStatus status = ex.getHttpStatus() == 503 
        ? HttpStatus.SERVICE_UNAVAILABLE 
        : HttpStatus.INTERNAL_SERVER_ERROR;
    return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), status);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);
    return new ResponseEntity<>( new ErrorResponse("Internal server error occurred"),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
