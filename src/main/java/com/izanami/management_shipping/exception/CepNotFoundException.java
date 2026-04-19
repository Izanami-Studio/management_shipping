package com.izanami.management_shipping.exception;

/**
 * Exception thrown when a CEP is not found in the ViaCEP API.
 *
 * <p>Indicates that the provided CEP has a valid format (8 digits),
 * but does not match any address registered with the Brazilian postal service.</p>
 *
 * <h3>Traceability:</h3>
 * <p>When thrown, generates a WARN log in {@link GlobalExceptionHandler}
 * and returns HTTP 404 to the client.</p>
 *
 * @see GlobalExceptionHandler#handleCepNotFound(CepNotFoundException)
 */
public class CepNotFoundException extends RuntimeException {

    public CepNotFoundException(String cep) {
        super("CEP not found: " + cep);
    }
}
