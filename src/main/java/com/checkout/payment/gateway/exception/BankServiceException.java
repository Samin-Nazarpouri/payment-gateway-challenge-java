package com.checkout.payment.gateway.exception;

/**
 * Exception thrown when the acquiring bank service is unavailable or fails.
 * This includes scenarios like 503 Service Unavailable or network errors.
 */
public class BankServiceException extends RuntimeException {
    private final int httpStatus;

    public BankServiceException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public BankServiceException(String message, Throwable cause, int httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
