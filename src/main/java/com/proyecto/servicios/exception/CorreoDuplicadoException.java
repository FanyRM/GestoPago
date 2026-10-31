package com.proyecto.servicios.exception;

public class CorreoDuplicadoException extends ClienteNegocioException {
    public CorreoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
