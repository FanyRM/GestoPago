package com.proyecto.servicios.controller;

import com.proyecto.servicios.entity.catalogo.CatalogoEstadoCivil;
import com.proyecto.servicios.entity.catalogo.CatalogoNacionalidad;
import com.proyecto.servicios.entity.catalogo.CatalogoOcupacion;
import com.proyecto.servicios.entity.catalogo.CatalogoPais;
import com.proyecto.servicios.service.CatalogoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    @Autowired
    private CatalogoService catalogoService;

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
}
