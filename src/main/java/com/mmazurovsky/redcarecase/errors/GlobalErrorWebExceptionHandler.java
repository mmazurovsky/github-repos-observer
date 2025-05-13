package com.mmazurovsky.redcarecase.errors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;


@Component
@Order(-2) // Ensures high precedence
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalErrorWebExceptionHandler.class);
    private final ObjectMapper objectMapper;

    public GlobalErrorWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatusCode status = determineStatus(ex);
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = new ErrorResponse(extractErrorMessage(ex));

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(toJsonBytes(errorResponse))));
    }

    private HttpStatusCode determineStatus(Throwable ex) {
        if (findBindException(ex) != null) return HttpStatus.BAD_REQUEST;
        if (ex instanceof ResponseStatusException rse) return rse.getStatusCode();
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String extractErrorMessage(Throwable ex) {
        WebExchangeBindException bind = findBindException(ex);
        if (bind != null) {
            return bind.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("; "));
        }

        if (ex instanceof ResponseStatusException rse) {
            return (rse.getReason() != null) ? rse.getReason()
                    : rse.getStatusCode().toString();
        }

        logger.error("Unexpected error", ex);

        return "Unexpected error";
    }

    /** Walk the cause chain and return the first WebExchangeBindException (or null). */
    private WebExchangeBindException findBindException(Throwable t) {
        while (t != null) {
            if (t instanceof WebExchangeBindException web) return web;
            t = t.getCause();
        }
        return null;
    }


    private byte[] toJsonBytes(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize error response", e);
            return "{\"error\":\"Internal server error\"}".getBytes(StandardCharsets.UTF_8);
        }
    }

    public record ErrorResponse(String error) {
    }
}

