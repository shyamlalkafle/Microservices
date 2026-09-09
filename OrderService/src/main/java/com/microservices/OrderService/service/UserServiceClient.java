package com.microservices.OrderService.service;


import com.microservices.OrderService.exception.UserServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.time.Duration;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder,
                             @Value("${user-service.base-url}") String userServiceBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(userServiceBaseUrl).build();
    }

    public boolean userExists(Long userId) {
        try {
            Boolean exists = webClient.get()
                    .uri("/api/users/{id}/exists", userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return Boolean.TRUE.equals(exists);
        } catch (WebClientRequestException e) {
            throw new UserServiceUnavailableException(
                    "Unable to connect to User Service at " + e.getUri() +
                            ". Service may be down or unreachable.", e
            );
        }catch (Exception e) {
            throw new RuntimeException("Could not verify user with User Service: " + e.getMessage(), e);
        }
    }
}
