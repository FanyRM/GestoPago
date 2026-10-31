package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.gestopago.EstatusTransaccion;
import com.proyecto.servicios.entity.gestopago.TransaccionServicio;
import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.exception.OperacionRechazadaProveedorException;
import com.proyecto.servicios.exception.ReglaNegocioException;
import com.proyecto.servicios.exception.TransaccionTimeoutConciliacionException;
import com.proyecto.servicios.model.gestopago.catalogo.ProductoXml;
import com.proyecto.servicios.model.gestopago.sendtx.GestoPagoTxResult;
import com.proyecto.servicios.model.gestopago.sendtx.GestoPagoTxResult.EstadoProveedor;
import com.proyecto.servicios.model.gestopago.sendtx.PagoServicioResponse;
import com.proyecto.servicios.model.gestopago.sendtx.SendTx;
import com.proyecto.servicios.model.gestopago.sendtx.SendTxRequest;
import com.proyecto.servicios.repositorys.gestopago.TransaccionServicioRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.service.CatalogoProductoService;
import com.proyecto.servicios.service.GestoPagoSendTxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de PagoServicioServiceImpl")
class PagoServicioServiceImplTest {

    @Mock
    private CatalogoProductoService catalogoProductoService;

    @Mock
    private TransaccionPersistenceService persistenceService;

    @Mock
    private GestoPagoSendTxService gestoPagoSendTxService;

    @Mock
    private TransaccionServicioRepository transaccionRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private PagoServicioServiceImpl servicio;

    private ProductoXml productoRecarga;
    private ProductoXml productoServicio;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(servicio, "unidadDistribuidor", "GPS83-TPV-17");
        ReflectionTestUtils.setField(servicio, "montoMinimoPermitido", new BigDecimal("1.00"));
        ReflectionTestUtils.setField(servicio, "montoMaximoPermitido", new BigDecimal("50000.00"));
        ReflectionTestUtils.setField(servicio, "conciliacionInmediata", true);
        ReflectionTestUtils.setField(servicio, "maxIntentosConciliacion", 5);

        productoRecarga = new ProductoXml();
        productoRecarga.setIdServicio("2284");
        productoRecarga.setIdProducto("14302");
        productoRecarga.setServicio("Telcel");
        productoRecarga.setNombreProducto("Telcel $100");
        productoRecarga.setPrecio("100.00");
        productoRecarga.setTipoFront("1");

        productoServicio = new ProductoXml();
        productoServicio.setIdServicio("500");
        productoServicio.setIdProducto("5001");
        productoServicio.setServicio("CFE");
        productoServicio.setNombreProducto("CFE Suministrador");
        productoServicio.setPrecio("0.00"); // Monto abierto
        productoServicio.setTipoFront("2");
    }

    @Test
    @DisplayName("Debe procesar exitosamente una recarga telefonica cuando los datos son validos")
    void procesarPago_exitoso() {
        SendTxRequest request = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("100.00"))
                .telefono("5512345678")
                .claveIdempotencia("tx-test-0001")
                .build();

        TransaccionServicio txEnProceso = new TransaccionServicio();
        txEnProceso.setId(100L);
        txEnProceso.setUpc("123456789012");
        txEnProceso.setNumeroCuenta("1234567890");
        txEnProceso.setIdServicio(2284);
        txEnProceso.setNombreServicio("Telcel");
        txEnProceso.setIdProducto(14302);
        txEnProceso.setNombreProducto("Telcel $100");
        txEnProceso.setMonto(new BigDecimal("100.00"));
        txEnProceso.setSaldoAnterior(new BigDecimal("500.00"));
        txEnProceso.setSaldoPosterior(new BigDecimal("400.00"));
        txEnProceso.setTelefono("5512345678");
        txEnProceso.setHoraLocal("20261030120000");
        txEnProceso.setEstatus(EstatusTransaccion.EN_PROCESO);
        txEnProceso.setFechaCreacion(LocalDateTime.now());

        TransaccionServicio txExitosa = new TransaccionServicio();
        txExitosa.setId(100L);
        txExitosa.setUpc("123456789012");
        txExitosa.setNumeroCuenta("1234567890");
        txExitosa.setIdServicio(2284);
        txExitosa.setNombreServicio("Telcel");
        txExitosa.setIdProducto(14302);
        txExitosa.setNombreProducto("Telcel $100");
        txExitosa.setMonto(new BigDecimal("100.00"));
        txExitosa.setSaldoPosterior(new BigDecimal("400.00"));
        txExitosa.setEstatus(EstatusTransaccion.EXITOSA);
        txExitosa.setFolioProveedor("FOL-9988");
        txExitosa.setNumAutorizacion("AUTH-1234");
        txExitosa.setMensajeProveedor("Transaccion Aprobada");
        txExitosa.setFechaCreacion(LocalDateTime.now());

        when(persistenceService.buscarPorClaveIdempotencia("tx-test-0001")).thenReturn(Optional.empty());
        when(catalogoProductoService.validarYObtenerProducto(2284, 14302)).thenReturn(productoRecarga);
        when(persistenceService.reservarYDebitarSaldo(any(), eq("1234567890"), eq("tx-test-0001"), eq(productoRecarga),
                eq(2284), eq(14302), eq(new BigDecimal("100.00")), eq("5512345678"), any()))
                .thenReturn(txEnProceso);

        GestoPagoTxResult resultadoAprobado = GestoPagoTxResult.builder()
                .estado(EstadoProveedor.APROBADA)
                .codigo("00")
                .mensaje("Transaccion Aprobada")
                .idTx("FOL-9988")
                .numAutorizacion("AUTH-1234")
                .build();

        when(gestoPagoSendTxService.ejecutarTransaccion(any(SendTx.class))).thenReturn(resultadoAprobado);
        when(persistenceService.confirmarExito(eq(100L), eq(resultadoAprobado))).thenReturn(txExitosa);

        PagoServicioResponse response = servicio.procesarPagoServicio(request, null);

        assertNotNull(response);
        assertEquals(EstatusTransaccion.EXITOSA, response.getEstatus());
        assertEquals("123456789012", response.getUpc());
        assertEquals(new BigDecimal("400.00"), response.getSaldoDisponible());
        assertEquals("AUTH-1234", response.getNumAutorizacion());
        verify(persistenceService).confirmarExito(100L, resultadoAprobado);
        verify(persistenceService, never()).reversarCargo(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Debe rechazar la transaccion si el monto no coincide con el precio fijo del catalogo")
    void procesarPago_montoManipulado() {
        SendTxRequest request = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("50.00")) // Catalogo exige 100.00
                .telefono("5512345678")
                .claveIdempotencia("tx-test-0002")
                .build();

        when(persistenceService.buscarPorClaveIdempotencia("tx-test-0002")).thenReturn(Optional.empty());
        when(catalogoProductoService.validarYObtenerProducto(2284, 14302)).thenReturn(productoRecarga);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                servicio.procesarPagoServicio(request, null));

        assertTrue(ex.getMessage().contains("no corresponde al precio autorizado"));
        verify(persistenceService, never()).reservarYDebitarSaldo(any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(gestoPagoSendTxService, never()).ejecutarTransaccion(any());
    }

    @Test
    @DisplayName("Debe rechazar la transaccion si no incluye telefono de 10 digitos para recarga")
    void procesarPago_telefonoInvalido() {
        SendTxRequest request = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("100.00"))
                .telefono("55123") // Menor a 10 digitos
                .claveIdempotencia("tx-test-0003")
                .build();

        when(persistenceService.buscarPorClaveIdempotencia("tx-test-0003")).thenReturn(Optional.empty());
        when(catalogoProductoService.validarYObtenerProducto(2284, 14302)).thenReturn(productoRecarga);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                servicio.procesarPagoServicio(request, null));

        assertTrue(ex.getMessage().contains("10 digitos"));
        verify(persistenceService, never()).reservarYDebitarSaldo(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Debe rechazar si falta la referencia en pago de servicio")
    void procesarPago_faltaReferencia() {
        SendTxRequest request = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(500)
                .idProducto(5001)
                .montoPago(new BigDecimal("250.00"))
                .referencia("12") // Menor a 4 caracteres
                .claveIdempotencia("tx-test-0004")
                .build();

        when(persistenceService.buscarPorClaveIdempotencia("tx-test-0004")).thenReturn(Optional.empty());
        when(catalogoProductoService.validarYObtenerProducto(500, 5001)).thenReturn(productoServicio);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () ->
                servicio.procesarPagoServicio(request, null));

        assertTrue(ex.getMessage().contains("referencia de pago valida"));
        verify(persistenceService, never()).reservarYDebitarSaldo(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Debe restituir el saldo cuando el proveedor rechaza con certeza la operacion")
    void procesarPago_rechazoProveedor_reversaSaldo() {
        SendTxRequest request = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("100.00"))
                .telefono("5512345678")
                .claveIdempotencia("tx-test-0005")
                .build();

        TransaccionServicio txEnProceso = new TransaccionServicio();
        txEnProceso.setId(101L);
        txEnProceso.setUpc("123456789013");
        txEnProceso.setNumeroCuenta("1234567890");
        txEnProceso.setIdServicio(2284);
        txEnProceso.setIdProducto(14302);
        txEnProceso.setMonto(new BigDecimal("100.00"));
        txEnProceso.setEstatus(EstatusTransaccion.EN_PROCESO);

        when(persistenceService.buscarPorClaveIdempotencia("tx-test-0005")).thenReturn(Optional.empty());
        when(catalogoProductoService.validarYObtenerProducto(2284, 14302)).thenReturn(productoRecarga);
        when(persistenceService.reservarYDebitarSaldo(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(txEnProceso);

        GestoPagoTxResult resultadoRechazado = GestoPagoTxResult.builder()
                .estado(EstadoProveedor.RECHAZADA_PROVEEDOR)
                .codigo("99")
                .mensaje("Numero telefonico suspendido o no valido")
                .build();

        when(gestoPagoSendTxService.ejecutarTransaccion(any())).thenReturn(resultadoRechazado);

        OperacionRechazadaProveedorException ex = assertThrows(OperacionRechazadaProveedorException.class, () ->
                servicio.procesarPagoServicio(request, null));

        assertTrue(ex.getMessage().contains("restituido"));
        verify(persistenceService).reversarCargo(eq(101L), eq(EstatusTransaccion.RECHAZADA), eq("99"), anyString());
        verify(persistenceService, never()).confirmarExito(any(), any());
    }

    @Test
    @DisplayName("Ante timeout del proveedor no debe reversar saldo ciegamente sino marcar PENDIENTE_CONCILIACION")
    void procesarPago_timeoutProveedor_noReversa_quedaPendiente() {
        SendTxRequest request = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("100.00"))
                .telefono("5512345678")
                .claveIdempotencia("tx-test-0006")
                .build();

        TransaccionServicio txEnProceso = new TransaccionServicio();
        txEnProceso.setId(102L);
        txEnProceso.setUpc("123456789014");
        txEnProceso.setNumeroCuenta("1234567890");
        txEnProceso.setIdServicio(2284);
        txEnProceso.setIdProducto(14302);
        txEnProceso.setMonto(new BigDecimal("100.00"));
        txEnProceso.setEstatus(EstatusTransaccion.EN_PROCESO);

        TransaccionServicio txPendiente = new TransaccionServicio();
        txPendiente.setId(102L);
        txPendiente.setUpc("123456789014");
        txPendiente.setEstatus(EstatusTransaccion.PENDIENTE_CONCILIACION);

        when(persistenceService.buscarPorClaveIdempotencia("tx-test-0006")).thenReturn(Optional.empty());
        when(catalogoProductoService.validarYObtenerProducto(2284, 14302)).thenReturn(productoRecarga);
        when(persistenceService.reservarYDebitarSaldo(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(txEnProceso);

        GestoPagoTxResult resultadoTimeout = GestoPagoTxResult.builder()
                .estado(EstadoProveedor.PENDIENTE_PROVEEDOR)
                .codigo("TIMEOUT")
                .mensaje("Tiempo de espera agotado al conectar con proveedor")
                .build();

        when(gestoPagoSendTxService.ejecutarTransaccion(any())).thenReturn(resultadoTimeout);
        when(persistenceService.marcarPendienteConciliacion(eq(102L), eq("TIMEOUT"), anyString(), eq(false)))
                .thenReturn(txPendiente);

        // verifyTx aun no tiene respuesta definitiva
        when(gestoPagoSendTxService.verificarTransaccion(any())).thenReturn(resultadoTimeout);
        when(persistenceService.marcarPendienteConciliacion(eq(102L), any(), any(), eq(true)))
                .thenReturn(txPendiente);

        TransaccionTimeoutConciliacionException ex = assertThrows(TransaccionTimeoutConciliacionException.class, () ->
                servicio.procesarPagoServicio(request, null));

        assertEquals("123456789014", ex.getUpc());
        assertTrue(ex.getMessage().contains("conciliacion"));
        // Comprobamos que NO se reversó el saldo ante el timeout
        verify(persistenceService, never()).reversarCargo(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Debe responder de forma idempotente ante solicitud repetida con exito previo")
    void procesarPago_idempotencia() {
        SendTxRequest request = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("100.00"))
                .telefono("5512345678")
                .claveIdempotencia("tx-test-0007")
                .build();

        TransaccionServicio txPrevia = new TransaccionServicio();
        txPrevia.setId(103L);
        txPrevia.setUpc("123456789015");
        txPrevia.setNumeroCuenta("1234567890");
        txPrevia.setIdServicio(2284);
        txPrevia.setNombreServicio("Telcel");
        txPrevia.setIdProducto(14302);
        txPrevia.setNombreProducto("Telcel $100");
        txPrevia.setMonto(new BigDecimal("100.00"));
        txPrevia.setSaldoPosterior(new BigDecimal("900.00"));
        txPrevia.setEstatus(EstatusTransaccion.EXITOSA);
        txPrevia.setFolioProveedor("FOL-PREV");
        txPrevia.setNumAutorizacion("AUTH-PREV");
        txPrevia.setFechaCreacion(LocalDateTime.now());

        when(persistenceService.buscarPorClaveIdempotencia("tx-test-0007")).thenReturn(Optional.of(txPrevia));

        PagoServicioResponse response = servicio.procesarPagoServicio(request, null);

        assertNotNull(response);
        assertEquals(Boolean.TRUE, response.getRespuestaIdempotente());
        assertEquals("123456789015", response.getUpc());
        verify(persistenceService, never()).reservarYDebitarSaldo(any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(gestoPagoSendTxService, never()).ejecutarTransaccion(any());
    }
}
