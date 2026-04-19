package com.izanami.management_shipping.exception;

/**
 * Exception thrown when the CEP has an invalid format.
 *
 * <p>The CEP must contain exactly 8 numeric digits after sanitization
 * (removal of hyphens and special characters). Otherwise, this exception is thrown.</p>
 *
 * <h3>Invalid CEP examples:</h3>
 * <ul>
 *   <li>"123" → only 3 digits</li>
 *   <li>"" → empty</li>
 *   <li>"ABCDEFGH" → no numeric digits</li>
 * </ul>
 *
 * <h3>Traceability:</h3>
 * <p>When thrown, generates a WARN log in {@link GlobalExceptionHandler}
 * and returns HTTP 400 to the client.</p>
 *
 * @see GlobalExceptionHandler#handleInvalidCep(InvalidCepException)
 */
public class InvalidCepException extends RuntimeException {

    public InvalidCepException(String cep) {
        super("Invalid CEP format: " + cep + ". Expected 8 digits.");
    }
}
