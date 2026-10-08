package com.proyecto.servicios.model;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ClienteRequest {

    // Datos Personales
    @NotEmpty(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo debe contener letras y espacios")
    private String nombre;

    @Size(max = 50, message = "El segundo nombre debe tener máximo 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$", message = "El segundo nombre solo debe contener letras y espacios")
    private String segundoNombre;

    @NotEmpty(message = "El apellido paterno es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido paterno debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido paterno solo debe contener letras y espacios")
    private String apellidoPaterno;

    @NotEmpty(message = "El apellido materno es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido materno debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido materno solo debe contener letras y espacios")
    private String apellidoMaterno;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento no puede ser una fecha futura")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 50, message = "La contraseña debe tener entre 8 y 50 caracteres")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
        message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial"
    )
    private String password;

    @NotEmpty(message = "La CURP es obligatoria")
    @Size(min = 18, max = 18, message = "La CURP debe contener 18 caracteres")
    @Pattern(regexp = "^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z\\d]\\d$", message = "Formato de CURP inválido")
    private String curp;

    @NotEmpty(message = "El RFC es obligatorio")
    @Size(min = 12, max = 13, message = "El RFC debe contener 12 o 13 caracteres")
    @Pattern(regexp = "^[A-ZÑ&]{3,4}\\d{6}[A-Z\\d]{3}$", message = "Formato de RFC inválido")
    private String rfc;

    @NotNull(message = "El sexo es obligatorio")
    private Character sexo;

    @NotNull(message = "El ID de la nacionalidad es obligatorio")
    @Positive(message = "El ID de la nacionalidad debe ser un número positivo")
    private Integer idNacionalidad;

    @NotNull(message = "El ID del estado civil es obligatorio")
    @Positive(message = "El ID del estado civil debe ser un número positivo")
    private Integer idEstadoCivil;

    // Datos de Contacto
    @NotEmpty(message = "El correo electrónico es obligatorio")
    @Email(message = "Debe tener un formato de correo electrónico válido")
    @Size(max = 100, message = "El correo electrónico debe tener máximo 100 caracteres")
    private String correoElectronico;

    @NotNull(message = "La lada móvil es obligatoria")
    @Min(value = 100, message = "La lada móvil debe tener al menos 3 dígitos")
    @Max(value = 999, message = "La lada móvil debe tener máximo 3 dígitos")
    private Integer ladaMovil;

    @NotNull(message = "El teléfono móvil es obligatorio")
    @Pattern(regexp = "^\\d{10}$", message = "El teléfono móvil debe contener exactamente  ́10 dígitos")
    private Integer telefonoMovil;

    @Min(value = 100, message = "La lada alternativo debe tener al menos  ́3 dígitos si se proporciona")
    @Max(value = 999, message = "La lada alternativo debe tener máximo  ́3 dígitos si se proporciona")
    private Integer ladaAlternativo;

    @Pattern(regexp = "^(\\d{10})?$", message = "El teléfono alternativo debe contener exactamente  ́10 dígitos si se proporciona")
    private Integer telefonoAlternativo;



    // Información laboral
    @NotNull(message = "El ID de la ocupación es obligatorio")
    @Positive(message = "El ID de la ocupación debe ser un número positivo")
    private Integer idOcupacion;

    @Size(max = 100, message = "La empresa debe tener máximo  ́100 caracteres")
    private String empresa;

    @NotNull(message = "El ingreso mensual es obligatorio")
    @DecimalMin(value = "0.01", message = "El ingreso mensual debe ser mayor a cero")
    @Digits(integer = 10, fraction = 2, message = "El ingreso mensual debe tener máximo  ́10 enteros y 2 decimales")
    private BigDecimal ingresoMensual;

    // Domicilio
    @Size(max = 100, message = "La calle debe tener máximo  ́100 caracteres")
    private String calle;

    @Size(max = 10, message = "El número exterior debe tener máximo  ́10 caracteres")
    private String numeroExterior;

    @Size(max = 10, message = "El número interior debe tener máximo  ́10 caracteres")
    private String numeroInterior;

    @Size(max = 100, message = "La colonia debe tener máximo  ́100 caracteres")
    private String colonia;

    @Size(max = 100, message = "El municipio debe tener máximo  ́100 caracteres")
    private String municipio;

    @Size(max = 100, message = "El estado debe tener máximo  ́100 caracteres")
    private String estado;

    @NotNull(message = "El código postal es obligatorio")
    @Pattern(regexp = "^\\d{5}$", message = "El código postal debe contener exactamente  ́5 dígitos")
    private String codigoPostal;

    @NotNull(message = "El ID del país es obligatorio")
    @Positive(message = "El ID del país debe ser un número positivo")
    private Integer idPais;

    @AssertTrue(message = "El cliente debe ser mayor de edad (18 años o más)")
    public boolean esMayorDeEdad() {
        if (fechaNacimiento == null) return false;
        return fechaNacimiento.plusYears(18).isBefore(LocalDate.now())
            || fechaNacimiento.plusYears(18).isEqual(LocalDate.now());
    }

    @AssertTrue(message = "El sexo debe ser 'H' (Hombre), 'M' (Mujer) o 'O' (Otro)")
    public boolean isSexoValido() {
        return sexo != null && (sexo == 'H' || sexo == 'M' || sexo == 'O');
    }
}
