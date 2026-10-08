package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.enums.EstatusCuenta;
import com.proyecto.servicios.model.CuentaResponse;
import com.proyecto.servicios.model.CuentaUpdateRequest;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.service.CuentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CuentaServiceImpl implements CuentaService {

    @Autowired
    private CuentaRepository cuentaRepository;

    @Override
    public List<CuentaResponse> consultarTodas() {
        return cuentaRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public CuentaResponse consultarPorNumeroCuenta(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta).map(this::mapToResponse).orElseGet(() -> {
            CuentaResponse error = new CuentaResponse();
            error.setEstatus("ERROR");
            error.setMensaje("Cuenta no encontrada");
            return error;
        });
    }

    @Override
    @Transactional
    public GenericResponse actualizarCuenta(String numeroCuenta, CuentaUpdateRequest request) {
        GenericResponse response = new GenericResponse();
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta).orElse(null);
        
        if (cuenta == null) {
            response.setEstatus("ERROR");
            response.setMensaje("Cuenta no encontrada");
            return response;
        }

        if (cuenta.getCliente() == null) {
            response.setEstatus("ERROR");
            response.setMensaje("No puede existir una cuenta sin cliente asociado existente");
            return response;
        }

        // Regla: "solo clientes activos con cuentas activas"
        // Si el cliente está inactivo y se intenta activar la cuenta, se debe rechazar
        if (request.getEstatus() != null && request.getEstatus() == EstatusCuenta.ACTIVA) {
            if (!cuenta.getCliente().getActivo()) {
                response.setEstatus("ERROR");
                response.setMensaje("No se puede activar la cuenta porque el cliente asociado está inactivo.");
                return response;
            }
        }

        // Copiar solo campos no nulos
        if (request.getSaldo() != null) {
            cuenta.setSaldo(request.getSaldo());
        }
        if (request.getEstatus() != null) {
            cuenta.setEstatus(request.getEstatus());
        }

        cuentaRepository.save(cuenta);

        response.setEstatus("EXITO");
        response.setMensaje("Cuenta actualizada correctamente");
        return response;
    }

    @Override
    @Transactional
    public GenericResponse eliminarCuenta(String numeroCuenta) {
        GenericResponse response = new GenericResponse();
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta).orElse(null);
        
        if (cuenta == null) {
            response.setEstatus("ERROR");
            response.setMensaje("Cuenta no encontrada");
            return response;
        }

        cuenta.setEstatus(EstatusCuenta.INACTIVA);
        cuentaRepository.save(cuenta);

        response.setEstatus("EXITO");
        response.setMensaje("Cuenta eliminada (inactivada) correctamente");
        return response;
    }

    private CuentaResponse mapToResponse(Cuenta cuenta) {
        CuentaResponse resp = new CuentaResponse();
        resp.setEstatus("EXITO");
        resp.setMensaje("Consulta exitosa");
        resp.setId(cuenta.getId());
        resp.setNumeroCuenta(cuenta.getNumeroCuenta());
        resp.setSaldo(cuenta.getSaldo());
        resp.setEstatusCuenta(cuenta.getEstatus());
        resp.setFechaCreacion(cuenta.getFechaCreacion());
        if (cuenta.getCliente() != null) {
            resp.setIdCliente(cuenta.getCliente().getId());
        }
        return resp;
    }
}
