package com.proyecto.servicios.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Cliente Feign para el servicio de catálogo de productos de GestoPago.
 * Consume los endpoints del portal {@code gestopago.portalventas.net}.
 *
 * <p>La URL base se inyecta desde {@code application.properties}
 * mediante la propiedad {@code gestopago.auth.url}.</p>
 */
@FeignClient(name = "gestoPagoCatProduct", url = "${gestopago.auth.url}")
public interface GestoPagoCatProduct {

    /**
     * Verifica la conectividad con el servicio de GestoPago (echo de prueba).
     *
     * @param token Bearer token en formato {@code "Bearer <token>"}
     * @return respuesta XML de eco del servidor
     */
    @GetMapping("/sistema/service/sendEcho.do")
    String sendEcho(@RequestHeader("Authorization") String token);

    /**
     * Obtiene el catálogo completo de productos disponibles.
     * La respuesta es un XML que debe deserializarse con JAXB.
     *
     * @param token Bearer token en formato {@code "Bearer <token>"}
     * @return respuesta XML con el listado de productos
     */
    @GetMapping("/sistema/service/getProductList.do")
    String getProductList(@RequestHeader("Authorization") String token);
}
