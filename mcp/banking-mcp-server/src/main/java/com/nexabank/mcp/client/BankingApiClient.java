package com.nexabank.mcp.client;

import com.nexabank.mcp.dto.AccountView;
import com.nexabank.mcp.dto.BalanceView;
import com.nexabank.mcp.dto.TransactionView;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class BankingApiClient {
    private final RestClient restClient;

    public BankingApiClient(RestClient bankingRestClient) {
        this.restClient = bankingRestClient;
    }

    public List<AccountView> getCustomerAccounts(String customerId, String token) {
        List<AccountView> result = restClient.get().uri("/api/v1/accounts/customer/{customerId}", customerId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token).retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return result == null ? List.of() : result;
    }

    public BalanceView getAccountBalance(String accountId, String token) {
        return restClient.get().uri("/api/v1/accounts/{accountId}/balance", accountId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token).retrieve().body(BalanceView.class);
    }

    public List<TransactionView> getTransactions(String accountId, String from, String to,
                                                  BigDecimal minAmount, String token) {
        List<String> query = new ArrayList<>();
        if (from != null && !from.isBlank()) query.add("from=" + encode(from));
        if (to != null && !to.isBlank()) query.add("to=" + encode(to));
        if (minAmount != null) query.add("minAmount=" + encode(minAmount.toPlainString()));
        String uri = "/api/v1/transactions/account/" + encode(accountId)
                + (query.isEmpty() ? "" : "?" + String.join("&", query));
        List<TransactionView> result = restClient.get().uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token).retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return result == null ? List.of() : result;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
