package com.izanami.management_shipping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API error response DTO.
 *
 * <p>All exceptions handled by {@link com.izanami.management_shipping.exception.GlobalExceptionHandler}
 * return this structure, allowing consumers to interpret errors uniformly.</p>
 *
 * <h3>Traceability fields:</h3>
 * <ul>
 *   <li>{@code status} → HTTP code (400, 404, 503, 500)</li>
 *   <li>{@code message} → human-readable error description for debugging</li>
 *   <li>{@code timestamp} → exact moment of the error for correlation with server logs</li>
 * </ul>
 *
 * <h3>Response example:</h3>
 * <pre>
 * {
 *   "status": 404,
 *   "message": "CEP not found: 00000000",
 *   "timestamp": "2025-01-15T10:30:00"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error message describing what went wrong", example = "Invalid CEP format: 123. Expected 8 digits.")
    private String message;

    @Schema(description = "Timestamp of the error occurrence (use to correlate with server logs)", example = "2025-01-15T10:30:00")
    private LocalDateTime timestamp;
}
