package com.izanami.management_shipping.service;

import com.izanami.management_shipping.config.ShippingProperties;
import com.izanami.management_shipping.dto.AddressResponse;
import com.izanami.management_shipping.dto.ShippingResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

    @Mock
    private AddressService addressService;

    @Mock
    private ShippingProperties shippingProperties;

    @InjectMocks
    private ShippingService shippingService;

    @Test
    @DisplayName("Should calculate R$10 shipping for same state delivery")
    void shouldCalculateSameStateShipping() {
        when(shippingProperties.getFreeShippingThreshold()).thenReturn(new BigDecimal("200.00"));
        when(shippingProperties.getOriginState()).thenReturn("SP");
        when(shippingProperties.getSameStateCost()).thenReturn(new BigDecimal("10.00"));

        AddressResponse address = AddressResponse.builder()
                .street("Avenida Paulista")
                .city("São Paulo")
                .state("SP")
                .cep("01001000")
                .build();

        when(addressService.lookupAddress("01001000")).thenReturn(address);

        ShippingResponse response = shippingService.calculateShipping(
                new BigDecimal("100.00"), "01001000", "cart-1");

        assertThat(response.getShippingCost()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(response.getDestinationState()).isEqualTo("SP");
        assertThat(response.isFreeShipping()).isFalse();
    }

    @Test
    @DisplayName("Should calculate R$20 shipping for different state delivery")
    void shouldCalculateDifferentStateShipping() {
        when(shippingProperties.getFreeShippingThreshold()).thenReturn(new BigDecimal("200.00"));
        when(shippingProperties.getOriginState()).thenReturn("SP");
        when(shippingProperties.getDifferentStateCost()).thenReturn(new BigDecimal("20.00"));

        AddressResponse address = AddressResponse.builder()
                .street("Rua da Assembleia")
                .city("Rio de Janeiro")
                .state("RJ")
                .cep("20040020")
                .build();

        when(addressService.lookupAddress("20040020")).thenReturn(address);

        ShippingResponse response = shippingService.calculateShipping(
                new BigDecimal("100.00"), "20040020", "cart-2");

        assertThat(response.getShippingCost()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(response.getDestinationState()).isEqualTo("RJ");
        assertThat(response.isFreeShipping()).isFalse();
    }

    @Test
    @DisplayName("Should apply free shipping when subtotal >= R$200")
    void shouldApplyFreeShipping() {
        when(shippingProperties.getFreeShippingThreshold()).thenReturn(new BigDecimal("200.00"));

        AddressResponse address = AddressResponse.builder()
                .street("Rua da Assembleia")
                .city("Rio de Janeiro")
                .state("RJ")
                .cep("20040020")
                .build();

        when(addressService.lookupAddress("20040020")).thenReturn(address);

        ShippingResponse response = shippingService.calculateShipping(
                new BigDecimal("250.00"), "20040020", null);

        assertThat(response.getShippingCost()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getDestinationState()).isEqualTo("RJ");
        assertThat(response.isFreeShipping()).isTrue();
    }

    @Test
    @DisplayName("Should apply free shipping when subtotal equals exactly R$200")
    void shouldApplyFreeShippingAtExactThreshold() {
        when(shippingProperties.getFreeShippingThreshold()).thenReturn(new BigDecimal("200.00"));

        AddressResponse address = AddressResponse.builder()
                .street("Avenida Paulista")
                .city("São Paulo")
                .state("SP")
                .cep("01001000")
                .build();

        when(addressService.lookupAddress("01001000")).thenReturn(address);

        ShippingResponse response = shippingService.calculateShipping(
                new BigDecimal("200.00"), "01001000", "cart-3");

        assertThat(response.getShippingCost()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.isFreeShipping()).isTrue();
    }

    @Test
    @DisplayName("Should reject negative subtotal")
    void shouldRejectNegativeSubtotal() {
        assertThatThrownBy(() ->
                shippingService.calculateShipping(new BigDecimal("-10.00"), "01001000", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Subtotal must be greater than or equal to zero");
    }
}
