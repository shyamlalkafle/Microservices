package com.microservices.PaymentServices.service;

import com.microservices.OrderService.exception.UserServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.math.BigDecimal;
import java.time.Duration;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder,
                             @Value("${user-service.base-url}") String userServiceBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(userServiceBaseUrl).build();
    }

    public boolean deductBalance(Long userId, BigDecimal amount) {
        try {
            Boolean success = webClient.patch()
                    .uri("/api/users/{id}/deduct?amount={amount}", userId, amount)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return Boolean.TRUE.equals(success);
        } catch (WebClientRequestException e) {
            throw new UserServiceUnavailableException(
                    "Unable to connect to User Service at " + e.getUri() +
                            ". Service may be down or unreachable.", e
            );
        } catch (Exception e) {
            return false;
        }
    }
}
