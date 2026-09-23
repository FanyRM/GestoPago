package com.proyecto.servicios.controller;

import com.proyecto.servicios.exception.CatalogoException;
import com.proyecto.servicios.model.gestopago.catalogo.CatalogoProductoResponse;
import com.proyecto.servicios.service.CatalogoProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la consulta del catálogo de productos de GestoPago.
 *
 * <p>Expone un endpoint de solo lectura que sirve la información almacenada
 * en la caché de MongoDB, sin realizar llamadas al servicio externo.</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/gestopago")
public class CatalogoProductoController {

    private final CatalogoProductoService catalocoProductoService;

    /**
     * Recupera el catálogo de productos desde la caché de MongoDB.
     *
     * <p>Retorna el catálogo completo en formato JSON con los productos
     * almacenados en la última sincronización diaria.</p>
     *
     * @return {@code 200 OK} con el catálogo de productos,
     *         {@code 404 Not Found} si la caché está vacía,
     *         {@code 500 Internal Server Error} si falla la consulta a MongoDB
     */
    @GetMapping(value = "/catalogo/productos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CatalogoProductoResponse> obtenerCatalogo() {
        log.info("[CatalogoProductoController] Solicitud recibida: GET /gestopago/catalogo/productos");
        try {
            CatalogoProductoResponse respuesta = catalocoProductoService.obtenerCatalogo();
            log.info("[CatalogoProductoController] Respuesta exitosa. TotalProductos={}", respuesta.getTotalProductos());
            return ResponseEntity.ok(respuesta);

        } catch (CatalogoException ex) {
            log.warn("[CatalogoProductoController] Error controlado: [{}] {}",
                    ex.getResponseEnum().name(), ex.getMessage());

            CatalogoProductoResponse errorResponse = CatalogoProductoResponse.builder()
                    .codigo(ex.getResponseEnum().getCodigo())
                    .mensaje(ex.getResponseEnum().getMensaje())
                    .build();

            return ResponseEntity
                    .status(ex.getResponseEnum().getHttpStatus())
                    .body(errorResponse);
        }
    }
}
