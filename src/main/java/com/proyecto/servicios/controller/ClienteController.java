package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.ClienteActualizaRequest;
import com.proyecto.servicios.model.ClienteBajaRequest;
import com.proyecto.servicios.model.ClienteConsultaRequest;
import com.proyecto.servicios.model.ClienteRequest;
import com.proyecto.servicios.model.ClienteResponse;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.model.PaginaResponse;
import com.proyecto.servicios.service.IClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/clientes", "/usuarios"})
@RequiredArgsConstructor
public class ClienteController {

    private final IClienteService clienteService;

    @PostMapping
    public ResponseEntity<GenericResponse<ClienteResponse>> registrarCliente(
            @Valid @RequestBody ClienteRequest request
    ) {
        ClienteResponse cliente = clienteService.registrarCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GenericResponse.exito(cliente, "Cliente registrado exitosamente"));
    }

    @PostMapping({"/consultar", "/buscar"})
    public ResponseEntity<GenericResponse<PaginaResponse<ClienteResponse>>> consultarClientes(
            @Valid @RequestBody(required = false) ClienteConsultaRequest request
    ) {
        ClienteConsultaRequest consulta = request != null ? request : new ClienteConsultaRequest();
        Page<ClienteResponse> paginaClientes = clienteService.consultarClientes(consulta);
        return ResponseEntity.ok(GenericResponse.exito(PaginaResponse.de(paginaClientes), "Consulta realizada con éxito"));
    }

    @PutMapping
    public ResponseEntity<GenericResponse<ClienteResponse>> actualizarCliente(
            @Valid @RequestBody ClienteActualizaRequest request
    ) {
        ClienteResponse clienteActualizado = clienteService.actualizarCliente(request);
        return ResponseEntity.ok(GenericResponse.exito(clienteActualizado, "Cliente actualizado correctamente"));
    }

    @DeleteMapping
    public ResponseEntity<GenericResponse<Void>> desactivarCliente(
            @Valid @RequestBody ClienteBajaRequest request
    ) {
        clienteService.desactivarCliente(request.getId());
        return ResponseEntity.ok(GenericResponse.exito("Cliente desactivado correctamente (baja lógica)"));
    }
}
