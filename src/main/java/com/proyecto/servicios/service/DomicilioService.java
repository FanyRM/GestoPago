package com.proyecto.servicios.service;

import com.proyecto.servicios.model.DomicilioRequest;
import com.proyecto.servicios.model.DomicilioResponse;
import com.proyecto.servicios.model.GenericResponse;

import java.util.List;

public interface DomicilioService {
    DomicilioResponse crearDomicilio(DomicilioRequest request);
    List<DomicilioResponse> consultarTodos();
    DomicilioResponse consultarPorId(Integer id);
    DomicilioResponse consultarPorCliente(Integer idCliente);
    GenericResponse actualizarDomicilio(Integer id, DomicilioRequest request);
    GenericResponse eliminarDomicilio(Integer id);
}
