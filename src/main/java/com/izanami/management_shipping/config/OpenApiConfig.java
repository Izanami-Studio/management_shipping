package com.izanami.management_shipping.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for API documentation.
 *
 * <p>Generates interactive documentation accessible at:</p>
 * <ul>
 *   <li>Swagger UI: {@code http://localhost:8080/swagger-ui.html}</li>
 *   <li>API Docs (JSON): {@code http://localhost:8080/api-docs}</li>
 * </ul>
 *
 * <p>The documentation includes all endpoints, DTOs, request/response examples,
 * and possible error codes, facilitating API integration by consumers.</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI shippingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shipping Service API")
                        .description("API for shipping cost calculation and address lookup via CEP. "
                                + "This service does not manage carts — it receives data externally and calculates the shipping cost.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Izanami Team")
                                .email("dev@izanami.com")));
    }
}
