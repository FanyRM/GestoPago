package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.catalogo.CatalogoEstadoCivil;
import com.proyecto.servicios.entity.catalogo.CatalogoOcupacion;
import com.proyecto.servicios.entity.catalogo.CatalogoPais;
import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.entity.sf.Domicilio;
import com.proyecto.servicios.entity.cuentas.EstatusCuenta;
import com.proyecto.servicios.exception.*;
import com.proyecto.servicios.model.ClienteActualizaRequest;
import com.proyecto.servicios.model.ClienteConsultaRequest;
import com.proyecto.servicios.model.ClienteRequest;
import com.proyecto.servicios.model.ClienteResponse;
import com.proyecto.servicios.repositorys.catalogo.CatalogoEstadoCivilRepository;
import com.proyecto.servicios.repositorys.catalogo.CatalogoOcupacionRepository;
import com.proyecto.servicios.repositorys.catalogo.CatalogoPaisRepository;
import com.proyecto.servicios.repositorys.sf.ClienteRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.service.IClienteService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements IClienteService {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final CatalogoPaisRepository paisRepository;
    private final CatalogoEstadoCivilRepository estadoCivilRepository;
    private final CatalogoOcupacionRepository ocupacionRepository;
    private final com.proyecto.servicios.security.AuthenticatedUserService authenticatedUserService;

    @Override
    @Transactional
    public ClienteResponse registrarCliente(ClienteRequest request) {
        if (clienteRepository.findByCurp(request.getCurp()).isPresent()) {
            throw new CurpDuplicadaException("Ya existe un cliente registrado con la misma CURP: " + request.getCurp());
        }
        if (clienteRepository.findByRfc(request.getRfc()).isPresent()) {
            throw new RfcDuplicadoException("Ya existe un cliente registrado con el mismo RFC: " + request.getRfc());
        }
        if (clienteRepository.findByCorreoElectronico(request.getCorreoElectronico()).isPresent()) {
            throw new CorreoDuplicadoException("Ya existe un cliente registrado con el mismo correo electrónico: " + request.getCorreoElectronico());
        }

        Cliente cliente = new Cliente();
        mapearDatosPrincipales(request, cliente);
        cliente.setActivo(true);

        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(generarNumeroCuentaUnico());
        cuenta.setSaldo(BigDecimal.ZERO);
        cuenta.setEstatus(EstatusCuenta.ACTIVA);
        cuenta.setCliente(cliente);
        cliente.setCuenta(cuenta);

        Domicilio domicilio = new Domicilio();
        domicilio.setCalle(request.getCalle());
        domicilio.setNumeroExterior(request.getNumeroExterior());
        domicilio.setNumeroInterior(request.getNumeroInterior());
        domicilio.setColonia(request.getColonia());
        domicilio.setMunicipio(request.getMunicipio());
        domicilio.setEstado(request.getEstado());
        domicilio.setCodigoPostal(request.getCodigoPostal());
        if (request.getIdPais() != null) {
            CatalogoPais pais = paisRepository.findById(request.getIdPais())
                    .orElseThrow(() -> new ClienteNegocioException("País no encontrado: " + request.getIdPais()));
            domicilio.setPais(pais);
        } else if (request.getPais() != null && !request.getPais().trim().isEmpty()) {
            CatalogoPais pais = paisRepository.findByDescripcionIgnoreCase(request.getPais().trim())
                    .orElseThrow(() -> new ClienteNegocioException("País no encontrado: " + request.getPais()));
            domicilio.setPais(pais);
        }
        domicilio.setCliente(cliente);
        cliente.setDomicilio(domicilio);

        Cliente guardado = clienteRepository.save(cliente);
        return mapearAResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponse> consultarClientes(ClienteConsultaRequest request) {
        ClienteConsultaRequest filtro = request != null ? request : new ClienteConsultaRequest();

        // Si se busca explícitamente por ID y no existe en la BD, lanzar excepción específica de negocio (404)
        if (filtro.getId() != null && !clienteRepository.existsById(filtro.getId())) {
            throw new ClienteNoEncontradoException("Cliente no encontrado con ID: " + filtro.getId());
        }

        int pagina = filtro.getPagina() != null ? Math.max(filtro.getPagina(), 0) : 0;
        int tamano = filtro.getTamano() != null ? Math.min(Math.max(filtro.getTamano(), 1), 100) : 10;
        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by("id").descending());

        Specification<Cliente> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Optional.ofNullable(filtro.getId())
                    .ifPresent(val -> predicates.add(cb.equal(root.get("id"), val)));

            Optional.ofNullable(filtro.getCurp())
                    .filter(val -> !val.trim().isEmpty())
                    .ifPresent(val -> predicates.add(cb.equal(cb.upper(root.get("curp")), val.trim().toUpperCase())));

            Optional.ofNullable(filtro.getRfc())
                    .filter(val -> !val.trim().isEmpty())
                    .ifPresent(val -> predicates.add(cb.equal(cb.upper(root.get("rfc")), val.trim().toUpperCase())));

            Optional.ofNullable(filtro.getCorreoElectronico())
                    .filter(val -> !val.trim().isEmpty())
                    .ifPresent(val -> predicates.add(cb.equal(cb.lower(root.get("correoElectronico")), val.trim().toLowerCase())));

            Optional.ofNullable(filtro.getNombre())
                    .filter(val -> !val.trim().isEmpty())
                    .ifPresent(val -> predicates.add(cb.like(cb.lower(root.get("nombre")), "%" + val.trim().toLowerCase() + "%")));

            Optional.ofNullable(filtro.getApellidoPaterno())
                    .filter(val -> !val.trim().isEmpty())
                    .ifPresent(val -> predicates.add(cb.like(cb.lower(root.get("apellidoPaterno")), "%" + val.trim().toLowerCase() + "%")));

            Optional.ofNullable(filtro.getApellidoMaterno())
                    .filter(val -> !val.trim().isEmpty())
                    .ifPresent(val -> predicates.add(cb.like(cb.lower(root.get("apellidoMaterno")), "%" + val.trim().toLowerCase() + "%")));

            Optional.ofNullable(filtro.getActivo())
                    .ifPresent(val -> predicates.add(cb.equal(root.get("activo"), val)));

            Optional.ofNullable(filtro.getNumeroCuenta())
                    .filter(val -> !val.trim().isEmpty())
                    .ifPresent(val -> predicates.add(cb.equal(cb.upper(root.join("cuenta").get("numeroCuenta")), val.trim().toUpperCase())));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return clienteRepository.findAll(spec, pageable).map(this::mapearAResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorId(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado con ID: " + id));
        return mapearAResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse actualizarCliente(ClienteActualizaRequest request) {
        Cliente cliente = clienteRepository.findById(request.getId())
                .orElseThrow(() -> new ClienteNoEncontradoException("No se puede actualizar. Cliente no encontrado con ID: " + request.getId()));

        if (request.getCorreoElectronico() != null && !request.getCorreoElectronico().trim().isEmpty()) {
            String nuevoCorreo = request.getCorreoElectronico().trim();
            if (!nuevoCorreo.equalsIgnoreCase(cliente.getCorreoElectronico())) {
                clienteRepository.findByCorreoElectronico(nuevoCorreo).ifPresent(c -> {
                    throw new CorreoDuplicadoException("Ya existe otro cliente con el correo electrónico: " + nuevoCorreo);
                });
                cliente.setCorreoElectronico(nuevoCorreo);
            }
        }

        // Actualiza únicamente campos permitidos. Campos controlados por el servidor (CURP, RFC, Cuenta, Saldo) NO se modifican.
        actualizarCamposNoNulos(request, cliente);

        Cliente actualizado = clienteRepository.save(cliente);
        return mapearAResponse(actualizado);
    }

    @Override
    @Transactional
    public void desactivarCliente(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("No se puede desactivar. Cliente no encontrado con ID: " + id));

        cliente.setActivo(false);
        if (cliente.getCuenta() != null) {
            cliente.getCuenta().setEstatus(EstatusCuenta.INACTIVA);
        }
        clienteRepository.save(cliente);
    }

    private void actualizarCamposNoNulos(ClienteActualizaRequest req, Cliente cliente) {
        Optional.ofNullable(req.getNombre()).filter(v -> !v.trim().isEmpty()).ifPresent(cliente::setNombre);
        Optional.ofNullable(req.getSegundoNombre()).filter(v -> !v.trim().isEmpty()).ifPresent(cliente::setSegundoNombre);
        Optional.ofNullable(req.getApellidoPaterno()).filter(v -> !v.trim().isEmpty()).ifPresent(cliente::setApellidoPaterno);
        Optional.ofNullable(req.getApellidoMaterno()).filter(v -> !v.trim().isEmpty()).ifPresent(cliente::setApellidoMaterno);
        Optional.ofNullable(req.getFechaNacimiento()).ifPresent(cliente::setFechaNacimiento);
        Optional.ofNullable(req.getSexo()).ifPresent(cliente::setSexo);
        Optional.ofNullable(req.getLadaMovil()).ifPresent(cliente::setLadaMovil);
        Optional.ofNullable(req.getTelefonoMovil()).filter(v -> !v.trim().isEmpty()).ifPresent(v -> cliente.setTelefonoMovil(Integer.valueOf(v)));
        Optional.ofNullable(req.getLadaAlternativo()).ifPresent(cliente::setLadaAlternativo);
        Optional.ofNullable(req.getTelefonoAlternativo()).filter(v -> !v.trim().isEmpty()).ifPresent(v -> cliente.setTelefonoAlternativo(Integer.valueOf(v)));
        Optional.ofNullable(req.getEmpresa()).filter(v -> !v.trim().isEmpty()).ifPresent(cliente::setEmpresa);
        Optional.ofNullable(req.getIngresoMensual()).ifPresent(cliente::setIngresoMensual);

        if (req.getIdNacionalidad() != null) {
            CatalogoPais nac = paisRepository.findById(req.getIdNacionalidad())
                    .orElseThrow(() -> new ClienteNegocioException("La nacionalidad especificada no existe con ID: " + req.getIdNacionalidad()));
            cliente.setNacionalidad(nac);
        } else if (req.getNacionalidad() != null && !req.getNacionalidad().trim().isEmpty()) {
            CatalogoPais nac = paisRepository.findByNacionalidadIgnoreCase(req.getNacionalidad().trim())
                    .orElseThrow(() -> new ClienteNegocioException("La nacionalidad especificada no existe: " + req.getNacionalidad()));
            cliente.setNacionalidad(nac);
        }
        if (req.getIdEstadoCivil() != null) {
            CatalogoEstadoCivil edo = estadoCivilRepository.findById(req.getIdEstadoCivil())
                    .orElseThrow(() -> new ClienteNegocioException("El estado civil especificado no existe con ID: " + req.getIdEstadoCivil()));
            cliente.setEstadoCivil(edo);
        }
        if (req.getIdOcupacion() != null) {
            CatalogoOcupacion ocup = ocupacionRepository.findById(req.getIdOcupacion())
                    .orElseThrow(() -> new ClienteNegocioException("La ocupación especificada no existe con ID: " + req.getIdOcupacion()));
            cliente.setOcupacion(ocup);
        }

        if (cliente.getDomicilio() != null) {
            Domicilio d = cliente.getDomicilio();
            Optional.ofNullable(req.getCalle()).filter(v -> !v.trim().isEmpty()).ifPresent(d::setCalle);
            Optional.ofNullable(req.getNumeroExterior()).filter(v -> !v.trim().isEmpty()).ifPresent(d::setNumeroExterior);
            Optional.ofNullable(req.getNumeroInterior()).filter(v -> !v.trim().isEmpty()).ifPresent(d::setNumeroInterior);
            Optional.ofNullable(req.getColonia()).filter(v -> !v.trim().isEmpty()).ifPresent(d::setColonia);
            Optional.ofNullable(req.getMunicipio()).filter(v -> !v.trim().isEmpty()).ifPresent(d::setMunicipio);
            Optional.ofNullable(req.getEstado()).filter(v -> !v.trim().isEmpty()).ifPresent(d::setEstado);
            Optional.ofNullable(req.getCodigoPostal()).filter(v -> !v.trim().isEmpty()).ifPresent(d::setCodigoPostal);
            if (req.getIdPais() != null) {
                CatalogoPais pais = paisRepository.findById(req.getIdPais())
                        .orElseThrow(() -> new ClienteNegocioException("El país especificado no existe con ID: " + req.getIdPais()));
                d.setPais(pais);
            } else if (req.getPais() != null && !req.getPais().trim().isEmpty()) {
                CatalogoPais pais = paisRepository.findByDescripcionIgnoreCase(req.getPais().trim())
                        .orElseThrow(() -> new ClienteNegocioException("El país especificado no existe: " + req.getPais()));
                d.setPais(pais);
            }
        }
    }

    private void mapearDatosPrincipales(ClienteRequest req, Cliente cliente) {
        cliente.setNombre(req.getNombre());
        cliente.setSegundoNombre(req.getSegundoNombre());
        cliente.setApellidoPaterno(req.getApellidoPaterno());
        cliente.setApellidoMaterno(req.getApellidoMaterno());
        cliente.setFechaNacimiento(req.getFechaNacimiento());
        cliente.setCurp(req.getCurp().trim().toUpperCase());
        cliente.setRfc(req.getRfc().trim().toUpperCase());
        cliente.setSexo(req.getSexo());
        cliente.setCorreoElectronico(req.getCorreoElectronico().trim());
        cliente.setLadaMovil(req.getLadaMovil());
        cliente.setTelefonoMovil(Integer.valueOf(req.getTelefonoMovil()));
        cliente.setLadaAlternativo(req.getLadaAlternativo());
        if (req.getTelefonoAlternativo() != null && !req.getTelefonoAlternativo().trim().isEmpty()) {
            cliente.setTelefonoAlternativo(Integer.valueOf(req.getTelefonoAlternativo()));
        }
        cliente.setEmpresa(req.getEmpresa());
        cliente.setIngresoMensual(req.getIngresoMensual());

        CatalogoPais nac = null;
        if (req.getIdNacionalidad() != null) {
            nac = paisRepository.findById(req.getIdNacionalidad())
                    .orElseThrow(() -> new ClienteNegocioException("Nacionalidad no encontrada: " + req.getIdNacionalidad()));
        } else if (req.getNacionalidad() != null && !req.getNacionalidad().trim().isEmpty()) {
            nac = paisRepository.findByNacionalidadIgnoreCase(req.getNacionalidad().trim())
                    .orElseThrow(() -> new ClienteNegocioException("Nacionalidad no encontrada: " + req.getNacionalidad()));
        }
        if (nac != null) {
            cliente.setNacionalidad(nac);
        }

        CatalogoEstadoCivil edo = estadoCivilRepository.findById(req.getIdEstadoCivil())
                .orElseThrow(() -> new ClienteNegocioException("Estado civil no encontrado: " + req.getIdEstadoCivil()));
        cliente.setEstadoCivil(edo);

        CatalogoOcupacion ocup = ocupacionRepository.findById(req.getIdOcupacion())
                .orElseThrow(() -> new ClienteNegocioException("Ocupación no encontrada: " + req.getIdOcupacion()));
        cliente.setOcupacion(ocup);
    }

    private String generarNumeroCuentaUnico() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private ClienteResponse mapearAResponse(Cliente c) {
        ClienteResponse resp = ClienteResponse.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .segundoNombre(c.getSegundoNombre())
                .apellidoPaterno(c.getApellidoPaterno())
                .apellidoMaterno(c.getApellidoMaterno())
                .fechaNacimiento(c.getFechaNacimiento())
                .curp(c.getCurp())
                .rfc(c.getRfc())
                .sexo(c.getSexo())
                .correoElectronico(c.getCorreoElectronico())
                .ladaMovil(c.getLadaMovil())
                .telefonoMovil(c.getTelefonoMovil())
                .ladaAlternativo(c.getLadaAlternativo())
                .telefonoAlternativo(c.getTelefonoAlternativo())
                .empresa(c.getEmpresa())
                .ingresoMensual(c.getIngresoMensual())
                .activo(c.getActivo())
                .build();

        if (c.getCuenta() != null) {
            resp.setNumeroCuentaAsignada(c.getCuenta().getNumeroCuenta());
        }

        if (c.getDomicilio() != null) {
            Domicilio d = c.getDomicilio();
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
                resp.setCodigoIsoPais(d.getPais().getCodigoIso());
                resp.setLadaPais(d.getPais().getLada());
            }
        }

        if (c.getNacionalidad() != null) {
            resp.setIdNacionalidad(c.getNacionalidad().getId());
            resp.setNacionalidad(c.getNacionalidad().getNacionalidad());
            if (resp.getPais() == null) {
                resp.setPais(c.getNacionalidad().getDescripcion());
            }
            if (resp.getCodigoIsoPais() == null) {
                resp.setCodigoIsoPais(c.getNacionalidad().getCodigoIso());
            }
            if (resp.getLadaPais() == null) {
                resp.setLadaPais(c.getNacionalidad().getLada());
            }
        }
        if (c.getEstadoCivil() != null) {
            resp.setIdEstadoCivil(c.getEstadoCivil().getId());
            resp.setEstadoCivil(c.getEstadoCivil().getDescripcion());
        }
        if (c.getOcupacion() != null) {
            resp.setIdOcupacion(c.getOcupacion().getId());
            resp.setOcupacion(c.getOcupacion().getNombre());
        }

        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtenerMiPerfil() {
        com.proyecto.servicios.entity.seguridad.Usuario usuario = authenticatedUserService.obtenerUsuarioAutenticadoActivo();
        if (!authenticatedUserService.esCliente(usuario) || usuario.getCliente() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Solo un cliente autenticado puede consultar su propio perfil.");
        }
        return consultarPorId(usuario.getCliente().getId());
    }
}
