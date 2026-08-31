package com.nexabank.gateway.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {
    @Test
    void createsAndForwardsCorrelationIdWhenMissing() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        new CorrelationIdFilter().doFilter(new MockHttpServletRequest(), response, chain);

        String id = response.getHeader(CorrelationIdFilter.HEADER);
        assertThat(id).isNotBlank();
        assertThat(((HttpServletRequest) chain.getRequest()).getHeader(CorrelationIdFilter.HEADER)).isEqualTo(id);
    }

    @Test
    void preservesExistingCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER, "REQ-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new CorrelationIdFilter().doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("REQ-123");
    }
}
