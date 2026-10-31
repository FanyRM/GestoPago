package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.sendtx.GestoPagoTxResult;
import com.proyecto.servicios.model.gestopago.sendtx.SendTx;

public interface GestoPagoSendTxService {

    String sendTx(SendTx request);

    GestoPagoTxResult ejecutarTransaccion(SendTx request);

    GestoPagoTxResult verificarTransaccion(SendTx request);
}
