package com.proyecto.servicios.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClienteResponse extends GenericResponse {
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String numeroCuentaAsignada;
}
