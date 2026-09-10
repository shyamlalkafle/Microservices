package com.microservices.OrderService.service;

import com.microservices.OrderService.dto.PaymentRequest;
import com.microservices.OrderService.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;

@Component
public class PaymentServiceClient {

    private final WebClient webClient;

    public PaymentServiceClient(WebClient.Builder webClientBuilder,
                                @Value("${payment-service.base-url}") String paymentServiceBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(paymentServiceBaseUrl).build();
    }

    public boolean processPayment(Long orderId, Long userId, BigDecimal amount) {
        try {
            PaymentRequest request = new PaymentRequest(orderId, userId, amount);

            PaymentResponse response = webClient.post()
                    .uri("/api/payments")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PaymentResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            return response != null && "SUCCESS".equals(response.getStatus());

        } catch (Exception e) {
            return false; // payment-service unreachable/slow/error → treat as failed
        }
    }
}
