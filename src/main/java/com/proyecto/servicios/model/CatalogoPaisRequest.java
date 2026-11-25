package com.proyecto.servicios.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoPaisRequest {

    @NotBlank(message = "El país es obligatorio")
    @Size(min = 3, max = 100, message = "El país debe tener entre 3 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El país solo debe contener letras y espacios")
    private String pais;

    @NotBlank(message = "La nacionalidad es obligatoria")
    @Size(min = 3, max = 100, message = "La nacionalidad debe tener entre 3 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "La nacionalidad solo debe contener letras y espacios")
    private String nacionalidad;

    @NotBlank(message = "La lada es obligatoria")
    @Pattern(regexp = "^\\d{1,3}$", message = "La lada debe ser un valor numérico y tener entre 1 y 3 dígitos")
    private String lada;

    @Size(min = 2, max = 3, message = "El código ISO debe tener entre 2 y 3 caracteres")
    @Pattern(regexp = "^([A-Z]{2,3})?$", message = "El código ISO debe contener 2 o 3 letras mayúsculas")
    private String codigoIso;

    public String getDescripcion() {
        return pais;
    }

    public void setDescripcion(String descripcion) {
        this.pais = descripcion;
    }
}
