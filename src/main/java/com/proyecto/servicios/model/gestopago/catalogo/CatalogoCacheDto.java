package com.proyecto.servicios.model.gestopago.catalogo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Documento MongoDB que representa la caché del catálogo de productos de GestoPago.
 *
 * <p>La colección {@code catalogo_productos} almacenará un único documento actualizado
 * diariamente por el cron job. En cada sincronización se eliminan los documentos
 * anteriores y se inserta uno nuevo con la información más reciente.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Document(collection = "catalogo_productos")
public class CatalogoCacheDto {

    @Id
    private String id;

    /** Lista de productos deserializados desde el XML. */
    @Field("productos")
    private List<ProductoXml> productos;

    /** Fecha y hora en que se realizó la última sincronización. */
    @Field("fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    /** Número total de productos almacenados en esta carga. */
    @Field("total_productos")
    private Integer totalProductos;
}
