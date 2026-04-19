package com.izanami.management_shipping.exception;

/**
 * Exception thrown when there is a failure communicating with external APIs.
 *
 * <p>Encapsulates timeout errors, connection refused, or HTTP failures
 * during calls to ViaCEP or other external services.</p>
 *
 * <h3>Common scenarios:</h3>
 * <ul>
 *   <li>Connection timeout (configurable via {@code viacep.timeout})</li>
 *   <li>Read timeout</li>
 *   <li>External service unavailable (HTTP 5xx)</li>
 *   <li>DNS resolution failure</li>
 * </ul>
 *
 * <h3>Traceability:</h3>
 * <p>When thrown, generates an ERROR log in {@link GlobalExceptionHandler}
 * (with full stack trace) and returns HTTP 503 to the client.</p>
 *
 * @see GlobalExceptionHandler#handleExternalApi(ExternalApiException)
 * @see com.izanami.management_shipping.client.ViaCepClient
 */
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message) {
        super(message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
