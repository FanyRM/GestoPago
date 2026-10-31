package com.proyecto.servicios.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {

    private Integer id;
    private String nombre;
    private String segundoNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private LocalDate fechaNacimiento;
    private String curp;
    private String rfc;
    private Character sexo;
    private String correoElectronico;
    private Integer ladaMovil;
    private Integer telefonoMovil;
    private Integer ladaAlternativo;
    private Integer telefonoAlternativo;
    private String empresa;
    private BigDecimal ingresoMensual;
    private Boolean activo;
    private String numeroCuentaAsignada;

    // Domicilio
    private String calle;
    private String numeroExterior;
    private String numeroInterior;
    private String colonia;
    private String municipio;
    private String estado;
    private String codigoPostal;
    private Integer idPais;

    // Catálogos
    private Integer idNacionalidad;
    private Integer idEstadoCivil;
    private Integer idOcupacion;
}
