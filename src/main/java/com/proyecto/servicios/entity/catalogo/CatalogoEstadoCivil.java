package com.proyecto.servicios.entity.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "catalogo_estado_civil")
@Getter
@Setter
public class CatalogoEstadoCivil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "descripcion", nullable = false, length = 100)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
