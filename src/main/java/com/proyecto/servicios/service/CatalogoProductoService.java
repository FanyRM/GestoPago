package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.catalogo.CatalogoProductoResponse;

/**
 * Contrato del servicio de catálogo de productos de GestoPago.
 *
 * <p>Define las dos operaciones principales:</p>
 * <ul>
 *   <li>Sincronización con el servicio externo y almacenamiento en MongoDB.</li>
 *   <li>Consulta del catálogo desde la caché MongoDB.</li>
 * </ul>
 */
public interface CatalogoProductoService {

    /**
     * Ejecuta la sincronización del catálogo de productos con el servicio externo de GestoPago.
     * Incluye lógica de reintentos y almacenamiento en MongoDB.
     *
     * <p>Este método es invocado por el cron job diario a las 06:00 AM (hora México).</p>
     */
    void sincronizarCatalogo();

    /**
     * Consulta el catálogo de productos almacenado en la caché de MongoDB.
     *
     * @return {@link CatalogoProductoResponse} con la lista de productos y metadatos
     * @throws com.proyecto.servicios.exception.CatalogoException si no hay datos en caché
     *         o si ocurre un error al consultar MongoDB
     */
    CatalogoProductoResponse obtenerCatalogo();
}
