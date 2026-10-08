package com.proyecto.servicios.service;

import com.proyecto.servicios.model.CuentaResponse;
import com.proyecto.servicios.model.CuentaUpdateRequest;
import com.proyecto.servicios.model.GenericResponse;

import java.util.List;

public interface CuentaService {
    List<CuentaResponse> consultarTodas();
    CuentaResponse consultarPorNumeroCuenta(String numeroCuenta);
    GenericResponse actualizarCuenta(String numeroCuenta, CuentaUpdateRequest request);
    GenericResponse eliminarCuenta(String numeroCuenta); // Borrado lógico
}
