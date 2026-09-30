package com.jesson.create_url_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitFilterDoFilterTest {

    @Test
    void nonCreateUrlRequestPassesThroughWithoutRateLimiting() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        HttpServletRequest request = requestFor("/actuator/health", "203.0.113.1");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    void createUrlRequestUnderLimitPassesThrough() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        HttpServletRequest request = requestFor("/api/v1/create-url", "203.0.113.1");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    void createUrlRequestOverLimitReturns429AndErrorBody() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        HttpServletRequest request = requestFor("/api/v1/create-url", "203.0.113.1");
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        FilterChain chain = mock(FilterChain.class);

        for (int requestNumber = 0; requestNumber < 11; requestNumber++) {
            filter.doFilter(request, response, chain);
        }

        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        assertEquals("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}", body.toString());
    }

    private HttpServletRequest requestFor(String uri, String remoteAddress) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getRemoteAddr()).thenReturn(remoteAddress);
        return request;
    }
}
