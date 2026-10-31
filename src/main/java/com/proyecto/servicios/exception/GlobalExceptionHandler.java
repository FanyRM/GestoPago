package com.proyecto.servicios.exception;

import com.proyecto.servicios.model.GenericResponse;
import com.proyecto.servicios.security.LogSanitizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<Map<String, String>>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errores.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        GenericResponse<Map<String, String>> respuesta = GenericResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación en los campos de entrada",
                errores
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<GenericResponse<Void>> manejarSaldoInsuficiente(SaldoInsuficienteException ex) {
        log.warn("Operacion rechazada por fondos insuficientes: {}", LogSanitizer.sanitize(ex.getMessage()));
        GenericResponse<Void> respuesta = GenericResponse.error(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(respuesta);
    }

    @ExceptionHandler(OperacionRechazadaProveedorException.class)
    public ResponseEntity<GenericResponse<Void>> manejarRechazoProveedor(OperacionRechazadaProveedorException ex) {
        log.warn("Operacion rechazada por el proveedor externo: {}", LogSanitizer.sanitize(ex.getMessage()));
        GenericResponse<Void> respuesta = GenericResponse.error(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(respuesta);
    }

    @ExceptionHandler(TransaccionTimeoutConciliacionException.class)
    public ResponseEntity<GenericResponse<Map<String, String>>> manejarTimeoutConciliacion(TransaccionTimeoutConciliacionException ex) {
        log.warn("Transaccion en espera de conciliacion (upc={}): {}", ex.getUpc(), LogSanitizer.sanitize(ex.getMessage()));
        Map<String, String> datos = new HashMap<>();
        datos.put("upc", ex.getUpc());
        datos.put("estatus", "PENDIENTE_CONCILIACION");

        GenericResponse<Map<String, String>> respuesta = GenericResponse.error(
                HttpStatus.GATEWAY_TIMEOUT.value(),
                ex.getMessage(),
                datos
        );
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(respuesta);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<GenericResponse<Void>> manejarDuplicadoRecurso(DuplicateResourceException ex) {
        log.warn("Conflicto por duplicidad: {}", LogSanitizer.sanitize(ex.getMessage()));
        GenericResponse<Void> respuesta = GenericResponse.error(
                HttpStatus.CONFLICT.value(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(respuesta);
    }

    @ExceptionHandler(CuentaNoOperativaException.class)
    public ResponseEntity<GenericResponse<Void>> manejarCuentaNoOperativa(CuentaNoOperativaException ex) {
        log.warn("Cuenta no operativa: {}", LogSanitizer.sanitize(ex.getMessage()));
        GenericResponse<Void> respuesta = GenericResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<GenericResponse<Void>> manejarReglaNegocio(ReglaNegocioException ex) {
        log.warn("Violacion de regla de negocio: {}", LogSanitizer.sanitize(ex.getMessage()));
        GenericResponse<Void> respuesta = GenericResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(ProveedorExternoException.class)
    public ResponseEntity<GenericResponse<Void>> manejarProveedorExterno(ProveedorExternoException ex) {
        log.error("Fallo del servicio externo: {}", LogSanitizer.sanitize(ex.getMessage()));
        GenericResponse<Void> respuesta = GenericResponse.error(
                HttpStatus.BAD_GATEWAY.value(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(respuesta);
    }

    @ExceptionHandler({ResourceNotFoundException.class, CatalogoNotFoundException.class,
            ClienteNoEncontradoException.class, UsuarioNoEncontradoException.class})
    public ResponseEntity<GenericResponse<Void>> manejarNoEncontrado(RuntimeException ex) {
        GenericResponse<Void> respuesta = GenericResponse.error(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
    }

    @ExceptionHandler({CurpDuplicadaException.class, RfcDuplicadoException.class, CorreoDuplicadoException.class})
    public ResponseEntity<GenericResponse<Void>> manejarDuplicados(ClienteNegocioException ex) {
        GenericResponse<Void> respuesta = GenericResponse.error(
                HttpStatus.CONFLICT.value(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(respuesta);
    }

    @ExceptionHandler(ClienteNegocioException.class)
    public ResponseEntity<GenericResponse<Void>> manejarClienteNegocio(ClienteNegocioException ex) {
        GenericResponse<Void> respuesta = GenericResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericResponse<Void>> manejarCuerpoIlegible(HttpMessageNotReadableException ex) {
        log.warn("Cuerpo de solicitud invalido o no legible");
        GenericResponse<Void> respuesta = GenericResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                "El cuerpo de la solicitud no tiene un formato JSON valido o contiene datos ilegibles"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(CatalogoException.class)
    public ResponseEntity<GenericResponse<Void>> manejarCatalogoException(CatalogoException ex) {
        HttpStatus status = ex.getResponseEnum() != null ? ex.getResponseEnum().getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        String mensaje = ex.getMessage() != null ? ex.getMessage() : "Error en el catalogo";
        GenericResponse<Void> respuesta = GenericResponse.error(status.value(), mensaje);
        return ResponseEntity.status(status).body(respuesta);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse<Void>> manejarExcepcionGenerica(Exception ex) {
        log.error("Error no controlado en el servidor: ", ex);
        GenericResponse<Void> respuesta = GenericResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ha ocurrido un error inesperado al procesar la solicitud en el servidor. Por favor comuniquese con el administrador."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
    }
}
