package com.izanami.management_shipping.controller;

import com.izanami.management_shipping.dto.AddressResponse;
import com.izanami.management_shipping.dto.ErrorResponse;
import com.izanami.management_shipping.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for address lookup via CEP (Brazilian postal code).
 *
 * <p>Uses the external ViaCEP service to resolve a Brazilian CEP
 * into structured address data (street, city, state).</p>
 *
 * <h3>Traceability flow:</h3>
 * <ol>
 *   <li>Request received with CEP → INFO log</li>
 *   <li>CEP sanitized and validated → WARN log if invalid</li>
 *   <li>ViaCEP API call → INFO log (start and end)</li>
 *   <li>Response returned to client → INFO log with result</li>
 * </ol>
 *
 * @see AddressService
 */
@Slf4j
@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
@Tag(name = "Address", description = "Address lookup via CEP")
public class AddressController {

    private final AddressService addressService;

    /**
     * Looks up an address from a Brazilian CEP.
     *
     * <p>The CEP can be provided with or without a hyphen (e.g., "01001000" or "01001-000").
     * Non-numeric characters are stripped before validation.</p>
     *
     * @param cep Brazilian CEP (8 numeric digits, with or without formatting)
     * @return {@link AddressResponse} containing street, city, state, and formatted CEP
     */
    @GetMapping("/{cep}")
    @Operation(summary = "Lookup address by CEP", description = "Resolves a Brazilian CEP into street, city and state using ViaCEP API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Address found",
                    content = @Content(schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid CEP format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "CEP not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "ViaCEP service unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AddressResponse> lookupAddress(@PathVariable String cep) {
        log.info("[ADDRESS_LOOKUP] Requisição recebida - cep={}", cep);

        long startTime = System.currentTimeMillis();
        AddressResponse response = addressService.lookupAddress(cep);
        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("[ADDRESS_LOOKUP] Resposta retornada em {}ms - cep={}, cidade={}, estado={}",
                elapsedTime, cep, response.getCity(), response.getState());

        return ResponseEntity.ok(response);
    }
}
