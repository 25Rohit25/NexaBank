package com.nexabank.account.client;

import com.nexabank.account.exception.CustomerValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class CustomerClient {

    private final RestClient restClient;

    public CustomerClient(RestClient.Builder builder,
                          @Value("${clients.customer-service.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public void requireCustomer(String customerId, String bearerToken) {
        try {
            restClient.get()
                    .uri("/api/v1/customers/{id}", customerId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new CustomerValidationException(HttpStatus.BAD_REQUEST, "Customer does not exist");
            }
            if (exception.getStatusCode().value() == 401 || exception.getStatusCode().value() == 403) {
                throw new CustomerValidationException(HttpStatus.FORBIDDEN, "Customer identity could not be authorized");
            }
            throw new CustomerValidationException(HttpStatus.BAD_GATEWAY, "Customer Service rejected validation");
        } catch (RestClientException exception) {
            throw new CustomerValidationException(HttpStatus.SERVICE_UNAVAILABLE, "Customer Service is unavailable");
        }
    }
}

