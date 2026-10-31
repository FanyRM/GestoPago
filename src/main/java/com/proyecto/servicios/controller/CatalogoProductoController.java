package com.proyecto.servicios.controller;

import com.proyecto.servicios.exception.CatalogoException;
import com.proyecto.servicios.model.gestopago.catalogo.CatalogoProductoResponse;
import com.proyecto.servicios.service.CatalogoProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gestopago")
public class CatalogoProductoController {

    private final CatalogoProductoService catalocoProductoService;
    @GetMapping(value = "/catalogo/productos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CatalogoProductoResponse> obtenerCatalogo(
            @RequestParam(name = "forzar", defaultValue = "false") boolean forzar
    ) {
        if (forzar) {
            catalocoProductoService.sincronizarCatalogo();
        }
        return procesarRespuesta();
    }
    @PostMapping(value = "/catalogo/sincronizar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CatalogoProductoResponse> sincronizarManualmente() {
        catalocoProductoService.sincronizarCatalogo();
        return procesarRespuesta();
    }

    private ResponseEntity<CatalogoProductoResponse> procesarRespuesta() {
        try {
            CatalogoProductoResponse respuesta = catalocoProductoService.obtenerCatalogo();
            return ResponseEntity.ok(respuesta);

        } catch (CatalogoException ex) {

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
