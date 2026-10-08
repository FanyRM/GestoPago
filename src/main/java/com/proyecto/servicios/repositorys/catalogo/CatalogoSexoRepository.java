package com.proyecto.servicios.repositorys.catalogo;

import com.proyecto.servicios.entity.catalogo.CatalogoSexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatalogoSexoRepository extends JpaRepository<CatalogoSexo, Integer> {
    List<CatalogoSexo> findByActivoTrue();
}
