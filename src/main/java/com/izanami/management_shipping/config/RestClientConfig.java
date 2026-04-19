package com.izanami.management_shipping.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * HTTP client (RestTemplate) configuration for external API calls.
 *
 * <p>Defines connection and read timeouts to protect the application against
 * slow or unavailable external APIs (e.g., ViaCEP).</p>
 *
 * <h3>Configurable timeouts:</h3>
 * <ul>
 *   <li><b>connectTimeout:</b> maximum time to establish a TCP connection</li>
 *   <li><b>readTimeout:</b> maximum time to receive a response after connection</li>
 * </ul>
 *
 * <h3>Configuration in application.yaml:</h3>
 * <pre>
 * viacep:
 *   timeout: 5000  # milliseconds (applied to both connect and read)
 * </pre>
 *
 * <h3>Traceability:</h3>
 * <p>When the timeout is exceeded, the {@link com.izanami.management_shipping.client.ViaCepClient}
 * catches the exception and logs an ERROR with the elapsed time.</p>
 *
 * @see com.izanami.management_shipping.client.ViaCepClient
 */
@Configuration
public class RestClientConfig {

    @Value("${viacep.timeout:5000}")
    private int timeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(timeout))
                .setReadTimeout(Duration.ofMillis(timeout))
                .build();
    }
}
