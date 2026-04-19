package com.izanami.management_shipping.client;

import com.izanami.management_shipping.dto.ViaCepResponse;
import com.izanami.management_shipping.exception.CepNotFoundException;
import com.izanami.management_shipping.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for communication with the external ViaCEP API.
 *
 * <p>Responsible for making REST calls to the ViaCEP service
 * ({@code https://viacep.com.br/ws/{cep}/json/}) and mapping the response
 * to the internal DTO {@link ViaCepResponse}.</p>
 *
 * <h3>Error handling:</h3>
 * <ul>
 *   <li><b>CEP not found:</b> ViaCEP returns {@code {"erro": true}} → {@link CepNotFoundException}</li>
 *   <li><b>Timeout/connection:</b> {@link ResourceAccessException} → {@link ExternalApiException}</li>
 *   <li><b>Other HTTP errors:</b> {@link RestClientException} → {@link ExternalApiException}</li>
 * </ul>
 *
 * <h3>Generated logs (external call traceability):</h3>
 * <ul>
 *   <li>INFO: URL being called (request start)</li>
 *   <li>INFO: response received successfully (response time)</li>
 *   <li>WARN: CEP returned error (not found in ViaCEP)</li>
 *   <li>ERROR: timeout or connection failure (with stack trace)</li>
 *   <li>ERROR: generic REST call error (with stack trace)</li>
 * </ul>
 *
 * <h3>Configuration:</h3>
 * <p>The base URL and timeout are configurable via {@code application.yaml}:</p>
 * <pre>
 *   viacep:
 *     base-url: https://viacep.com.br/ws
 *     timeout: 5000
 * </pre>
 *
 * @see ViaCepResponse
 */
@Slf4j
@Component
public class ViaCepClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ViaCepClient(RestTemplate restTemplate,
                        @Value("${viacep.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Queries the ViaCEP API to retrieve address data from a CEP.
     *
     * <p>The call has a configurable timeout. On failure, specific exceptions
     * are thrown for handling by the upper layer.</p>
     *
     * @param cep already sanitized CEP (8 numeric digits, no formatting)
     * @return {@link ViaCepResponse} with data returned by ViaCEP
     * @throws CepNotFoundException if the CEP does not exist (ViaCEP returns error)
     * @throws ExternalApiException if there is a timeout, connection failure, or HTTP error
     */
    public ViaCepResponse fetchAddress(String cep) {
        String url = baseUrl + "/" + cep + "/json/";
        log.info("[VIACEP_CLIENT] Iniciando chamada externa - url={}, cep={}", url, cep);

        long startTime = System.currentTimeMillis();

        try {
            ViaCepResponse response = restTemplate.getForObject(url, ViaCepResponse.class);
            long elapsedTime = System.currentTimeMillis() - startTime;

            if (response == null || Boolean.TRUE.equals(response.getErro())) {
                log.warn("[VIACEP_CLIENT] CEP não encontrado no ViaCEP - cep={}, tempoResposta={}ms", cep, elapsedTime);
                throw new CepNotFoundException(cep);
            }

            log.info("[VIACEP_CLIENT] Resposta recebida com sucesso - cep={}, cidade={}, estado={}, tempoResposta={}ms",
                    cep, response.getLocalidade(), response.getUf(), elapsedTime);
            return response;

        } catch (ResourceAccessException ex) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.error("[VIACEP_CLIENT] Timeout ou erro de conexão - cep={}, tempoDecorrido={}ms, erro={}",
                    cep, elapsedTime, ex.getMessage(), ex);
            throw new ExternalApiException("ViaCEP service is unavailable. Please try again later.", ex);
        } catch (RestClientException ex) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.error("[VIACEP_CLIENT] Erro na chamada REST - cep={}, tempoDecorrido={}ms, erro={}",
                    cep, elapsedTime, ex.getMessage(), ex);
            throw new ExternalApiException("Failed to fetch address from ViaCEP.", ex);
        }
    }
}
