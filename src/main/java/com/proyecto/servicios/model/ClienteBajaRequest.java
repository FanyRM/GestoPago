package com.proyecto.servicios.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteBajaRequest {

    @NotNull(message = "El ID es obligatorio para realizar la desactivación")
    @Positive(message = "El ID debe ser un número positivo")
    private Integer id;
}
