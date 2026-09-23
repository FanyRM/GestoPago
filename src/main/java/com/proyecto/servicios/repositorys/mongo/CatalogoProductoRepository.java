package com.proyecto.servicios.repositorys.mongo;

import com.proyecto.servicios.model.gestopago.catalogo.CatalogoCacheDto;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio MongoDB para la caché del catálogo de productos de GestoPago.
 *
 * <p>La colección {@code catalogo_productos} actúa como caché diaria.
 * Solo se mantiene el documento más reciente.</p>
 */
@Repository
public interface CatalogoProductoRepository extends MongoRepository<CatalogoCacheDto, String> {

    /**
     * Recupera el documento de catálogo más reciente basándose en la fecha de actualización.
     *
     * @return el catálogo más reciente, o {@link Optional#empty()} si la colección está vacía
     */
    Optional<CatalogoCacheDto> findTopByOrderByFechaActualizacionDesc();
}
