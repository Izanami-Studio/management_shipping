package com.izanami.management_shipping.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Configurable properties for shipping cost calculation.
 *
 * <p>Mapped from the {@code shipping} prefix in {@code application.yaml}.
 * Allows changing business rules without modifying code.</p>
 *
 * <h3>Configuration in application.yaml:</h3>
 * <pre>
 * shipping:
 *   origin-state: SP                # Origin state for comparison
 *   same-state-cost: 10.00          # Cost when destination = origin
 *   different-state-cost: 20.00     # Cost when destination ≠ origin
 *   free-shipping-threshold: 200.00 # Minimum subtotal for free shipping
 * </pre>
 *
 * <h3>Usage in calculation:</h3>
 * <ul>
 *   <li>{@code originState} → compared with destination CEP state</li>
 *   <li>{@code freeShippingThreshold} → checked BEFORE state comparison</li>
 *   <li>{@code sameStateCost} / {@code differentStateCost} → applied when free shipping does not apply</li>
 * </ul>
 *
 * @see com.izanami.management_shipping.service.ShippingService
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "shipping")
public class ShippingProperties {

    /** Origin state for comparison (e.g., "SP") */
    private String originState;

    /** Shipping cost when destination is in the same state (e.g., 10.00) */
    private BigDecimal sameStateCost;

    /** Shipping cost when destination is in a different state (e.g., 20.00) */
    private BigDecimal differentStateCost;

    /** Minimum subtotal for free shipping eligibility (e.g., 200.00) */
    private BigDecimal freeShippingThreshold;
}
