package com.proyecto.servicios.controller;

import com.proyecto.servicios.entity.catalogo.CatalogoEstadoCivil;
import com.proyecto.servicios.entity.catalogo.CatalogoNacionalidad;
import com.proyecto.servicios.entity.catalogo.CatalogoOcupacion;
import com.proyecto.servicios.entity.catalogo.CatalogoPais;
import com.proyecto.servicios.model.CatalogoPaisRequest;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.service.CatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/estado-civil")
    public ResponseEntity<List<CatalogoEstadoCivil>> getEstadosCiviles() {
        return ResponseEntity.ok(catalogoService.consultarEstadosCiviles());
    }

    @GetMapping("/nacionalidad")
    public ResponseEntity<List<CatalogoNacionalidad>> getNacionalidades() {
        return ResponseEntity.ok(catalogoService.consultarNacionalidades());
    }

    @GetMapping("/ocupacion")
    public ResponseEntity<List<CatalogoOcupacion>> getOcupaciones() {
        return ResponseEntity.ok(catalogoService.consultarOcupaciones());
    }

    @GetMapping("/pais")
    public ResponseEntity<List<CatalogoPais>> getPaises() {
        return ResponseEntity.ok(catalogoService.consultarPaises());
    }

    @PostMapping("/pais")
    public ResponseEntity<GenericResponse<CatalogoPais>> registrarPais(
            @Valid @RequestBody CatalogoPaisRequest request
    ) {
        CatalogoPais paisGuardado = catalogoService.registrarPais(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GenericResponse.exito(paisGuardado, "País registrado exitosamente en el catálogo"));
    }
}
