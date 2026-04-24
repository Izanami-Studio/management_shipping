package com.izanami.management_shipping.service;

import com.izanami.management_shipping.client.ViaCepClient;
import com.izanami.management_shipping.dto.ViaCepResponse;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import java.time.Duration;
import java.util.Objects;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "cache.ttl=300ms",
        "cache.size=1000"
})
class AddressServiceCacheExpirationTest {

    @Autowired
    private AddressService addressService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private ViaCepClient viaCepClient;

    @BeforeEach
    void clearCache() {
        Objects.requireNonNull(cacheManager.getCache("address")).clear();
        reset(viaCepClient);
    }

    @Test
    @DisplayName("After TTL expires, the next lookup should hit ViaCEP again")
    void shouldExpireCacheEntryAfterTtl() {
        ViaCepResponse stub = ViaCepResponse.builder()
                .cep("01001-000")
                .logradouro("Praça da Sé")
                .localidade("São Paulo")
                .uf("SP")
                .build();
        when(viaCepClient.fetchAddress("01001000")).thenReturn(stub);

        addressService.lookupAddress("01001-000");
        addressService.lookupAddress("01001-000");
        verify(viaCepClient, times(1)).fetchAddress("01001000");

        Awaitility.await()
                .pollDelay(Duration.ofMillis(500))
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    addressService.lookupAddress("01001-000");
                    verify(viaCepClient, times(2)).fetchAddress("01001000");
                });
    }
}
