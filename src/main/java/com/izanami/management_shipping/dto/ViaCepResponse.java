package com.izanami.management_shipping.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO that maps the response from the external ViaCEP API.
 *
 * <p>Represents the JSON returned by {@code GET https://viacep.com.br/ws/{cep}/json/}.
 * Unknown fields are ignored to ensure forward compatibility if
 * the API adds new fields.</p>
 *
 * <h3>Main fields used by the system:</h3>
 * <ul>
 *   <li>{@code logradouro} → mapped to "street" in the final response</li>
 *   <li>{@code localidade} → mapped to "city" in the final response</li>
 *   <li>{@code uf} → mapped to "state" in the final response (used in shipping calculation)</li>
 *   <li>{@code erro} → indicates if the CEP was not found ({@code true})</li>
 * </ul>
 *
 * <h3>ViaCEP response example:</h3>
 * <pre>
 * {
 *   "cep": "01001-000",
 *   "logradouro": "Praça da Sé",
 *   "complemento": "lado ímpar",
 *   "bairro": "Sé",
 *   "localidade": "São Paulo",
 *   "uf": "SP"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ViaCepResponse {

    /** Formatted CEP (e.g., "01001-000") */
    private String cep;

    /** Street name (e.g., "Praça da Sé") */
    private String logradouro;

    /** Address complement (e.g., "lado ímpar") */
    private String complemento;

    /** Neighborhood (e.g., "Sé") */
    private String bairro;

    /** City name (e.g., "São Paulo") — mapped to "city" in the response */
    private String localidade;

    /** State abbreviation (e.g., "SP") — mapped to "state" and used in shipping calculation */
    private String uf;

    /** Error flag: {@code true} if the CEP was not found in ViaCEP */
    private Boolean erro;
}
