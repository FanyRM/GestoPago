package com.proyecto.servicios.entity.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "catalogo_ocupacion")
@Getter
@Setter
public class CatalogoOcupacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name="descripcion", length = 255)
    private String descripcion;
}
