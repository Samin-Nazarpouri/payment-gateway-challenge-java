package com.checkout.payment.gateway.configuration;

import java.time.Duration;
import com.checkout.payment.gateway.client.AcquiringBankClient;
import com.checkout.payment.gateway.client.AcquiringBankClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofMillis(10000))
        .setReadTimeout(Duration.ofMillis(10000))
        .build();
  }

  @Bean
  public AcquiringBankClient acquiringBankClient(RestTemplate restTemplate, @Value("${bank.simulator.url}") String url) {
    return new AcquiringBankClientImpl(restTemplate, url);
  }
}
