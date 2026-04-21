package com.izanami.management_shipping.service;

import com.izanami.management_shipping.client.ViaCepClient;
import com.izanami.management_shipping.dto.AddressResponse;
import com.izanami.management_shipping.dto.ViaCepResponse;
import com.izanami.management_shipping.exception.InvalidCepException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service responsible for resolving addresses from CEP.
 *
 * <p>Orchestrates CEP sanitization, validation, and external ViaCEP service lookup,
 * returning a structured response with the address data.</p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Sanitize the CEP (remove non-numeric characters)</li>
 *   <li>Validate CEP format (exactly 8 digits)</li>
 *   <li>Delegate lookup to {@link ViaCepClient}</li>
 *   <li>Map external response to internal DTO {@link AddressResponse}</li>
 * </ul>
 *
 * <h3>Generated logs:</h3>
 * <ul>
 *   <li>INFO: lookup start with sanitized CEP</li>
 *   <li>INFO: lookup result (city/state found)</li>
 *   <li>WARN: invalid CEP format (via {@link InvalidCepException})</li>
 * </ul>
 *
 * @see ViaCepClient
 * @see AddressResponse
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final ViaCepClient viaCepClient;

    /**
     * Looks up the address corresponding to a Brazilian CEP.
     *
     * <p>The CEP is sanitized (non-numeric characters removed) and validated
     * before querying ViaCEP. If the CEP is invalid or not found,
     * an appropriate exception is thrown.</p>
     *
     * @param cep CEP provided by the client (may contain hyphens or spaces)
     * @return {@link AddressResponse} with street, city, state, and formatted CEP
     * @throws InvalidCepException if the CEP does not have exactly 8 digits after sanitization
     * @throws com.izanami.management_shipping.exception.CepNotFoundException if the CEP does not exist in ViaCEP
     * @throws com.izanami.management_shipping.exception.ExternalApiException if there is a communication failure with ViaCEP
     */
    @Cacheable("address")
    public AddressResponse lookupAddress(String cep) {
        String sanitizedCep = sanitizeCep(cep);
        validateCep(sanitizedCep);

        log.info("[ADDRESS_SERVICE] Iniciando consulta ViaCEP - cepOriginal={}, cepSanitizado={}", cep, sanitizedCep);

        ViaCepResponse viaCepResponse = viaCepClient.fetchAddress(sanitizedCep);

        AddressResponse response = AddressResponse.builder()
                .street(viaCepResponse.getLogradouro())
                .city(viaCepResponse.getLocalidade())
                .state(viaCepResponse.getUf())
                .cep(viaCepResponse.getCep())
                .build();

        log.info("[ADDRESS_SERVICE] Endereço resolvido - cep={}, rua={}, cidade={}, estado={}",
                sanitizedCep, response.getStreet(), response.getCity(), response.getState());

        return response;
    }

    /**
     * Removes non-numeric characters from the CEP.
     *
     * @param cep CEP with possible formatting (e.g., "01001-000")
     * @return CEP containing only digits (e.g., "01001000")
     */
    private String sanitizeCep(String cep) {
        return cep.replaceAll("[^0-9]", "");
    }

    /**
     * Validates that the CEP has exactly 8 numeric digits.
     *
     * @param cep already sanitized CEP
     * @throws InvalidCepException if the CEP is null or does not have 8 characters
     */
    private void validateCep(String cep) {
        if (cep == null || cep.length() != 8) {
            log.warn("[ADDRESS_SERVICE] CEP inválido detectado - cep={}, tamanho={}",
                    cep, cep != null ? cep.length() : 0);
            throw new InvalidCepException(cep);
        }
    }
}
