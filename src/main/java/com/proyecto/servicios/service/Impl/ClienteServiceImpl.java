package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.catalogo.CatalogoEstadoCivil;
import com.proyecto.servicios.entity.catalogo.CatalogoNacionalidad;
import com.proyecto.servicios.entity.catalogo.CatalogoOcupacion;
import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.entity.sf.Domicilio;
import com.proyecto.servicios.model.ClienteRequest;
import com.proyecto.servicios.model.ClienteResponse;
import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.repositorys.catalogo.CatalogoEstadoCivilRepository;
import com.proyecto.servicios.repositorys.catalogo.CatalogoNacionalidadRepository;
import com.proyecto.servicios.repositorys.catalogo.CatalogoOcupacionRepository;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private CatalogoNacionalidadRepository nacionalidadRepository;

    @Autowired
    private CatalogoEstadoCivilRepository estadoCivilRepository;

    @Autowired
    private CatalogoOcupacionRepository ocupacionRepository;

    @Override
    @Transactional
    public ClienteResponse creaCliente(ClienteRequest request) {
        ClienteResponse response = new ClienteResponse();

        // Regla: Mayor de edad
        if (Period.between(request.getFechaNacimiento(), LocalDate.now()).getYears() < 18) {
            response.setEstatus("ERROR");
            response.setMensaje("El cliente debe ser mayor de edad (18 años o más).");
            return response;
        }

        // Reglas: Unicidad
        if (clienteRepository.findByCurp(request.getCurp()).isPresent()) {
            response.setEstatus("ERROR");
            response.setMensaje("Ya existe un cliente con la misma CURP.");
            return response;
        }
        if (clienteRepository.findByRfc(request.getRfc()).isPresent()) {
            response.setEstatus("ERROR");
            response.setMensaje("Ya existe un cliente con el mismo RFC.");
            return response;
        }
        if (clienteRepository.findByCorreoElectronico(request.getCorreoElectronico()).isPresent()) {
            response.setEstatus("ERROR");
            response.setMensaje("Ya existe un cliente con el mismo correo electrónico.");
            return response;
        }

        Cliente cliente = new Cliente();
        mapRequestToCliente(request, cliente);
        cliente.setActivo(true);

        // Cuenta automática
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(generarNumeroCuenta());
        cuenta.setSaldo(BigDecimal.ZERO);
        cuenta.setEstatus(com.proyecto.servicios.enums.EstatusCuenta.ACTIVA);
        cuenta.setCliente(cliente);
        cliente.setCuenta(cuenta);

        // Domicilio
        Domicilio domicilio = new Domicilio();
        domicilio.setCalle(request.getCalle());
        domicilio.setNumeroExterior(request.getNumeroExterior());
        domicilio.setNumeroInterior(request.getNumeroInterior());
        domicilio.setColonia(request.getColonia());
        domicilio.setMunicipio(request.getMunicipio());
        domicilio.setEstado(request.getEstado());
        domicilio.setCodigoPostal(request.getCodigoPostal());
        domicilio.setCliente(cliente);
        cliente.setDomicilio(domicilio);

        cliente = clienteRepository.save(cliente);

        response.setEstatus("EXITO");
        response.setMensaje("Cliente creado exitosamente");
        response.setNombre(cliente.getNombre());
        response.setApellidoPaterno(cliente.getApellidoPaterno());
        response.setApellidoMaterno(cliente.getApellidoMaterno());
        response.setNumeroCuentaAsignada(cliente.getCuenta().getNumeroCuenta());

        return response;
    }

    @Override
    public List<ClienteResponse> consultarTodos() {
        return clienteRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public ClienteResponse consultarPorId(Integer id) {
        return clienteRepository.findById(id).map(this::mapToResponse).orElseGet(() -> {
            ClienteResponse error = new ClienteResponse();
            error.setEstatus("ERROR");
            error.setMensaje("Cliente no encontrado");
            return error;
        });
    }

    @Override
    public ClienteResponse consultarPorCurp(String curp) {
        return clienteRepository.findByCurp(curp).map(this::mapToResponse).orElseGet(() -> {
            ClienteResponse error = new ClienteResponse();
            error.setEstatus("ERROR");
            error.setMensaje("Cliente no encontrado");
            return error;
        });
    }

    @Override
    public ClienteResponse consultarPorRfc(String rfc) {
        return clienteRepository.findByRfc(rfc).map(this::mapToResponse).orElseGet(() -> {
            ClienteResponse error = new ClienteResponse();
            error.setEstatus("ERROR");
            error.setMensaje("Cliente no encontrado");
            return error;
        });
    }

    @Override
    public ClienteResponse consultarPorCuenta(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta).map(c -> mapToResponse(c.getCliente())).orElseGet(() -> {
            ClienteResponse error = new ClienteResponse();
            error.setEstatus("ERROR");
            error.setMensaje("Cuenta no encontrada");
            return error;
        });
    }

    @Override
    @Transactional
    public GenericResponse actualizaCliente(Integer id, ClienteRequest request) {
        GenericResponse response = new GenericResponse();
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente == null) {
            response.setEstatus("ERROR");
            response.setMensaje("Cliente no encontrado");
            return response;
        }

        // Se permite actualizar Datos Personales (excepto CURP y RFC), Datos de Contacto, Domicilio, Información Laboral.
        cliente.setNombre(request.getNombre());
        cliente.setSegundoNombre(request.getSegundoNombre());
        cliente.setApellidoPaterno(request.getApellidoPaterno());
        cliente.setApellidoMaterno(request.getApellidoMaterno());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        
        if (!cliente.getCorreoElectronico().equals(request.getCorreoElectronico()) &&
            clienteRepository.findByCorreoElectronico(request.getCorreoElectronico()).isPresent()) {
            response.setEstatus("ERROR");
            response.setMensaje("Ya existe otro cliente con el mismo correo electrónico.");
            return response;
        }

        cliente.setCorreoElectronico(request.getCorreoElectronico());
        cliente.setLadaMovil(request.getLadaMovil());
        cliente.setTelefonoMovil(request.getTelefonoMovil());
        cliente.setLadaAlternativo(request.getLadaAlternativo());
        cliente.setTelefonoAlternativo(request.getTelefonoAlternativo());
        cliente.setEmpresa(request.getEmpresa());
        cliente.setIngresoMensual(request.getIngresoMensual());
        cliente.setSexo(request.getSexo());

        setCatalogos(request, cliente);

        if (cliente.getDomicilio() != null) {
            cliente.getDomicilio().setCalle(request.getCalle());
            cliente.getDomicilio().setNumeroExterior(request.getNumeroExterior());
            cliente.getDomicilio().setNumeroInterior(request.getNumeroInterior());
            cliente.getDomicilio().setColonia(request.getColonia());
            cliente.getDomicilio().setMunicipio(request.getMunicipio());
            cliente.getDomicilio().setEstado(request.getEstado());
            cliente.getDomicilio().setCodigoPostal(request.getCodigoPostal());
        }

        clienteRepository.save(cliente);

        response.setEstatus("EXITO");
        response.setMensaje("Cliente actualizado correctamente");
        return response;
    }

    @Override
    @Transactional
    public GenericResponse desactivarCliente(Integer id) {
        GenericResponse response = new GenericResponse();
        Cliente cliente = clienteRepository.findById(id).orElse(null);
        if (cliente == null) {
            response.setEstatus("ERROR");
            response.setMensaje("Cliente no encontrado");
            return response;
        }
        cliente.setActivo(false);
        if (cliente.getCuenta() != null) {
            cliente.getCuenta().setEstatus(com.proyecto.servicios.enums.EstatusCuenta.INACTIVA);
        }
        clienteRepository.save(cliente);
        response.setEstatus("EXITO");
        response.setMensaje("Cliente desactivado correctamente (borrado lógico)");
        return response;
    }

    private void mapRequestToCliente(ClienteRequest request, Cliente cliente) {
        cliente.setNombre(request.getNombre());
        cliente.setSegundoNombre(request.getSegundoNombre());
        cliente.setApellidoPaterno(request.getApellidoPaterno());
        cliente.setApellidoMaterno(request.getApellidoMaterno());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setCurp(request.getCurp());
        cliente.setRfc(request.getRfc());
        cliente.setCorreoElectronico(request.getCorreoElectronico());
        cliente.setLadaMovil(request.getLadaMovil());
        cliente.setTelefonoMovil(request.getTelefonoMovil());
        cliente.setLadaAlternativo(request.getLadaAlternativo());
        cliente.setTelefonoAlternativo(request.getTelefonoAlternativo());
        cliente.setEmpresa(request.getEmpresa());
        cliente.setIngresoMensual(request.getIngresoMensual());
        cliente.setSexo(request.getSexo());
        setCatalogos(request, cliente);
    }

    private void setCatalogos(ClienteRequest request, Cliente cliente) {
        if (request.getIdNacionalidad() != null) {
            CatalogoNacionalidad nac = nacionalidadRepository.findById(request.getIdNacionalidad()).orElse(null);
            cliente.setNacionalidad(nac);
        }
        if (request.getIdEstadoCivil() != null) {
            CatalogoEstadoCivil edo = estadoCivilRepository.findById(request.getIdEstadoCivil()).orElse(null);
            cliente.setEstadoCivil(edo);
        }
        if (request.getIdOcupacion() != null) {
            CatalogoOcupacion ocup = ocupacionRepository.findById(request.getIdOcupacion()).orElse(null);
            cliente.setOcupacion(ocup);
        }
    }

    private String generarNumeroCuenta() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
    }

    private ClienteResponse mapToResponse(Cliente c) {
        ClienteResponse cr = new ClienteResponse();
        cr.setEstatus("EXITO");
        cr.setMensaje("Consulta exitosa");
        cr.setNombre(c.getNombre());
        cr.setApellidoPaterno(c.getApellidoPaterno());
        cr.setApellidoMaterno(c.getApellidoMaterno());
        if (c.getCuenta() != null) {
            cr.setNumeroCuentaAsignada(c.getCuenta().getNumeroCuenta());
        }
        return cr;
    }
}
