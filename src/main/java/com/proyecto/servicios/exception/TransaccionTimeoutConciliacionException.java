package com.proyecto.servicios.exception;

import lombok.Getter;

@Getter
public class TransaccionTimeoutConciliacionException extends RuntimeException {

    private final String upc;

    public TransaccionTimeoutConciliacionException(String upc, String message) {
        super(message);
        this.upc = upc;
    }
}
