package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.DomicilioRequest;
import com.proyecto.servicios.model.DomicilioResponse;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.service.DomicilioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/domicilios")
public class DomicilioController {

    @Autowired
    private DomicilioService domicilioService;

    @PostMapping
    public ResponseEntity<DomicilioResponse> crearDomicilio(@Valid @RequestBody DomicilioRequest request) {
        return ResponseEntity.ok(domicilioService.crearDomicilio(request));
    }

    @GetMapping
    public ResponseEntity<List<DomicilioResponse>> consultarTodos() {
        return ResponseEntity.ok(domicilioService.consultarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DomicilioResponse> consultarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(domicilioService.consultarPorId(id));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<DomicilioResponse> consultarPorCliente(@PathVariable Integer idCliente) {
        return ResponseEntity.ok(domicilioService.consultarPorCliente(idCliente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse> actualizarDomicilio(@PathVariable Integer id, @Valid @RequestBody DomicilioRequest request) {
        return ResponseEntity.ok(domicilioService.actualizarDomicilio(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse> eliminarDomicilio(@PathVariable Integer id) {
        return ResponseEntity.ok(domicilioService.eliminarDomicilio(id));
    }
}
