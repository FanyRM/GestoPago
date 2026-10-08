package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.catalogo.CatalogoEstadoCivil;
import com.proyecto.servicios.entity.catalogo.CatalogoNacionalidad;
import com.proyecto.servicios.entity.catalogo.CatalogoOcupacion;
import com.proyecto.servicios.entity.catalogo.CatalogoPais;
import com.proyecto.servicios.repositorys.catalogo.CatalogoEstadoCivilRepository;
import com.proyecto.servicios.repositorys.catalogo.CatalogoNacionalidadRepository;
import com.proyecto.servicios.repositorys.catalogo.CatalogoOcupacionRepository;
import com.proyecto.servicios.repositorys.catalogo.CatalogoPaisRepository;
import com.proyecto.servicios.service.CatalogoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogoServiceImpl implements CatalogoService {

    @Autowired
    private CatalogoEstadoCivilRepository estadoCivilRepository;

    @Autowired
    private CatalogoNacionalidadRepository nacionalidadRepository;

    @Autowired
    private CatalogoOcupacionRepository ocupacionRepository;

    @Autowired
    private CatalogoPaisRepository paisRepository;

    @Override
    public List<CatalogoEstadoCivil> consultarEstadosCiviles() {
        return estadoCivilRepository.findAll().stream()
                .filter(CatalogoEstadoCivil::getActivo)
                .collect(Collectors.toList());
    }

    @Override
    public List<CatalogoNacionalidad> consultarNacionalidades() {
        return nacionalidadRepository.findAll().stream()
                .filter(CatalogoNacionalidad::getActivo)
                .collect(Collectors.toList());
    }

    @Override
    public List<CatalogoOcupacion> consultarOcupaciones() {
        return ocupacionRepository.findAll().stream()
                .filter(CatalogoOcupacion::getActivo)
                .collect(Collectors.toList());
    }

    @Override
    public List<CatalogoPais> consultarPaises() {
        return paisRepository.findAll().stream()
                .filter(CatalogoPais::getActivo)
                .collect(Collectors.toList());
    }
}
