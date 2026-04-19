package com.izanami.management_shipping.controller;

import com.izanami.management_shipping.dto.ErrorResponse;
import com.izanami.management_shipping.dto.ShippingResponse;
import com.izanami.management_shipping.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Controller responsible for shipping cost calculation.
 *
 * <p>Receives cart data (subtotal) and the destination CEP to calculate
 * the shipping cost. This service does NOT manage carts — it only calculates
 * shipping based on externally provided data.</p>
 *
 * <h3>Business rules:</h3>
 * <ul>
 *   <li>Subtotal ≥ R$200.00 → free shipping</li>
 *   <li>Same state as origin (configurable) → R$10.00</li>
 *   <li>Different state from origin → R$20.00</li>
 * </ul>
 *
 * <h3>Traceability flow:</h3>
 * <ol>
 *   <li>Request received with parameters → INFO log (includes cartId if provided)</li>
 *   <li>Subtotal validation → WARN log if invalid</li>
 *   <li>CEP resolution via AddressService → internal logs</li>
 *   <li>Calculation decision (free/same/different) → INFO log</li>
 *   <li>Response returned → INFO log with final cost and processing time</li>
 * </ol>
 *
 * @see ShippingService
 */
@Slf4j
@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
@Tag(name = "Shipping", description = "Shipping cost calculation")
public class ShippingController {

    private final ShippingService shippingService;

    /**
     * Calculates shipping cost based on cart subtotal and destination CEP.
     *
     * <p>The {@code cartId} parameter is optional and is used only for traceability
     * in logs, making it easier to correlate with the cart service.</p>
     *
     * @param subtotal cart total value (must be ≥ 0)
     * @param cep      destination CEP (8 digits, with or without formatting)
     * @param cartId   cart identifier (optional, for traceability only)
     * @return {@link ShippingResponse} containing shipping cost, destination state, and free shipping flag
     */
    @GetMapping
    @Operation(summary = "Calculate shipping cost",
            description = "Calculates shipping based on cart subtotal and destination CEP. " +
                    "Rules: same state = R$10, different state = R$20, subtotal >= R$200 = free shipping.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shipping calculated successfully",
                    content = @Content(schema = @Schema(implementation = ShippingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "CEP not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "ViaCEP service unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ShippingResponse> calculateShipping(
            @Parameter(description = "Cart subtotal (must be >= 0)", example = "150.00", required = true)
            @RequestParam BigDecimal subtotal,

            @Parameter(description = "Destination CEP (8 digits)", example = "20040020", required = true)
            @RequestParam String cep,

            @Parameter(description = "Cart ID for traceability (optional)", example = "cart-123")
            @RequestParam(required = false) String cartId) {

        log.info("[SHIPPING_CALC] Requisição recebida - cartId={}, subtotal={}, cep={}",
                cartId != null ? cartId : "N/A", subtotal, cep);

        long startTime = System.currentTimeMillis();
        ShippingResponse response = shippingService.calculateShipping(subtotal, cep, cartId);
        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("[SHIPPING_CALC] Resposta retornada em {}ms - cartId={}, custo={}, estado={}, freteGratis={}",
                elapsedTime, cartId != null ? cartId : "N/A",
                response.getShippingCost(), response.getDestinationState(), response.isFreeShipping());

        return ResponseEntity.ok(response);
    }
}
