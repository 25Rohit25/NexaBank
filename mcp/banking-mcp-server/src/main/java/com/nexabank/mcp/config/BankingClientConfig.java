package com.nexabank.mcp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BankingClientConfig {
    @Bean
    RestClient bankingRestClient(@Value("${banking.gateway-url}") String gatewayUrl) {
        return RestClient.builder().baseUrl(gatewayUrl).build();
    }
}
