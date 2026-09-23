package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoCatProduct;
import com.proyecto.servicios.enums.ApiResponseEnum;
import com.proyecto.servicios.exception.CatalogoException;
import com.proyecto.servicios.model.gestopago.catalogo.CatalogoCacheDto;
import com.proyecto.servicios.model.gestopago.catalogo.CatalogoProductoResponse;
import com.proyecto.servicios.model.gestopago.catalogo.GestoPagoCatProductResponse;
import com.proyecto.servicios.model.gestopago.catalogo.ProductoXml;
import com.proyecto.servicios.repositorys.mongo.CatalogoProductoRepository;
import com.proyecto.servicios.service.CatalogoProductoService;
import com.proyecto.servicios.service.GestoPagoTokenService;
import feign.FeignException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Implementación del servicio de catálogo de productos de GestoPago.
 *
 * <p>Responsabilidades principales:</p>
 * <ul>
 *   <li>Obtener el Bearer Token activo desde {@link GestoPagoTokenService}.</li>
 *   <li>Llamar al endpoint externo {@code GET /sistema/service/getProductList.do}.</li>
 *   <li>Deserializar el XML de respuesta con JAXB.</li>
 *   <li>Persistir el resultado en MongoDB como caché diaria.</li>
 *   <li>Implementar política de reintentos automáticos.</li>
 *   <li>Consultar la caché MongoDB cuando el cliente lo solicite.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogoProductoServiceImpl implements CatalogoProductoService {

    private static final int MAX_REINTENTOS = 3;
    private static final long ESPERA_ENTRE_REINTENTOS_MS = 2000L;

    private final GestoPagoCatProduct catProductClient;
    private final CatalogoProductoRepository catalogoRepository;
    private final GestoPagoTokenService gestoPagoTokenService;

    @Value("${gestopago.auth.id-distribuidor}")
    private Integer idDistribuidor;

    @Value("${gestopago.auth.codigo-dispositivo}")
    private String codigoDispositivo;

    // =========================================================================
    // Sincronización (invocada por el Cron)
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * <p>Ejecuta hasta {@value #MAX_REINTENTOS} intentos de llamada al servicio externo.
     * Si todos fallan, registra un error crítico en los logs para que los equipos
     * de operaciones puedan detectar el problema.</p>
     */
    @Override
    public void sincronizarCatalogo() {
        log.info("[CatalogoProducto] Iniciando sincronización del catálogo de productos.");

        String token = obtenerBearerToken();
        if (token == null) {
            log.error("[CatalogoProducto] No se pudo obtener token activo. Sincronización cancelada.");
            return;
        }

        String xmlRespuesta = invocarServicioConReintentos(token);

        if (xmlRespuesta == null) {
            log.error("[CatalogoProducto] ALERTA CRÍTICA: El catálogo NO pudo obtenerse " +
                      "después de {} intentos. Se requiere intervención manual.", MAX_REINTENTOS);
            return;
        }

        log.info("[CatalogoProducto] XML recibido del servicio externo (primeros 500 chars): {}",
                xmlRespuesta.length() > 500 ? xmlRespuesta.substring(0, 500) + "..." : xmlRespuesta);

        List<ProductoXml> productos = deserializarXml(xmlRespuesta);

        if (productos == null || productos.isEmpty()) {
            log.warn("[CatalogoProducto] El XML fue procesado pero no contiene productos. " +
                     "Se abortará la actualización en caché para no sobreescribir datos previos.");
            return;
        }

        almacenarEnMongo(productos);
    }

    // =========================================================================
    // Consulta (invocada por el Controller)
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * @throws CatalogoException con {@link ApiResponseEnum#MONGO_NOT_FOUND} si no hay datos en caché.
     * @throws CatalogoException con {@link ApiResponseEnum#MONGO_ERROR} si falla la consulta a MongoDB.
     */
    @Override
    public CatalogoProductoResponse obtenerCatalogo() {
        log.info("[CatalogoProducto] Consultando catálogo desde caché MongoDB.");

        try {
            CatalogoCacheDto cache = catalogoRepository
                    .findTopByOrderByFechaActualizacionDesc()
                    .orElseThrow(() -> {
                        log.warn("[CatalogoProducto] No existe catálogo en MongoDB. " +
                                 "El cron aún no ha ejecutado o falló.");
                        return new CatalogoException(ApiResponseEnum.MONGO_NOT_FOUND);
                    });

            log.info("[CatalogoProducto] Catálogo encontrado en MongoDB. " +
                     "Total de productos: {}, Última actualización: {}",
                    cache.getTotalProductos(), cache.getFechaActualizacion());

            return CatalogoProductoResponse.builder()
                    .codigo(ApiResponseEnum.OK.getCodigo())
                    .mensaje(ApiResponseEnum.OK.getMensaje())
                    .totalProductos(cache.getTotalProductos())
                    .fechaActualizacion(cache.getFechaActualizacion())
                    .productos(cache.getProductos())
                    .build();

        } catch (CatalogoException e) {
            throw e;
        } catch (Exception e) {
            log.error("[CatalogoProducto] Error inesperado al consultar MongoDB: {}", e.getMessage(), e);
            throw new CatalogoException(ApiResponseEnum.MONGO_ERROR, e);
        }
    }

    // =========================================================================
    // Métodos privados de soporte
    // =========================================================================

    /**
     * Obtiene el Bearer Token activo almacenado en PostgreSQL.
     *
     * @return token con prefijo "Bearer " o {@code null} si no está disponible
     */
    private String obtenerBearerToken() {
        log.info("[CatalogoProducto] Obteniendo token activo para distribuidor={}", idDistribuidor);
        return gestoPagoTokenService
                .obtenerTokenActivo(idDistribuidor, codigoDispositivo)
                .map(t -> {
                    log.info("[CatalogoProducto] Token activo obtenido correctamente.");
                    return "Bearer " + t.getToken();
                })
                .orElseGet(() -> {
                    log.error("[CatalogoProducto] No se encontró token activo en base de datos.");
                    return null;
                });
    }

    /**
     * Invoca el servicio externo con política de reintentos.
     *
     * @param bearerToken token de autenticación
     * @return XML de respuesta o {@code null} si todos los intentos fallaron
     */
    private String invocarServicioConReintentos(String bearerToken) {
        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            log.info("[CatalogoProducto] Intento {} de {} — llamando a getProductList.", intento, MAX_REINTENTOS);
            try {
                String respuesta = catProductClient.getProductList(bearerToken);
                log.info("[CatalogoProducto] Respuesta exitosa en intento {}.", intento);
                return respuesta;

            } catch (FeignException.Unauthorized e) {
                log.error("[CatalogoProducto] Error de autenticación (401) en intento {}: {}", intento, e.getMessage());
                // No tiene sentido reintentar si el token no es válido
                return null;

            } catch (FeignException.GatewayTimeout | feign.RetryableException e) {
                log.warn("[CatalogoProducto] Timeout en intento {}: {}", intento, e.getMessage());

            } catch (FeignException e) {
                log.warn("[CatalogoProducto] Error HTTP {} en intento {}: {}",
                        e.status(), intento, e.getMessage());

            } catch (Exception e) {
                log.warn("[CatalogoProducto] Error inesperado en intento {}: {}", intento, e.getMessage());
            }

            if (intento < MAX_REINTENTOS) {
                esperarEntreReintentos();
            }
        }
        return null;
    }

    /**
     * Pausa la ejecución entre reintentos para evitar saturar el servicio externo.
     */
    private void esperarEntreReintentos() {
        try {
            log.info("[CatalogoProducto] Esperando {}ms antes del siguiente intento...", ESPERA_ENTRE_REINTENTOS_MS);
            Thread.sleep(ESPERA_ENTRE_REINTENTOS_MS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("[CatalogoProducto] Espera entre reintentos interrumpida.");
        }
    }

    /**
     * Deserializa el String XML a la clase wrapper usando JAXB.
     *
     * @param xml cadena XML recibida del servicio externo
     * @return lista de productos o lista vacía si hay error de parseo
     */
    private List<ProductoXml> deserializarXml(String xml) {
        log.info("[CatalogoProducto] Deserializando respuesta XML con JAXB.");
        try {
            JAXBContext context = JAXBContext.newInstance(GestoPagoCatProductResponse.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            GestoPagoCatProductResponse response =
                    (GestoPagoCatProductResponse) unmarshaller.unmarshal(new StringReader(xml));

            if (response.getMensaje() != null) {
                log.info("[CatalogoProducto] Mensaje GestoPago: Codigo={}, Texto={}", 
                         response.getMensaje().getCodigo(), response.getMensaje().getTexto());
            }

            List<ProductoXml> productos = (response.getProductos() != null) ? response.getProductos().getProductos() : null;
            int total = (productos != null) ? productos.size() : 0;
            log.info("[CatalogoProducto] XML deserializado correctamente. Productos encontrados: {}", total);
            return productos != null ? productos : Collections.emptyList();

        } catch (JAXBException e) {
            log.error("[CatalogoProducto] Error al deserializar el XML de productos: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Persiste la lista de productos en MongoDB, reemplazando el catálogo anterior.
     *
     * @param productos lista de productos a almacenar
     */
    private void almacenarEnMongo(List<ProductoXml> productos) {
        log.info("[CatalogoProducto] Almacenando {} productos en MongoDB (caché).", productos.size());
        try {
            catalogoRepository.deleteAll();
            log.info("[CatalogoProducto] Catálogo anterior eliminado de MongoDB.");

            CatalogoCacheDto nuevoCache = new CatalogoCacheDto();
            nuevoCache.setProductos(productos);
            nuevoCache.setFechaActualizacion(LocalDateTime.now());
            nuevoCache.setTotalProductos(productos.size());

            CatalogoCacheDto guardado = catalogoRepository.save(nuevoCache);
            log.info("[CatalogoProducto] Catálogo guardado en MongoDB con id={}, " +
                     "totalProductos={}, fechaActualizacion={}",
                    guardado.getId(), guardado.getTotalProductos(), guardado.getFechaActualizacion());

        } catch (Exception e) {
            log.error("[CatalogoProducto] Error al almacenar el catálogo en MongoDB: {}", e.getMessage(), e);
        }
    }
}
