package com.proyecto.servicios.repositorys.sf;

import com.proyecto.servicios.entity.sf.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByNombre(String nombre);
    Optional<Cliente> findByCurp(String curp);
    Optional<Cliente> findByRfc(String rfc);
    Optional<Cliente> findByCorreoElectronico(String correo);
    
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);
    List<Cliente> findByApellidoPaternoContainingIgnoreCase(String apellidoPaterno);
    List<Cliente> findByApellidoMaternoContainingIgnoreCase(String apellidoMaterno);
}
