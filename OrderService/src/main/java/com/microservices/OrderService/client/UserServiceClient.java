package com.microservices.OrderService.client;


import com.microservices.OrderService.exception.UserNotFoundException;
import com.microservices.OrderService.exception.UserServiceErrorException;
import com.microservices.OrderService.exception.UserServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

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
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            response -> {
                                if (response.statusCode().value() == 404) {
                                    return Mono.error(new UserNotFoundException(
                                            "User with id " + userId + " not found in User Service"
                                    ));
                                }
                                return Mono.error(new UserServiceErrorException(
                                        "Client error from User Service: " + response.statusCode()
                                ));
                            }
                    )
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            response -> Mono.error(new UserServiceErrorException(
                                    "User Service returned server error: " + response.statusCode()
                            ))
                    )
                    .bodyToMono(Boolean.class)
                    .timeout(Duration.ofSeconds(5))
                    .retryWhen(
                            Retry.fixedDelay(2, Duration.ofMillis(500))
                                    .filter(this::isRetryable)
                                    .onRetryExhaustedThrow((retrySpec, signal) ->
                                            new UserServiceUnavailableException(
                                                    "User Service still unreachable after retries", signal.failure()
                                            )
                                    )
                    )
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

    private boolean isRetryable(Throwable throwable) {
        // Only retry connection failures / timeouts — NOT 4xx errors like user-not-found
        return throwable instanceof WebClientRequestException
                || throwable instanceof java.util.concurrent.TimeoutException;
    }
}
