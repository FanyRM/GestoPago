package com.proyecto.servicios.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DomicilioResponse extends GenericResponse {
    private Integer id;
    private String calle;
    private String numeroExterior;
    private String numeroInterior;
    private String colonia;
    private String municipio;
    private String estado;
    private String codigoPostal;
    private Integer idPais;
    private String pais;
    private String codigoIso;
    private String lada;
    private Integer idCliente;
}
