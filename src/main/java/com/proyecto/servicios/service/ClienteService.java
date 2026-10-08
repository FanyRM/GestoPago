package com.proyecto.servicios.service;

import com.proyecto.servicios.model.ClienteRequest;
import com.proyecto.servicios.model.ClienteResponse;
import com.proyecto.servicios.model.GenericResponse;

import java.util.List;

public interface ClienteService {
    ClienteResponse creaCliente(ClienteRequest request);
    List<ClienteResponse> consultarTodos();
    ClienteResponse consultarPorId(Integer id);
    ClienteResponse consultarPorCurp(String curp);
    ClienteResponse consultarPorRfc(String rfc);
    ClienteResponse consultarPorCuenta(String numeroCuenta);
    GenericResponse actualizaCliente(Integer id, ClienteRequest request);
    GenericResponse desactivarCliente(Integer id);
}
