package com.proyecto.servicios.service;

import com.proyecto.servicios.model.ClienteActualizaRequest;
import com.proyecto.servicios.model.ClienteConsultaRequest;
import com.proyecto.servicios.model.ClienteRequest;
import com.proyecto.servicios.model.ClienteResponse;
import org.springframework.data.domain.Page;

public interface IClienteService {

    ClienteResponse registrarCliente(ClienteRequest request);

    Page<ClienteResponse> consultarClientes(ClienteConsultaRequest request);

    ClienteResponse actualizarCliente(ClienteActualizaRequest request);

    void desactivarCliente(Integer id);
}
