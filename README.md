# payment-gateway-challenge-java
This is an API based application that allows processing and retrieving payments. As part of this project, a mock acquiring bank has been implemented to be able to test the whole flow. 

# Table of Contents
- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Design Decisions](#design-decisions)
- [503 Service Unavailable Handling](#503-Service-Unavailable-Handling)
- [Future Improvements](#Future-Improvements)
- [Getting Started](#Getting-Started)

## Overview
This project is a simplified implementation of a payment gateway that allows a merchant to securely process and retrieve online card payments. 
As part of this implementation, an acquiring bank gets simulated in order to allow us to demonstrate the full flow of a payment.
The purpose of this project is to demonstrate how a payment processing service, validates incoming requests, interacts with a downstream bank and returns safe masked payment information.

## Tech Stack
- **Java 17**
- **Spring Boot** 
- **Gradle** 
- **Lombok** 
- **Spring Web**
- **RestTemplate**
- **springdoc-openapi**
- **JUnit 5**
- **Mockito**
- **SLF4J / Logback**
- **Jackson**

## Design Decisions

### Architecture
- **Thin Controller Pattern**: controllers only handle HTTP routing and status code mapping.
- **Constructor Injection + Lombok**: ensures immutability and testability.
- **Single PaymentResponse DTO**: merged from skeleton's POST/GET responses for simplicity and consistency.
- **In-memory repository**: meets challenge requirement without DB complexity.
- **Global exception handler**: centralizes error formatting.

### 503 Service Unavailable Handling
When the acquiring bank returns `503 Service Unavailable` (e.g., when card number ends with 0) or any other bank service failure:

- **Strategy**: The gateway throws a `BankServiceException` which is caught by the global exception handler.
- **HTTP Response**: Returns `503 Service Unavailable` with an error message indicating the bank service is currently unavailable.
- **Behavior**: The payment is **not** stored, and the merchant receives a clear error response indicating they should retry the request later.
- **Rationale**: This distinguishes bank service failures from validation failures (REJECTED). A 503 indicates a temporary service issue that may be resolved on retry, whereas REJECTED indicates invalid input that will always fail.

## Future Improvements
- Add Integration tests
- Harden validation by Implementing Luhn check for PAN, expiry date not in past, amount range checks in order to reduce bad traffic to the bank
- Implement ConcurrentHashMap for thread-safety 
- Add External Database to store payments
- Idempotency on POST to prevent double charging on retries/timeouts

## Getting Started

**Prerequisites:** Java 17, Gradle

```bash
# Run bank simulator
docker compose up

# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test
```
