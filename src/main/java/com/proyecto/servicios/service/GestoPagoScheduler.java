package com.proyecto.servicios.service;

import com.proyecto.servicios.service.CatalogoProductoService;
import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GestoPagoScheduler {

    private final CatalogoProductoService catalocoProductoService;

    @Scheduled(cron = "0 0 6 * * *", zone = "America/Mexico_City")
    public void sincronizarCatalogoDiario() {
        try {
            catalocoProductoService.sincronizarCatalogo();
        } catch (Exception e) {
            // Ignore
        }
    }
}
