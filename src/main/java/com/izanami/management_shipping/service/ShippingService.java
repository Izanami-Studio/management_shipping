package com.izanami.management_shipping.service;

import com.izanami.management_shipping.config.ShippingProperties;
import com.izanami.management_shipping.dto.AddressResponse;
import com.izanami.management_shipping.dto.ShippingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service responsible for shipping cost calculation.
 *
 * <p>Implements the business rules to determine shipping cost based on
 * the cart subtotal and the destination state resolved via CEP.</p>
 *
 * <h3>Calculation rules (in priority order):</h3>
 * <ol>
 *   <li><b>Free shipping:</b> subtotal ≥ configured threshold (default R$200.00)</li>
 *   <li><b>Same state:</b> fixed configured cost (default R$10.00)</li>
 *   <li><b>Different state:</b> fixed configured cost (default R$20.00)</li>
 * </ol>
 *
 * <h3>Internal flow:</h3>
 * <ol>
 *   <li>Subtotal validation (non-null, ≥ 0)</li>
 *   <li>Address resolution via {@link AddressService}</li>
 *   <li>Destination state comparison with configured origin state</li>
 *   <li>Application of the corresponding business rule</li>
 * </ol>
 *
 * <h3>Generated logs (traceability):</h3>
 * <ul>
 *   <li>INFO: calculation start with all received parameters</li>
 *   <li>INFO: decision taken (free shipping / same state / different state)</li>
 *   <li>WARN: invalid subtotal</li>
 * </ul>
 *
 * <h3>Configuration:</h3>
 * <p>Cost values and threshold are configurable via {@code application.yaml}
 * under the {@code shipping.*} prefix. See {@link ShippingProperties}.</p>
 *
 * @see ShippingProperties
 * @see AddressService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService {

    private final AddressService addressService;
    private final ShippingProperties shippingProperties;

    /**
     * Calculates the shipping cost for an order.
     *
     * <p>Decision flow:</p>
     * <pre>
     *   subtotal >= threshold? → free shipping (R$0)
     *   destination state == origin state? → same state cost (R$10)
     *   otherwise → different state cost (R$20)
     * </pre>
     *
     * @param subtotal cart total value (required, ≥ 0)
     * @param cep      destination CEP for state resolution
     * @param cartId   cart identifier for traceability (may be null)
     * @return {@link ShippingResponse} with calculated cost, destination state, and free shipping flag
     * @throws IllegalArgumentException if subtotal is null or negative
     */
    public ShippingResponse calculateShipping(BigDecimal subtotal, String cep, String cartId) {
        validateSubtotal(subtotal);

        log.info("[SHIPPING_SERVICE] Iniciando cálculo - cartId={}, subtotal={}, cep={}, origem={}",
                cartId != null ? cartId : "N/A", subtotal, cep, shippingProperties.getOriginState());

        AddressResponse address = addressService.lookupAddress(cep);
        String destinationState = address.getState();

        // Rule 1: Free shipping when subtotal >= threshold
        if (subtotal.compareTo(shippingProperties.getFreeShippingThreshold()) >= 0) {
            log.info("[SHIPPING_SERVICE] Decisão: FRETE_GRATIS - cartId={}, subtotal={} >= limiar={}, destino={}",
                    cartId != null ? cartId : "N/A", subtotal,
                    shippingProperties.getFreeShippingThreshold(), destinationState);
            return ShippingResponse.builder()
                    .shippingCost(BigDecimal.ZERO)
                    .destinationState(destinationState)
                    .freeShipping(true)
                    .build();
        }

        // Rule 2/3: Cost based on state comparison
        boolean sameState = shippingProperties.getOriginState().equalsIgnoreCase(destinationState);
        BigDecimal cost = sameState
                ? shippingProperties.getSameStateCost()
                : shippingProperties.getDifferentStateCost();

        log.info("[SHIPPING_SERVICE] Decisão: {} - cartId={}, origem={}, destino={}, custo={}",
                sameState ? "MESMO_ESTADO" : "ESTADO_DIFERENTE",
                cartId != null ? cartId : "N/A",
                shippingProperties.getOriginState(), destinationState, cost);

        return ShippingResponse.builder()
                .shippingCost(cost)
                .destinationState(destinationState)
                .freeShipping(false)
                .build();
    }

    /**
     * Validates that the subtotal is non-null and non-negative.
     *
     * @param subtotal value to be validated
     * @throws IllegalArgumentException if subtotal is null or negative
     */
    private void validateSubtotal(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("[SHIPPING_SERVICE] Subtotal inválido recebido - valor={}", subtotal);
            throw new IllegalArgumentException("Subtotal must be greater than or equal to zero");
        }
    }
}
