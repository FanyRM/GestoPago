package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.catalogo.CatalogoProductoResponse;
import com.proyecto.servicios.model.gestopago.catalogo.ProductoXml;

/**
 * Contrato del servicio de catálogo de productos de GestoPago.
 */
public interface CatalogoProductoService {

    /**
     * Ejecuta la sincronización del catálogo de productos con el servicio externo de GestoPago.
     */
    void sincronizarCatalogo();

    /**
     * Consulta el catálogo de productos almacenado en la caché de MongoDB.
     */
    CatalogoProductoResponse obtenerCatalogo();

    /**
     * Valida la existencia del servicio y del producto en el catálogo en caché de MongoDB
     * y retorna la definición del producto para validar restricciones operativas y precios.
     */
    ProductoXml validarYObtenerProducto(Integer idServicio, Integer idProducto);
}
