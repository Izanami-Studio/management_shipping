package com.izanami.management_shipping.exception;

import com.izanami.management_shipping.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

/**
 * Centralized exception handler for the entire application.
 *
 * <p>Intercepts exceptions thrown by controllers and services, returning
 * standardized HTTP responses in {@link ErrorResponse} format.</p>
 *
 * <h3>Exception → HTTP status mapping:</h3>
 * <table>
 *   <tr><th>Exception</th><th>HTTP Status</th><th>Log Level</th></tr>
 *   <tr><td>{@link InvalidCepException}</td><td>400 Bad Request</td><td>WARN</td></tr>
 *   <tr><td>{@link IllegalArgumentException}</td><td>400 Bad Request</td><td>WARN</td></tr>
 *   <tr><td>{@link CepNotFoundException}</td><td>404 Not Found</td><td>WARN</td></tr>
 *   <tr><td>{@link ExternalApiException}</td><td>503 Service Unavailable</td><td>ERROR</td></tr>
 *   <tr><td>{@link Exception} (generic)</td><td>500 Internal Server Error</td><td>ERROR</td></tr>
 * </table>
 *
 * <h3>Traceability:</h3>
 * <p>Each handled exception generates a log at the appropriate level, allowing
 * correlation between server errors and client responses
 * (via the {@code timestamp} field in {@link ErrorResponse}).</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CepNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCepNotFound(CepNotFoundException ex) {
        log.warn("[EXCEPTION_HANDLER] CEP não encontrado - mensagem={}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidCepException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCep(InvalidCepException ex) {
        log.warn("[EXCEPTION_HANDLER] CEP inválido - mensagem={}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApi(ExternalApiException ex) {
        log.error("[EXCEPTION_HANDLER] Falha em API externa - mensagem={}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("[EXCEPTION_HANDLER] Erro de validação - mensagem={}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("[EXCEPTION_HANDLER] Parâmetro ausente - parametro={}", ex.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST, "Missing required parameter: " + ex.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("[EXCEPTION_HANDLER] Tipo inválido - parametro={}, valorRecebido={}",
                ex.getName(), ex.getValue());
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid value for parameter: " + ex.getName());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("[EXCEPTION_HANDLER] Erro inesperado - tipo={}, mensagem={}",
                ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        ErrorResponse error = ErrorResponse.builder()
                .status(status.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
