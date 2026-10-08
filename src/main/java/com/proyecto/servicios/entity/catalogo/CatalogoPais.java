package com.proyecto.servicios.entity.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "catalogo_pais")
@Getter
@Setter
public class CatalogoPais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "descripcion", nullable = false, length = 100)
    private String descripcion;

    @Column(name = "codigo_iso", length = 3)
    private String codigoIso;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
