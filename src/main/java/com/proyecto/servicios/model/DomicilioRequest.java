package com.proyecto.servicios.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DomicilioRequest {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Integer idCliente;

    @NotEmpty(message = "La calle es obligatoria")
    @Size(max = 100, message = "La calle debe tener máximo 100 caracteres")
    private String calle;

    @NotEmpty(message = "El número exterior es obligatorio")
    @Size(max = 10, message = "El número exterior debe tener máximo 10 caracteres")
    private String numeroExterior;

    @Size(max = 10, message = "El número interior debe tener máximo 10 caracteres")
    private String numeroInterior;

    @NotEmpty(message = "La colonia es obligatoria")
    @Size(max = 100, message = "La colonia debe tener máximo 100 caracteres")
    private String colonia;

    @NotEmpty(message = "El municipio es obligatorio")
    @Size(max = 100, message = "El municipio debe tener máximo 100 caracteres")
    private String municipio;

    @NotEmpty(message = "El estado es obligatorio")
    @Size(max = 100, message = "El estado debe tener máximo 100 caracteres")
    private String estado;

    @NotEmpty(message = "El código postal es obligatorio")
    @Pattern(regexp = "^\\d{5}$", message = "El código postal debe contener exactamente 5 dígitos")
    private String codigoPostal;

    @NotNull(message = "El ID del país es obligatorio")
    private Integer idPais;
}
