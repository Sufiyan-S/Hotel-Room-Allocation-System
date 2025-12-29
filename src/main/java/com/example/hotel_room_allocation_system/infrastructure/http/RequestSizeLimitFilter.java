package com.example.hotel_room_allocation_system.infrastructure.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.net.URI;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestSizeLimitFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestSizeLimitFilter.class);
    private static final URI TYPE_PAYLOAD_TOO_LARGE = URI.create("urn:problem:payload-too-large");

    private final ApiLimitsProperties apiLimitsProperties;
    private final ObjectMapper objectMapper;

    public RequestSizeLimitFilter(ApiLimitsProperties apiLimitsProperties, ObjectMapper objectMapper) {
        this.apiLimitsProperties = apiLimitsProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isBodyExpected(request)) {
            long maxBytes = apiLimitsProperties.getMaxRequestSizeInBytes();
            long contentLength = request.getContentLengthLong();

            if (contentLength > maxBytes) {
                logger.warn("Rejecting request with Content-Length {} exceeding max allowed {}", contentLength, maxBytes);

                ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.PAYLOAD_TOO_LARGE);
                pd.setType(TYPE_PAYLOAD_TOO_LARGE);
                pd.setTitle("Payload Too Large");
                pd.setDetail("Request payload size " + contentLength + " exceeds the maximum allowed");
                pd.setInstance(URI.create(request.getRequestURI()));
                pd.setProperty("maxRequestBytes", maxBytes);
                pd.setProperty("contentLength", contentLength);

                response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                objectMapper.writeValue(response.getOutputStream(), pd);

                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private static boolean isBodyExpected(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method);
    }
}
