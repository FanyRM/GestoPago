package com.proyecto.servicios.service;

import com.proyecto.servicios.service.CatalogoProductoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Componente responsable de las tareas programadas relacionadas con GestoPago.
 *
 * <p>Actualmente gestiona la sincronización diaria del catálogo de productos.
 * La renovación del token se maneja en {@link GestoPagoTokenService} con su propio scheduler.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GestoPagoScheduler {

    private final CatalogoProductoService catalocoProductoService;

    /**
     * Tarea programada que sincroniza el catálogo de productos de GestoPago con MongoDB.
     *
     * <p>Se ejecuta todos los días a las <b>06:00 AM hora México</b>
     * (zona horaria {@code America/Mexico_City}).</p>
     *
     * <p>El flujo de sincronización incluye:</p>
     * <ol>
     *   <li>Obtención del Bearer Token activo desde la base de datos.</li>
     *   <li>Llamada al endpoint externo {@code GET /sistema/service/getProductList.do}.</li>
     *   <li>Deserialización del XML con JAXB.</li>
     *   <li>Almacenamiento en MongoDB como caché diaria.</li>
     *   <li>Reintento automático hasta 3 veces ante fallos del servicio externo.</li>
     * </ol>
     */
    @Scheduled(cron = "0 0 6 * * *", zone = "America/Mexico_City")
    public void sincronizarCatalogoDiario() {
        log.info("[GestoPagoScheduler] ===== Inicio de tarea programada: Sincronización catálogo (06:00 AM) =====");
        try {
            catalocoProductoService.sincronizarCatalogo();
            log.info("[GestoPagoScheduler] ===== Tarea programada finalizada correctamente =====");
        } catch (Exception e) {
            log.error("[GestoPagoScheduler] Error no controlado en la tarea programada: {}", e.getMessage(), e);
        }
    }
}
