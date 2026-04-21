package com.izanami.management_shipping.service;

import com.izanami.management_shipping.client.ViaCepClient;
import com.izanami.management_shipping.dto.ViaCepResponse;
import com.izanami.management_shipping.exception.InvalidCepException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
class AddressServiceCacheTest {

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
    @DisplayName("Invalid CEP should never be cached and should not call ViaCEP")
    void invalidCepShouldNotBeCached() {
        assertThatThrownBy(() -> addressService.lookupAddress("1234"))
                .isInstanceOf(InvalidCepException.class);
        assertThatThrownBy(() -> addressService.lookupAddress("1234"))
                .isInstanceOf(InvalidCepException.class);

        verifyNoInteractions(viaCepClient);
        assertThat(Objects.requireNonNull(cacheManager.getCache("address"))
                .get("1234")).isNull();
    }


    @Test
    @DisplayName("Second lookup for the same CEP should be served from cache (ViaCEP called once)")
    void shouldHitCacheOnSecondLookup() {
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
    }

    @Test
    @DisplayName("Formatted and sanitized CEP should share the same cache entry")
    void shouldShareCacheEntryAcrossFormats() {
        ViaCepResponse stub = ViaCepResponse.builder()
                .cep("01001-000")
                .logradouro("Praça da Sé")
                .localidade("São Paulo")
                .uf("SP")
                .build();
        when(viaCepClient.fetchAddress("01001000")).thenReturn(stub);

        addressService.lookupAddress("01001-000");
        addressService.lookupAddress("01001000");

        verify(viaCepClient, times(1)).fetchAddress("01001000");
    }

    @Test
    @DisplayName("Different CEPs should each hit ViaCEP once")
    void differentCepsShouldNotShareCache() {
        when(viaCepClient.fetchAddress("01001000"))
                .thenReturn(ViaCepResponse.builder().cep("01001-000").uf("SP").build());
        when(viaCepClient.fetchAddress("20040020"))
                .thenReturn(ViaCepResponse.builder().cep("20040-020").uf("RJ").build());

        addressService.lookupAddress("01001-000");
        addressService.lookupAddress("20040-020");
        addressService.lookupAddress("01001000");
        addressService.lookupAddress("20040020");

        verify(viaCepClient, times(1)).fetchAddress("01001000");
        verify(viaCepClient, times(1)).fetchAddress("20040020");
    }
}
