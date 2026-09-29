package com.proyecto.servicios.controller;

import com.proyecto.servicios.exception.CatalogoException;
import com.proyecto.servicios.model.gestopago.catalogo.CatalogoProductoResponse;
import com.proyecto.servicios.service.CatalogoProductoService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/gestopago")
public class CatalogoProductoController {

    private final CatalogoProductoService catalocoProductoService;

    @GetMapping(value = "/catalogo/productos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CatalogoProductoResponse> obtenerCatalogo() {
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
