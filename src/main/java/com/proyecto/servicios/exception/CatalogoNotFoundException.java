package com.proyecto.servicios.exception;

public class CatalogoNotFoundException extends RuntimeException {
    public CatalogoNotFoundException(String message) {
        super(message);
    }
}
