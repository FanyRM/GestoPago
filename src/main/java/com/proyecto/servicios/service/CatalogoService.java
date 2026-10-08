package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.catalogo.CatalogoEstadoCivil;
import com.proyecto.servicios.entity.catalogo.CatalogoNacionalidad;
import com.proyecto.servicios.entity.catalogo.CatalogoOcupacion;
import com.proyecto.servicios.entity.catalogo.CatalogoPais;

import java.util.List;

public interface CatalogoService {
    List<CatalogoEstadoCivil> consultarEstadosCiviles();
    List<CatalogoNacionalidad> consultarNacionalidades();
    List<CatalogoOcupacion> consultarOcupaciones();
    List<CatalogoPais> consultarPaises();
}
