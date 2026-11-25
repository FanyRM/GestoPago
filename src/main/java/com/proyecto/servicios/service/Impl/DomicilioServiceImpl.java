package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.catalogo.CatalogoPais;
import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Domicilio;
import com.proyecto.servicios.model.DomicilioRequest;
import com.proyecto.servicios.model.DomicilioResponse;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.repositorys.catalogo.CatalogoPaisRepository;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.DomicilioRepository;
import com.proyecto.servicios.service.DomicilioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DomicilioServiceImpl implements DomicilioService {

    private static final String RESP_EXITO = "EXITO";
    private static final String RESP_ERROR = "ERROR";
    private static final String MSG_DOMICILIO_NO_ENCONTRADO = "Domicilio no encontrado";
    private static final String MSG_CONSULTA_EXITOSA = "Consulta exitosa";

    private final DomicilioRepository domicilioRepository;
    private final ClienteRepository clienteRepository;
    private final CatalogoPaisRepository paisRepository;

    @Override
    @Transactional
    public DomicilioResponse crearDomicilio(DomicilioRequest request) {
        DomicilioResponse response = new DomicilioResponse();
        
        Cliente cliente = clienteRepository.findById(request.getIdCliente()).orElse(null);
        if (cliente == null) {
            response.setEstatus(RESP_ERROR);
            response.setMensaje("Cliente no encontrado");
            return response;
        }

        if (domicilioRepository.findByClienteId(cliente.getId()).isPresent()) {
            response.setEstatus(RESP_ERROR);
            response.setMensaje("El cliente ya tiene un domicilio registrado");
            return response;
        }

        Domicilio domicilio = new Domicilio();
        mapRequestToDomicilio(request, domicilio);
        domicilio.setCliente(cliente);

        if (request.getIdPais() != null) {
            CatalogoPais pais = paisRepository.findById(request.getIdPais()).orElse(null);
            domicilio.setPais(pais);
        } else if (request.getPais() != null && !request.getPais().trim().isEmpty()) {
            CatalogoPais pais = paisRepository.findByDescripcionIgnoreCase(request.getPais().trim()).orElse(null);
            domicilio.setPais(pais);
        } else if (request.getPais() != null && !request.getPais().trim().isEmpty()) {
            CatalogoPais pais = paisRepository.findByDescripcionIgnoreCase(request.getPais().trim()).orElse(null);
            domicilio.setPais(pais);
        }

        domicilio = domicilioRepository.save(domicilio);
        return mapToResponse(domicilio, "Domicilio creado exitosamente");
    }

    @Override
    public List<DomicilioResponse> consultarTodos() {
        return domicilioRepository.findAll().stream()
                .map(d -> mapToResponse(d, MSG_CONSULTA_EXITOSA))
                .toList();
    }

    @Override
    public DomicilioResponse consultarPorId(Integer id) {
        return domicilioRepository.findById(id)
                .map(d -> mapToResponse(d, MSG_CONSULTA_EXITOSA))
                .orElseGet(() -> {
                    DomicilioResponse error = new DomicilioResponse();
                    error.setEstatus(RESP_ERROR);
                    error.setMensaje(MSG_DOMICILIO_NO_ENCONTRADO);
                    return error;
                });
    }

    @Override
    public DomicilioResponse consultarPorCliente(Integer idCliente) {
        return domicilioRepository.findByClienteId(idCliente)
                .map(d -> mapToResponse(d, MSG_CONSULTA_EXITOSA))
                .orElseGet(() -> {
                    DomicilioResponse error = new DomicilioResponse();
                    error.setEstatus(RESP_ERROR);
                    error.setMensaje("Domicilio no encontrado para este cliente");
                    return error;
                });
    }

    @Override
    @Transactional
    public GenericResponse actualizarDomicilio(Integer id, DomicilioRequest request) {
        GenericResponse response = new GenericResponse();
        Domicilio domicilio = domicilioRepository.findById(id).orElse(null);

        if (domicilio == null) {
            response.setEstatus(RESP_ERROR);
            response.setMensaje(MSG_DOMICILIO_NO_ENCONTRADO);
            return response;
        }

        mapRequestToDomicilio(request, domicilio);

        if (request.getIdPais() != null) {
            CatalogoPais pais = paisRepository.findById(request.getIdPais()).orElse(null);
            domicilio.setPais(pais);
        } else if (request.getPais() != null && !request.getPais().trim().isEmpty()) {
            CatalogoPais pais = paisRepository.findByDescripcionIgnoreCase(request.getPais().trim()).orElse(null);
            domicilio.setPais(pais);
        } else if (request.getPais() != null && !request.getPais().trim().isEmpty()) {
            CatalogoPais pais = paisRepository.findByDescripcionIgnoreCase(request.getPais().trim()).orElse(null);
            domicilio.setPais(pais);
        }

        // Si se envía otro idCliente, validar si es posible cambiarlo (en general, no se recomienda, 
        // pero lo actualizaremos si el cliente existe y no tiene domicilio).
        if (!domicilio.getCliente().getId().equals(request.getIdCliente())) {
            Cliente nuevoCliente = clienteRepository.findById(request.getIdCliente()).orElse(null);
            if (nuevoCliente == null) {
                response.setEstatus(RESP_ERROR);
                response.setMensaje("El nuevo cliente indicado no existe");
                return response;
            }
            if (domicilioRepository.findByClienteId(nuevoCliente.getId()).isPresent()) {
                response.setEstatus(RESP_ERROR);
                response.setMensaje("El nuevo cliente ya tiene un domicilio asociado");
                return response;
            }
            domicilio.setCliente(nuevoCliente);
        }

        domicilioRepository.save(domicilio);
        response.setEstatus(RESP_EXITO);
        response.setMensaje("Domicilio actualizado correctamente");
        return response;
    }

    @Override
    @Transactional
    public GenericResponse eliminarDomicilio(Integer id) {
        GenericResponse response = new GenericResponse();
        if (!domicilioRepository.existsById(id)) {
            response.setEstatus(RESP_ERROR);
            response.setMensaje(MSG_DOMICILIO_NO_ENCONTRADO);
            return response;
        }
        
        domicilioRepository.deleteById(id);
        response.setEstatus(RESP_EXITO);
        response.setMensaje("Domicilio eliminado correctamente");
        return response;
    }

    private void mapRequestToDomicilio(DomicilioRequest request, Domicilio domicilio) {
        domicilio.setCalle(request.getCalle());
        domicilio.setNumeroExterior(request.getNumeroExterior());
        domicilio.setNumeroInterior(request.getNumeroInterior());
        domicilio.setColonia(request.getColonia());
        domicilio.setMunicipio(request.getMunicipio());
        domicilio.setEstado(request.getEstado());
        domicilio.setCodigoPostal(request.getCodigoPostal());
    }

    private DomicilioResponse mapToResponse(Domicilio d, String mensaje) {
        DomicilioResponse resp = new DomicilioResponse();
        resp.setEstatus(RESP_EXITO);
        resp.setMensaje(mensaje);
        resp.setId(d.getId());
        resp.setCalle(d.getCalle());
        resp.setNumeroExterior(d.getNumeroExterior());
        resp.setNumeroInterior(d.getNumeroInterior());
        resp.setColonia(d.getColonia());
        resp.setMunicipio(d.getMunicipio());
        resp.setEstado(d.getEstado());
        resp.setCodigoPostal(d.getCodigoPostal());
        if (d.getPais() != null) {
            resp.setIdPais(d.getPais().getId());
            resp.setPais(d.getPais().getDescripcion());
            resp.setCodigoIso(d.getPais().getCodigoIso());
            resp.setLada(d.getPais().getLada());
        }
        if (d.getCliente() != null) {
            resp.setIdCliente(d.getCliente().getId());
        }
        return resp;
    }
}
