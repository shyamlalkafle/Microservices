package com.microservices.OrderService.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder,
                             @Value("${user-service.base-url}") String userServiceBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(userServiceBaseUrl).build();
    }

    public boolean userExists(Long userId) {
        Boolean exists = webClient.get()
                .uri("/api/users/{id}/exists", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
        return Boolean.TRUE.equals(exists);
    }
}
