package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.CuentaResponse;
import com.proyecto.servicios.model.CuentaUpdateRequest;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.service.CuentaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cuentas")
public class CuentaController {

    @Autowired
    private CuentaService cuentaService;

    @GetMapping
    public ResponseEntity<List<CuentaResponse>> consultarTodas() {
        return ResponseEntity.ok(cuentaService.consultarTodas());
    }

    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponse> consultarPorNumero(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.consultarPorNumeroCuenta(numeroCuenta));
    }

    @PatchMapping("/{numeroCuenta}")
    public ResponseEntity<GenericResponse> actualizarCuentaParcial(
            @PathVariable String numeroCuenta, 
            @Valid @RequestBody CuentaUpdateRequest request) {
        return ResponseEntity.ok(cuentaService.actualizarCuenta(numeroCuenta, request));
    }

    @DeleteMapping("/{numeroCuenta}")
    public ResponseEntity<GenericResponse> eliminarCuenta(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.eliminarCuenta(numeroCuenta));
    }
}
