package com.izanami.management_shipping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for shipping cost calculation.
 *
 * <p>Returns the shipping calculation result containing the cost, destination state,
 * and whether shipping is free.</p>
 *
 * <h3>Field interpretation:</h3>
 * <ul>
 *   <li>{@code shippingCost} → R$0.00 if free shipping, R$10.00 same state, R$20.00 different state</li>
 *   <li>{@code destinationState} → state abbreviation resolved from the destination CEP</li>
 *   <li>{@code freeShipping} → {@code true} when subtotal ≥ configured threshold (R$200.00)</li>
 * </ul>
 *
 * <h3>Traceability usage example:</h3>
 * <p>The consumer can correlate this response with the {@code cartId} sent
 * in the request to link the shipping cost to the corresponding cart.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Shipping calculation result")
public class ShippingResponse {

    @Schema(description = "Calculated shipping cost in BRL", example = "10.00")
    private BigDecimal shippingCost;

    @Schema(description = "Destination state abbreviation resolved from CEP", example = "RJ")
    private String destinationState;

    @Schema(description = "Whether shipping is free (subtotal >= R$200)", example = "false")
    private boolean freeShipping;
}
