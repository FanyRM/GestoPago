package com.proyecto.servicios.entity.sf;

import com.proyecto.servicios.entity.catalogo.CatalogoPais;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "domicilios")
@Getter
@Setter
public class Domicilio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="calle", nullable = false, length = 150)
    private String calle;
    
    @Column(name="numero_exterior", nullable = false, length = 20)
    private String numeroExterior;
    
    @Column(name="numero_interior", length = 20)
    private String numeroInterior;
    
    @Column(name="colonia", nullable = false, length = 150)
    private String colonia;
    
    @Column(name="municipio", nullable = false, length = 150)
    private String municipio;
    
    @Column(name="estado", nullable = false, length = 150)
    private String estado;
    
    @Column(name="codigo_postal", nullable = false, length = 10)
    private String codigoPostal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pais", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_domicilio_pais"))
    private CatalogoPais pais;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "fk_domicilio_cliente"))
    private Cliente cliente;
}
