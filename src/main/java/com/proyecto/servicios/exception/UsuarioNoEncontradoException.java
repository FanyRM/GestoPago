package com.proyecto.servicios.exception;

public class UsuarioNoEncontradoException extends ClienteNoEncontradoException {
    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
