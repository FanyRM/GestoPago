package com.proyecto.servicios.model.gestopago.catalogo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO limpio que el endpoint REST devuelve al cliente.
 * No expone detalles internos de MongoDB (como el {@code _id} del documento).
 */
@Getter
@Setter
@Builder
public class CatalogoProductoResponse {

    /** Código de respuesta (ej. 200). */
    private Integer codigo;

    /** Mensaje descriptivo del resultado. */
    private String mensaje;

    /** Número total de productos en el catálogo. */
    private Integer totalProductos;

    /** Fecha y hora de la última sincronización con el servicio externo. */
    private LocalDateTime fechaActualizacion;

    /** Lista de productos del catálogo. */
    private List<ProductoXml> productos;
}
