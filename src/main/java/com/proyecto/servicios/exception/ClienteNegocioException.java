package com.proyecto.servicios.exception;

public class ClienteNegocioException extends RuntimeException {
    public ClienteNegocioException(String mensaje) {
        super(mensaje);
    }
}
