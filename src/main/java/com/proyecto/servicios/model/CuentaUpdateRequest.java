package com.proyecto.servicios.model;

import com.proyecto.servicios.enums.EstatusCuenta;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CuentaUpdateRequest {

    @DecimalMin(value = "0.00", message = "El saldo no puede ser negativo")
    private BigDecimal saldo;

    private EstatusCuenta estatus;
}
