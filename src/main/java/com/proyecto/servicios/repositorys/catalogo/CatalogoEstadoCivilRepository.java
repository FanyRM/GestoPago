package com.proyecto.servicios.repositorys.catalogo;

import com.proyecto.servicios.entity.catalogo.CatalogoEstadoCivil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogoEstadoCivilRepository extends JpaRepository<CatalogoEstadoCivil, Integer> {
    List<CatalogoEstadoCivil> findByActivoTrue();
}
