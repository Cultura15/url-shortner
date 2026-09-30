package com.jesson.create_url_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimitFilterClientIpTest {

    @Test
    void forwardedForUsesFirstAddressFromChain() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        HttpServletRequest request = createRequest("198.51.100.10, 10.0.0.2", "203.0.113.99");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        FilterChain chain = mock(FilterChain.class);

        for (int requestNumber = 0; requestNumber < 11; requestNumber++) {
            filter.doFilter(request, response, chain);
        }

        assertEquals("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}", body.toString());
    }

    @Test
    void forwardedForUsesSingleAddressWhenNoChainIsPresent() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        HttpServletRequest request = createRequest("198.51.100.10", "203.0.113.99");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        FilterChain chain = mock(FilterChain.class);

        for (int requestNumber = 0; requestNumber < 11; requestNumber++) {
            filter.doFilter(request, response, chain);
        }

        assertEquals("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}", body.toString());
    }

    @Test
    void missingForwardedForFallsBackToRemoteAddress() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        HttpServletRequest request = createRequest(null, "203.0.113.99");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        FilterChain chain = mock(FilterChain.class);

        for (int requestNumber = 0; requestNumber < 11; requestNumber++) {
            filter.doFilter(request, response, chain);
        }

        assertEquals("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}", body.toString());
    }

    private HttpServletRequest createRequest(String forwardedFor, String remoteAddress) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/create-url");
        when(request.getHeader("X-Forwarded-For")).thenReturn(forwardedFor);
        when(request.getRemoteAddr()).thenReturn(remoteAddress);
        return request;
    }
}
