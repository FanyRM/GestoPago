package com.proyecto.servicios.entity.sf;

import com.proyecto.servicios.entity.catalogo.CatalogoEstadoCivil;
import com.proyecto.servicios.entity.catalogo.CatalogoNacionalidad;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "clientes", indexes = {
    @Index(name = "idx_cliente_curp", columnList = "curp", unique = true),
    @Index(name = "idx_cliente_rfc", columnList = "rfc")
})
@Getter
@Setter
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Datos Personales
    @Column(name="nombre", nullable = false, length = 100)
    private String nombre;
    
    @Column(name="segundo_nombre", length = 100)
    private String segundoNombre;
    
    @Column(name="apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;
    
    @Column(name="apellido_materno", nullable = false, length = 100)
    private String apellidoMaterno;
    
    @Column(name="fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;
    
    @Column(name="curp", nullable = false, length = 18, unique = true)
    private String curp;
    
    @Column(name="rfc", nullable = false, length = 13)
    private String rfc;

    @Column(name="sexo", nullable = false, length = 1)
    private Character sexo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nacionalidad", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_cliente_nacionalidad"))
    private CatalogoNacionalidad nacionalidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_civil", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_cliente_estadocivil"))
    private CatalogoEstadoCivil estadoCivil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ocupacion", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_cliente_ocupacion"))
    private com.proyecto.servicios.entity.catalogo.CatalogoOcupacion ocupacion;

    @Column(name="empresa", length = 150)
    private String empresa;

    @Column(name="ingreso_mensual", nullable = false)
    private java.math.BigDecimal ingresoMensual;

    // Datos de Contacto (Embebidos por simplicidad o pueden ir directo en clientes)
    @Column(name="correo_electronico", nullable = false, length = 100, unique = true)
    private String correoElectronico;
    
    @Column(name="lada_movil", nullable = false, length = 5)
    private Integer ladaMovil;

    @Column(name="telefono_movil", nullable = false, length = 10)
    private Integer telefonoMovil;
    
    @Column(name="lada_alternativo", length = 5)
    private Integer ladaAlternativo;

    @Column(name="telefono_alternativo", length = 10)
    private Integer telefonoAlternativo;

    // Relaciones principales
    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Domicilio domicilio;

    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Cuenta cuenta;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
