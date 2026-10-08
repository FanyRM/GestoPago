package com.proyecto.servicios.repositorys.catalogo;

import com.proyecto.servicios.entity.catalogo.CatalogoNacionalidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogoNacionalidadRepository extends JpaRepository<CatalogoNacionalidad, Integer> {
    List<CatalogoNacionalidad> findByActivoTrue();
}
