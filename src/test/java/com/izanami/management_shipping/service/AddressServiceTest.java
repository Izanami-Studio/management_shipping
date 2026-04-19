package com.izanami.management_shipping.service;

import com.izanami.management_shipping.client.ViaCepClient;
import com.izanami.management_shipping.dto.AddressResponse;
import com.izanami.management_shipping.dto.ViaCepResponse;
import com.izanami.management_shipping.exception.InvalidCepException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private ViaCepClient viaCepClient;

    @InjectMocks
    private AddressService addressService;

    @Test
    @DisplayName("Should return address response for valid CEP")
    void shouldReturnAddressForValidCep() {
        ViaCepResponse viaCepResponse = ViaCepResponse.builder()
                .cep("01001-000")
                .logradouro("Praça da Sé")
                .localidade("São Paulo")
                .uf("SP")
                .build();

        when(viaCepClient.fetchAddress("01001000")).thenReturn(viaCepResponse);

        AddressResponse response = addressService.lookupAddress("01001-000");

        assertThat(response.getStreet()).isEqualTo("Praça da Sé");
        assertThat(response.getCity()).isEqualTo("São Paulo");
        assertThat(response.getState()).isEqualTo("SP");
        assertThat(response.getCep()).isEqualTo("01001-000");
    }

    @Test
    @DisplayName("Should throw InvalidCepException for short CEP")
    void shouldThrowForInvalidCep() {
        assertThatThrownBy(() -> addressService.lookupAddress("1234"))
                .isInstanceOf(InvalidCepException.class);
    }

    @Test
    @DisplayName("Should sanitize CEP with hyphen before lookup")
    void shouldSanitizeCepWithHyphen() {
        ViaCepResponse viaCepResponse = ViaCepResponse.builder()
                .cep("20040-020")
                .logradouro("Rua da Assembleia")
                .localidade("Rio de Janeiro")
                .uf("RJ")
                .build();

        when(viaCepClient.fetchAddress("20040020")).thenReturn(viaCepResponse);

        AddressResponse response = addressService.lookupAddress("20040-020");

        assertThat(response.getState()).isEqualTo("RJ");
    }
}
