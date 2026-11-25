package com.proyecto.servicios.repositorys.catalogo;

import com.proyecto.servicios.entity.catalogo.CatalogoPais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatalogoPaisRepository extends JpaRepository<CatalogoPais, Integer> {
    List<CatalogoPais> findByActivoTrue();
    Optional<CatalogoPais> findByDescripcionIgnoreCase(String descripcion);
    Optional<CatalogoPais> findByNacionalidadIgnoreCase(String nacionalidad);
}
