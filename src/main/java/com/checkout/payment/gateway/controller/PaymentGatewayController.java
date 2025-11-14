package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.enums.PaymentStatus;
import com.checkout.payment.gateway.model.PaymentRequest;
import com.checkout.payment.gateway.model.PaymentResponse;
import com.checkout.payment.gateway.service.PaymentGatewayService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PaymentGatewayController {

  private final PaymentGatewayService paymentGatewayService;

  @GetMapping("/payment/{id}")
  public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable UUID id) {
    return new ResponseEntity<>(paymentGatewayService.getPaymentById(id), HttpStatus.OK);
  }

  @PostMapping("/payment")
  public ResponseEntity<PaymentResponse> executePayment(@RequestBody PaymentRequest paymentRequest) {
    PaymentResponse response = paymentGatewayService.processPayment(paymentRequest);
    
    // REJECTED status -> validation failed -> return 400 Bad Request
    // AUTHORIZED , DECLINED -> successful processing -> return 200 OK
    HttpStatus httpStatus = response.getStatus() == PaymentStatus.REJECTED 
        ? HttpStatus.BAD_REQUEST 
        : HttpStatus.OK;
    
    return new ResponseEntity<>(response, httpStatus);
  }
}
