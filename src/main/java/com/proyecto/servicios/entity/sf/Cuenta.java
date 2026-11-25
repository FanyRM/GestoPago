package com.proyecto.servicios.entity.sf;

import com.proyecto.servicios.entity.cuentas.EstatusCuenta;
import com.proyecto.servicios.entity.cuentas.TipoCuenta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "cuentas", indexes = {
    @Index(name = "idx_cuenta_numero", columnList = "numero_cuenta", unique = true)
})
@Getter
@Setter
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 20)
    private String numeroCuenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    private TipoCuenta tipoCuenta = TipoCuenta.AHORRO;

    @Column(name = "saldo", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus", nullable = false, length = 30)
    private EstatusCuenta estatus = EstatusCuenta.PENDIENTE_REVISION;

    @Column(name = "revisado_por", length = 100)
    private String revisadoPor;

    @Column(name = "observacion_revision", length = 255)
    private String observacionRevision;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "fk_cuenta_cliente"))
    private Cliente cliente;

    @PrePersist
    void onCreate() {
        this.fechaCreacion = LocalDateTime.now(ZoneId.systemDefault());
        if (this.tipoCuenta == null) {
            this.tipoCuenta = TipoCuenta.AHORRO;
        }
        if (this.estatus == null) {
            this.estatus = EstatusCuenta.PENDIENTE_REVISION;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now(ZoneId.systemDefault());
    }
}
