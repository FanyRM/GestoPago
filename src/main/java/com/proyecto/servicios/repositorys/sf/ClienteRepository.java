package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer>, JpaSpecificationExecutor<Cliente> {

    Optional<Cliente> findByCurp(String curp);

    Optional<Cliente> findByRfc(String rfc);

    Optional<Cliente> findByCorreoElectronico(String correoElectronico);

    Optional<Cliente> findByRfcAndCurp(String rfc, String curp);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cliente c WHERE LOWER(c.correoElectronico) = LOWER(:correo)")
    boolean existsByCorreo(@Param("correo") String correo);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cliente c WHERE LOWER(c.correoElectronico) = LOWER(:correo) AND CAST(c.id AS long) <> :id")
    boolean existsByCorreoAndIdNot(@Param("correo") String correo, @Param("id") Long id);

    default Optional<Cliente> findById(Long id) {
        return id != null ? findById(id.intValue()) : Optional.empty();
    }
}
