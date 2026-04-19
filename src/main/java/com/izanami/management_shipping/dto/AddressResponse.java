package com.izanami.management_shipping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for address lookup.
 *
 * <p>Contains the address data resolved from a Brazilian CEP,
 * already mapped from the ViaCEP format to the internal application format.</p>
 *
 * <h3>Field mapping:</h3>
 * <ul>
 *   <li>{@code street} ← ViaCEP {@code logradouro}</li>
 *   <li>{@code city} ← ViaCEP {@code localidade}</li>
 *   <li>{@code state} ← ViaCEP {@code uf}</li>
 *   <li>{@code cep} ← ViaCEP {@code cep} (formatted with hyphen)</li>
 * </ul>
 *
 * <h3>Usage in shipping calculation:</h3>
 * <p>The {@code state} field is used by {@link com.izanami.management_shipping.service.ShippingService}
 * to determine whether the destination is in the same state as the origin.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Address information resolved from CEP")
public class AddressResponse {

    @Schema(description = "Street name", example = "Avenida Paulista")
    private String street;

    @Schema(description = "City name", example = "São Paulo")
    private String city;

    @Schema(description = "State abbreviation (used for shipping calculation)", example = "SP")
    private String state;

    @Schema(description = "Formatted CEP returned by ViaCEP", example = "01001-000")
    private String cep;
}
