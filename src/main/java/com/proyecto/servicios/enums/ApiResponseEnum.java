package com.proyecto.servicios.enums;

import org.springframework.http.HttpStatus;

/**
 * Enum centralizado para el manejo de respuestas de la API.
 * Define los códigos HTTP y mensajes descriptivos para cada escenario
 * de éxito o error dentro del sistema.
 */
public enum ApiResponseEnum {

    // -------------------------------------------------------------------------
    // Respuestas exitosas
    // -------------------------------------------------------------------------

    OK(HttpStatus.OK, "Operación realizada correctamente"),

    // -------------------------------------------------------------------------
    // Errores relacionados con MongoDB (caché)
    // -------------------------------------------------------------------------

    MONGO_NOT_FOUND(HttpStatus.NOT_FOUND,
            "No se encontró información del catálogo en caché. " +
            "El proceso de sincronización aún no ha ejecutado o falló."),

    MONGO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "Error al consultar la base de datos de caché (MongoDB)."),

    // -------------------------------------------------------------------------
    // Errores de integración con el servicio externo
    // -------------------------------------------------------------------------

    EXTERNAL_SERVICE_ERROR(HttpStatus.BAD_GATEWAY,
            "Error al comunicarse con el servicio externo de GestoPago."),

    EXTERNAL_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE,
            "El servicio externo de GestoPago no está disponible en este momento."),

    // -------------------------------------------------------------------------
    // Errores de autenticación
    // -------------------------------------------------------------------------

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,
            "No se pudo autenticar con el servicio externo. Token inválido o expirado."),

    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED,
            "No existe un token activo para autenticarse con el servicio externo."),

    // -------------------------------------------------------------------------
    // Errores de tiempo de espera
    // -------------------------------------------------------------------------

    TIMEOUT(HttpStatus.GATEWAY_TIMEOUT,
            "El servicio externo tardó demasiado en responder (timeout)."),

    // -------------------------------------------------------------------------
    // Errores de deserialización / parseo
    // -------------------------------------------------------------------------

    PARSE_ERROR(HttpStatus.UNPROCESSABLE_ENTITY,
            "La respuesta del servicio externo no pudo ser procesada (error de parseo XML)."),

    // -------------------------------------------------------------------------
    // Error genérico interno
    // -------------------------------------------------------------------------

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "Error interno del servidor. Consulte los logs para más detalles.");

    // -------------------------------------------------------------------------

    private final HttpStatus httpStatus;
    private final String mensaje;

    ApiResponseEnum(HttpStatus httpStatus, String mensaje) {
        this.httpStatus = httpStatus;
        this.mensaje = mensaje;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getCodigo() {
        return httpStatus.value();
    }

    public String getMensaje() {
        return mensaje;
    }
}
