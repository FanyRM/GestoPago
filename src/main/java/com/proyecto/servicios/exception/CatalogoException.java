package com.proyecto.servicios.exception;

import com.proyecto.servicios.enums.ApiResponseEnum;

/**
 * Excepción personalizada del dominio de catálogo de productos.
 * Encapsula un {@link ApiResponseEnum} para transportar el código HTTP
 * y el mensaje de error de manera estandarizada hasta la capa de presentación.
 */
public class CatalogoException extends RuntimeException {

    private final ApiResponseEnum responseEnum;

    public CatalogoException(ApiResponseEnum responseEnum) {
        super(responseEnum.getMensaje());
        this.responseEnum = responseEnum;
    }

    public CatalogoException(ApiResponseEnum responseEnum, Throwable cause) {
        super(responseEnum.getMensaje(), cause);
        this.responseEnum = responseEnum;
    }

    public ApiResponseEnum getResponseEnum() {
        return responseEnum;
    }
}
