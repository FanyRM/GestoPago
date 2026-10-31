package com.proyecto.servicios.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteConsultaRequest {

    @Positive(message = "El ID debe ser un número positivo si se proporciona")
    private Integer id;

    private String nombre;

    private String apellidoPaterno;

    private String apellidoMaterno;

    private String curp;

    private String rfc;

    private String correoElectronico;

    private String numeroCuenta;

    private Boolean activo;

    @Builder.Default
    @Min(value = 0, message = "El número de página no puede ser menor a 0")
    private Integer pagina = 0;

    @Builder.Default
    @Min(value = 1, message = "El tamaño de página debe ser al menos 1")
    @Max(value = 100, message = "El tamaño de página no puede exceder 100")
    private Integer tamano = 10;
}
