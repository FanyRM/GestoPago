package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.Cuenta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Integer> {

    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cuenta c JOIN FETCH c.cliente WHERE c.numeroCuenta = :numeroCuenta")
    Optional<Cuenta> findByNumeroCuentaForUpdate(@Param("numeroCuenta") String numeroCuenta);

    @Query("SELECT c FROM Cuenta c JOIN FETCH c.cliente WHERE c.numeroCuenta = :numeroCuenta")
    Optional<Cuenta> findWithClienteByNumeroCuenta(@Param("numeroCuenta") String numeroCuenta);

    List<Cuenta> findByClienteId(Integer idCliente);

    List<Cuenta> findByEstatus(com.proyecto.servicios.enums.EstatusCuenta estatus);
}
