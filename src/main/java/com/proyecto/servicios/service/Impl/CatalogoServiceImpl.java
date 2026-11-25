package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.catalogo.CatalogoEstadoCivil;
import com.proyecto.servicios.entity.catalogo.CatalogoNacionalidad;
import com.proyecto.servicios.entity.catalogo.CatalogoOcupacion;
import com.proyecto.servicios.entity.catalogo.CatalogoPais;
import com.proyecto.servicios.model.CatalogoPaisRequest;
import com.proyecto.servicios.repositorys.catalogo.CatalogoEstadoCivilRepository;
import com.proyecto.servicios.repositorys.catalogo.CatalogoNacionalidadRepository;
import com.proyecto.servicios.repositorys.catalogo.CatalogoOcupacionRepository;
import com.proyecto.servicios.repositorys.catalogo.CatalogoPaisRepository;
import com.proyecto.servicios.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoServiceImpl implements CatalogoService {

    private final CatalogoEstadoCivilRepository estadoCivilRepository;
    private final CatalogoNacionalidadRepository nacionalidadRepository;
    private final CatalogoOcupacionRepository ocupacionRepository;
    private final CatalogoPaisRepository paisRepository;

    @Override
    public List<CatalogoEstadoCivil> consultarEstadosCiviles() {
        return estadoCivilRepository.findAll().stream()
                .filter(CatalogoEstadoCivil::getActivo)
                .toList();
    }

    @Override
    public List<CatalogoNacionalidad> consultarNacionalidades() {
        return nacionalidadRepository.findAll().stream()
                .filter(CatalogoNacionalidad::getActivo)
                .toList();
    }

    @Override
    public List<CatalogoOcupacion> consultarOcupaciones() {
        return ocupacionRepository.findAll();
    }

    @Override
    public List<CatalogoPais> consultarPaises() {
        return paisRepository.findAll().stream()
                .filter(CatalogoPais::getActivo)
                .toList();
    }

    @Override
    public CatalogoPais registrarPais(CatalogoPaisRequest request) {
        CatalogoPais nuevo = new CatalogoPais();
        nuevo.setDescripcion(request.getPais() != null ? request.getPais() : request.getDescripcion());
        nuevo.setNacionalidad(request.getNacionalidad());
        nuevo.setLada(request.getLada());
        nuevo.setCodigoIso(request.getCodigoIso() != null ? request.getCodigoIso().trim().toUpperCase() : null);
        nuevo.setActivo(true);
        return paisRepository.save(nuevo);
    }
}
