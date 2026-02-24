package com.ravn.ecommerce.presentation.graphql.exceptions;

import com.ravn.ecommerce.domain.exceptions.*;
import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GraphqlExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {

        // ── NOT FOUND ──────────────────────────────────────────────────────────
        switch (ex) {
            case EntityNotFoundException e -> {
                log.warn("[GraphQL] Not found: {}", e.getMessage());
                return buildError(e.getMessage(), e.getErrorCode(), ErrorType.NOT_FOUND, env);
            }


            // ── BAD REQUEST / BUSINESS RULES ───────────────────────────────────────
            case InsufficientStockException e -> {
                log.warn("[GraphQL] Insufficient stock: {}", e.getMessage());
                return buildError(e.getMessage(), e.getErrorCode(), ErrorType.BAD_REQUEST, env);
            }
            case InvalidOperationException e -> {
                log.warn("[GraphQL] Invalid operation: {}", e.getMessage());
                return buildError(e.getMessage(), e.getErrorCode(), ErrorType.BAD_REQUEST, env);
            }
            case InvalidCartLogicException e -> {
                log.warn("[GraphQL] Invalid cart logic: {}", e.getMessage());
                return buildError(e.getMessage(), "INVALID_CART_LOGIC", ErrorType.BAD_REQUEST, env);
            }
            case BusinessRuleViolation e -> {
                log.warn("[GraphQL] Business rule violation: {}", e.getMessage());
                return buildError(e.getMessage(), e.getErrorCode(), ErrorType.BAD_REQUEST, env);
            }
            case DuplicateResourceException e -> {
                log.warn("[GraphQL] Duplicate resource: {}", e.getMessage());
                return buildError(e.getMessage(), "DUPLICATE_RESOURCE", ErrorType.BAD_REQUEST, env);
            }


            // ── UNAUTHORIZED / FORBIDDEN ───────────────────────────────────────────
            case UnauthorizedException e -> {
                log.warn("[GraphQL] Unauthorized: {}", e.getMessage());
                return buildError(e.getMessage(), e.getErrorCode(), ErrorType.UNAUTHORIZED, env);
            }
            case AccessDeniedException e -> {
                log.warn("[GraphQL] Access denied: {}", e.getMessage());
                return buildError("You don't have permission to access this resource",
                        "ACCESS_DENIED", ErrorType.FORBIDDEN, env);
            }


            // ── GENERIC DOMAIN EXCEPTION ───────────────────────────────────────────
            case DomainException e -> {
                log.warn("[GraphQL] Domain exception: {}", e.getMessage());
                return buildError(e.getMessage(), e.getErrorCode(), ErrorType.BAD_REQUEST, env);
            }
            default -> {
            }
        }

        // ── FALLBACK (unexpected errors — don't leak internals) ────────────────
        log.error("[GraphQL] Unexpected error in field '{}': ", env.getField().getName(), ex);
        return buildError("An unexpected error occurred. Please try again later.",
                "INTERNAL_SERVER_ERROR", ErrorType.INTERNAL_ERROR, env);
    }

    private GraphQLError buildError(String message,
            String errorCode,
            ErrorClassification classification,
            DataFetchingEnvironment env) {
        return GraphqlErrorBuilder.newError(env)
                .message(message)
                .errorType(classification)
                .extensions(java.util.Map.of("errorCode", errorCode))
                .build();
    }
}
