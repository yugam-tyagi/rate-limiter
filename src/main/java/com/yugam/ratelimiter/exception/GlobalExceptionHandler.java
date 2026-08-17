package com.yugam.ratelimiter.exception;

import com.yugam.ratelimiter.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpHeaders;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleClientNotFoundException(ClientNotFoundException ex, HttpServletRequest request){
        log.warn("Client not found for request: {}",request.getRequestURI());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(),request);
    }

    @ExceptionHandler(StrategyNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleStrategyNotFoundException(StrategyNotFoundException ex, HttpServletRequest request){
        log.warn("Strategy not found for request: {}",request.getRequestURI());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitExceededException(RateLimitExceededException ex, HttpServletRequest request){
        ApiErrorResponse response = new ApiErrorResponse(Instant.now(),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());

        log.warn("Rate limit exceeded for request: {}. Retry after {} seconds",request.getRequestURI(),ex.getRetryAfterSeconds());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER,String.valueOf(ex.getRetryAfterSeconds()))
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request){
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid request");

        log.warn("Validation failed for request: {}", request.getRequestURI());
        return buildErrorResponse(HttpStatus.BAD_REQUEST,message,request);
    }

    @ExceptionHandler(InvalidPolicyException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPolicyException(InvalidPolicyException ex, HttpServletRequest request) {
        log.warn("Invalid policy for request: {}", request.getRequestURI());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request){
        log.warn("Unexpected error occurred while processing request: {}",request.getRequestURI(),ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,"An unexpected error occurred.",request);
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(HttpStatus status, String message, HttpServletRequest request){
        ApiErrorResponse response = new ApiErrorResponse(Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());

        return ResponseEntity.status(status).body(response);
    }
}
