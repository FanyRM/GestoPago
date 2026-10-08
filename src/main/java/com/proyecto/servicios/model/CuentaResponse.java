package com.proyecto.servicios.model;

import com.proyecto.servicios.enums.EstatusCuenta;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CuentaResponse extends GenericResponse {
    private Integer id;
    private String numeroCuenta;
    private BigDecimal saldo;
    private EstatusCuenta estatusCuenta;
    private LocalDateTime fechaCreacion;
    private Integer idCliente;
}
