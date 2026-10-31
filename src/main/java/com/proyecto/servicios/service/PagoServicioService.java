package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.gestopago.EstatusTransaccion;
import com.proyecto.servicios.model.gestopago.sendtx.HistorialCompraResponse;
import com.proyecto.servicios.model.gestopago.sendtx.PagoServicioResponse;
import com.proyecto.servicios.model.gestopago.sendtx.SendTxRequest;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface PagoServicioService {

    PagoServicioResponse procesarPagoServicio(SendTxRequest request, String headerIdempotencyKey);

    PagoServicioResponse consultarTransaccionPorUpc(String upc);

    PagoServicioResponse conciliarTransaccion(String upc);

    void ejecutarJobConciliacionPendientes();

    Page<HistorialCompraResponse> obtenerHistorialCompras(
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String numeroCuenta,
            EstatusTransaccion estatus,
            int page,
            int size,
            String sortBy,
            String sortDir);
}
