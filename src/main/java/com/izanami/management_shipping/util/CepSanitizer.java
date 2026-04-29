package com.izanami.management_shipping.util;

public final class CepSanitizer {
    private CepSanitizer() {}
    public static String sanitize(String cep) {
        return cep == null ? "" : cep.replaceAll("[^0-9]", "");
    }
}
