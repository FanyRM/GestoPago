package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.entity.cuentas.EstatusCuenta;
import com.proyecto.servicios.entity.cuentas.TipoCuenta;
import com.proyecto.servicios.entity.gestopago.EstatusTransaccion;
import com.proyecto.servicios.entity.gestopago.TransaccionServicio;
import com.proyecto.servicios.entity.seguridad.Usuario;
import com.proyecto.servicios.exception.DuplicateResourceException;
import com.proyecto.servicios.exception.OperacionRechazadaProveedorException;
import com.proyecto.servicios.exception.ReglaNegocioException;
import com.proyecto.servicios.exception.TransaccionTimeoutConciliacionException;
import com.proyecto.servicios.model.PageResponse;
import com.proyecto.servicios.model.gestopago.catalogo.ProductoXml;
import com.proyecto.servicios.model.gestopago.sendtx.GestoPagoTxResult;
import com.proyecto.servicios.model.gestopago.sendtx.GestoPagoTxResult.EstadoProveedor;
import com.proyecto.servicios.model.gestopago.sendtx.HistorialCompraResponse;
import com.proyecto.servicios.model.gestopago.sendtx.PagoServicioResponse;
import com.proyecto.servicios.model.gestopago.sendtx.SendTx;
import com.proyecto.servicios.model.gestopago.sendtx.SendTxRequest;
import com.proyecto.servicios.repositorys.gestopago.TransaccionServicioRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import com.proyecto.servicios.security.AuthenticatedUserService;
import com.proyecto.servicios.service.CatalogoProductoService;
import com.proyecto.servicios.service.GestoPagoSendTxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas de PagoServicioServiceImpl")
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

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private PagoServicioServiceImpl servicio;

    private Usuario usuarioCliente;
    private Cliente cliente;
    private ProductoXml productoRecarga;
    private ProductoXml productoServicio;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(servicio, "unidadDistribuidor", "GPS83-TPV-17");
        ReflectionTestUtils.setField(servicio, "montoMinimoPermitido", new BigDecimal("1.00"));
        ReflectionTestUtils.setField(servicio, "montoMaximoPermitido", new BigDecimal("50000.00"));
        ReflectionTestUtils.setField(servicio, "conciliacionInmediata", true);
        ReflectionTestUtils.setField(servicio, "maxIntentosConciliacion", 5);

        cliente = new Cliente();
        cliente.setId(10);
        cliente.setActivo(true);
        cliente.setTelefonoMovil(1234567890);

        usuarioCliente = new Usuario();
        usuarioCliente.setId(1L);
        usuarioCliente.setCorreo("cliente@banco.com");
        usuarioCliente.setPasswordHash("hash");
        usuarioCliente.setRol(AuthenticatedUserService.ROLE_CLIENTE);
        usuarioCliente.setActivo(true);
        usuarioCliente.setCliente(cliente);

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
        productoServicio.setPrecio("0.00");
        productoServicio.setTipoFront("2");
    }

    private TransaccionServicio transaccionMock(EstatusTransaccion estatus) {
        TransaccionServicio tx = new TransaccionServicio();
        tx.setId(1L);
        tx.setUpc("9988776655");
        tx.setNumeroCuenta("1234567890");
        tx.setIdServicio(2284);
        tx.setNombreServicio("Telcel");
        tx.setIdProducto(14302);
        tx.setNombreProducto("Telcel $100");
        tx.setMonto(new BigDecimal("100.00"));
        tx.setSaldoAnterior(new BigDecimal("1000.00"));
        tx.setSaldoPosterior(new BigDecimal("900.00"));
        tx.setTelefono("5512345678");
        tx.setEstatus(estatus);
        tx.setHoraLocal("20261020120000");
        tx.setCliente(cliente);
        tx.setFechaCreacion(LocalDateTime.now());
        return tx;
    }

    @Test
    @DisplayName("Flujo completo exitoso: valida catalogo, descuenta saldo, envia a GestoPago y confirma EXITOSA")
    void procesarPagoServicio_exitoCompleto() {
        when(authenticatedUserService.obtenerUsuarioAutenticadoActivo()).thenReturn(usuarioCliente);
        when(authenticatedUserService.esCliente(usuarioCliente)).thenReturn(true);
        when(catalogoProductoService.validarYObtenerProducto(2284, 14302)).thenReturn(productoRecarga);

        TransaccionServicio enProceso = transaccionMock(EstatusTransaccion.EN_PROCESO);
        when(persistenceService.reservarYDebitarSaldo(any(), anyString(), anyString(), any(), anyInt(), anyInt(), any(), anyString(), any()))
                .thenReturn(enProceso);

        GestoPagoTxResult aprobado = GestoPagoTxResult.builder()
                .estado(EstadoProveedor.APROBADA)
                .codigo("01")
                .mensaje("Operacion realizada con exito")
                .idTx("TX-900")
                .numAutorizacion("AUT-777")
                .build();
        when(gestoPagoSendTxService.ejecutarTransaccion(any(SendTx.class))).thenReturn(aprobado);

        TransaccionServicio confirmada = transaccionMock(EstatusTransaccion.EXITOSA);
        confirmada.setFolioProveedor("TX-900");
        confirmada.setNumAutorizacion("AUT-777");
        when(persistenceService.confirmarExito(1L, aprobado)).thenReturn(confirmada);

        SendTxRequest req = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("100.00"))
                .telefono("5512345678")
                .build();

        PagoServicioResponse resp = servicio.procesarPagoServicio(req, null);

        assertEquals(EstatusTransaccion.EXITOSA, resp.getEstatus());
        assertEquals("TX-900", resp.getFolioProveedor());
        assertEquals("AUT-777", resp.getNumAutorizacion());
        verify(gestoPagoSendTxService, times(1)).ejecutarTransaccion(any(SendTx.class));
        verify(persistenceService, never()).reversarCargo(anyLong(), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Si se omite numeroCuenta, resuelve automaticamente la cuenta de ahorro del cliente")
    void procesarPagoServicio_sinNumeroCuenta_resuelveCuentaAhorro() {
        when(authenticatedUserService.obtenerUsuarioAutenticadoActivo()).thenReturn(usuarioCliente);
        when(authenticatedUserService.esCliente(usuarioCliente)).thenReturn(true);

        Cuenta cuentaAhorro = new Cuenta();
        cuentaAhorro.setId(1);
        cuentaAhorro.setNumeroCuenta("1234567890");
        cuentaAhorro.setTipoCuenta(TipoCuenta.AHORRO);
        cuentaAhorro.setEstatus(EstatusCuenta.ACTIVA);
        cuentaAhorro.setCliente(cliente);
        when(cuentaRepository.findByClienteId(10)).thenReturn(List.of(cuentaAhorro));
        when(catalogoProductoService.validarYObtenerProducto(2284, 14302)).thenReturn(productoRecarga);

        TransaccionServicio enProceso = transaccionMock(EstatusTransaccion.EN_PROCESO);
        when(persistenceService.reservarYDebitarSaldo(any(), anyString(), anyString(), any(), anyInt(), anyInt(), any(), anyString(), any()))
                .thenReturn(enProceso);
        GestoPagoTxResult aprobado = GestoPagoTxResult.builder()
                .estado(EstadoProveedor.APROBADA)
                .codigo("01")
                .mensaje("OK")
                .build();
        when(gestoPagoSendTxService.ejecutarTransaccion(any())).thenReturn(aprobado);
        when(persistenceService.confirmarExito(1L, aprobado)).thenReturn(transaccionMock(EstatusTransaccion.EXITOSA));

        SendTxRequest req = SendTxRequest.builder()
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("100.00"))
                .telefono("5512345678")
                .build();

        PagoServicioResponse resp = servicio.procesarPagoServicio(req, null);
        assertEquals("1234567890", resp.getNumeroCuenta());
        verify(cuentaRepository).findByClienteId(10);
    }

    @Test
    @DisplayName("ROLE_ADMIN no puede realizar compras de servicios (403 AccessDeniedException)")
    void procesarPagoServicio_adminBloqueado_lanzaAccessDenied() {
        Usuario admin = new Usuario();
        admin.setId(99L);
        admin.setCorreo("admin@banco.com");
        admin.setRol(AuthenticatedUserService.ROLE_ADMIN);
        admin.setActivo(true);
        when(authenticatedUserService.obtenerUsuarioAutenticadoActivo()).thenReturn(admin);
        when(authenticatedUserService.esCliente(admin)).thenReturn(false);

        SendTxRequest req = SendTxRequest.builder()
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("100.00"))
                .telefono("5512345678")
                .build();

        assertThrows(AccessDeniedException.class, () -> servicio.procesarPagoServicio(req, null));
        verify(persistenceService, never()).reservarYDebitarSaldo(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Bloquea manipulacion de monto cuando no coincide con el precio del producto en catalogo")
    void procesarPagoServicio_montoManipulado_rechazaAntesDeDebitar() {
        when(authenticatedUserService.obtenerUsuarioAutenticadoActivo()).thenReturn(usuarioCliente);
        when(authenticatedUserService.esCliente(usuarioCliente)).thenReturn(true);
        when(catalogoProductoService.validarYObtenerProducto(2284, 14302)).thenReturn(productoRecarga);

        SendTxRequest req = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("10.00"))
                .telefono("5512345678")
                .build();

        assertThrows(ReglaNegocioException.class, () -> servicio.procesarPagoServicio(req, null));
        verify(persistenceService, never()).reservarYDebitarSaldo(any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(gestoPagoSendTxService, never()).ejecutarTransaccion(any());
    }

    @Test
    @DisplayName("Rechazo explicito del proveedor reversa el saldo del cliente automaticamente")
    void procesarPagoServicio_rechazoProveedor_reversaSaldo() {
        when(authenticatedUserService.obtenerUsuarioAutenticadoActivo()).thenReturn(usuarioCliente);
        when(authenticatedUserService.esCliente(usuarioCliente)).thenReturn(true);
        when(catalogoProductoService.validarYObtenerProducto(2284, 14302)).thenReturn(productoRecarga);
        TransaccionServicio enProceso = transaccionMock(EstatusTransaccion.EN_PROCESO);
        when(persistenceService.reservarYDebitarSaldo(any(), anyString(), anyString(), any(), anyInt(), anyInt(), any(), anyString(), any()))
                .thenReturn(enProceso);

        GestoPagoTxResult rechazado = GestoPagoTxResult.builder()
                .estado(EstadoProveedor.RECHAZADA_PROVEEDOR)
                .codigo("05")
                .mensaje("Linea suspendida")
                .build();
        when(gestoPagoSendTxService.ejecutarTransaccion(any())).thenReturn(rechazado);

        SendTxRequest req = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("100.00"))
                .telefono("5512345678")
                .build();

        assertThrows(OperacionRechazadaProveedorException.class, () -> servicio.procesarPagoServicio(req, null));
        verify(persistenceService).reversarCargo(
                eq(1L), eq(EstatusTransaccion.RECHAZADA), eq("05"), eq("Linea suspendida"));
    }

    @Test
    @DisplayName("Timeout en sendTx consulta conciliacion y mantiene PENDIENTE_CONCILIACION")
    void procesarPagoServicio_timeout_noReintentaCargoYMarcaConciliacion() {
        when(authenticatedUserService.obtenerUsuarioAutenticadoActivo()).thenReturn(usuarioCliente);
        when(authenticatedUserService.esCliente(usuarioCliente)).thenReturn(true);
        when(catalogoProductoService.validarYObtenerProducto(2284, 14302)).thenReturn(productoRecarga);
        TransaccionServicio enProceso = transaccionMock(EstatusTransaccion.EN_PROCESO);
        when(persistenceService.reservarYDebitarSaldo(any(), anyString(), anyString(), any(), anyInt(), anyInt(), any(), anyString(), any()))
                .thenReturn(enProceso);

        GestoPagoTxResult timeoutResult = GestoPagoTxResult.builder()
                .estado(EstadoProveedor.PENDIENTE_PROVEEDOR)
                .codigo("TIMEOUT")
                .mensaje("Read timed out")
                .build();
        when(gestoPagoSendTxService.ejecutarTransaccion(any())).thenReturn(timeoutResult);

        TransaccionServicio pendiente = transaccionMock(EstatusTransaccion.PENDIENTE_CONCILIACION);
        when(persistenceService.marcarPendienteConciliacion(eq(1L), anyString(), anyString(), anyBoolean()))
                .thenReturn(pendiente);
        when(gestoPagoSendTxService.verificarTransaccion(any())).thenReturn(timeoutResult);

        SendTxRequest req = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("100.00"))
                .telefono("5512345678")
                .build();

        TransaccionTimeoutConciliacionException ex = assertThrows(
                TransaccionTimeoutConciliacionException.class,
                () -> servicio.procesarPagoServicio(req, null)
        );

        assertEquals("9988776655", ex.getUpc());
        assertEquals(EstatusTransaccion.PENDIENTE_CONCILIACION, ex.getEstatus());
        verify(gestoPagoSendTxService, times(1)).ejecutarTransaccion(any());
        verify(gestoPagoSendTxService, times(1)).verificarTransaccion(any());
        verify(persistenceService, never()).reversarCargo(anyLong(), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Idempotency-Key repetida retorna operacion exitosa previa")
    void procesarPagoServicio_claveIdempotenteRepetida_noCobraDoble() {
        when(authenticatedUserService.obtenerUsuarioAutenticadoActivo()).thenReturn(usuarioCliente);
        when(authenticatedUserService.esCliente(usuarioCliente)).thenReturn(true);
        TransaccionServicio previa = transaccionMock(EstatusTransaccion.EXITOSA);
        previa.setCliente(cliente);
        previa.setFolioProveedor("TX-111");
        when(persistenceService.buscarPorClaveIdempotencia("idem-unica-001")).thenReturn(Optional.of(previa));

        SendTxRequest req = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("100.00"))
                .telefono("5512345678")
                .build();

        PagoServicioResponse resp = servicio.procesarPagoServicio(req, "idem-unica-001");

        assertTrue(resp.getRespuestaIdempotente());
        assertEquals("TX-111", resp.getFolioProveedor());
        verify(persistenceService, never()).reservarYDebitarSaldo(any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(gestoPagoSendTxService, never()).ejecutarTransaccion(any());
    }

    @Test
    @DisplayName("Idempotency-Key reutilizada con monto distinto lanza DuplicateResourceException (409)")
    void procesarPagoServicio_idempotencyKeyParametrosDistintos_lanzaConflicto() {
        when(authenticatedUserService.obtenerUsuarioAutenticadoActivo()).thenReturn(usuarioCliente);
        when(authenticatedUserService.esCliente(usuarioCliente)).thenReturn(true);
        TransaccionServicio previa = transaccionMock(EstatusTransaccion.EXITOSA);
        previa.setCliente(cliente);
        when(persistenceService.buscarPorClaveIdempotencia("idem-unica-001")).thenReturn(Optional.of(previa));

        SendTxRequest reqManipulado = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("200.00"))
                .telefono("5512345678")
                .build();

        assertThrows(DuplicateResourceException.class,
                () -> servicio.procesarPagoServicio(reqManipulado, "idem-unica-001"));
    }

    @Test
    @DisplayName("Conciliacion confirma rechazo del proveedor y restituye el saldo (REVERSADA)")
    void conciliarTransaccion_proveedorConfirmaNoAplicada_reversaSaldo() {
        when(authenticatedUserService.obtenerUsuarioAutenticadoActivo()).thenReturn(usuarioCliente);
        TransaccionServicio pendiente = transaccionMock(EstatusTransaccion.PENDIENTE_CONCILIACION);
        pendiente.setCliente(cliente);
        when(transaccionRepository.findByUpc("9988776655")).thenReturn(Optional.of(pendiente));

        GestoPagoTxResult noExiste = GestoPagoTxResult.builder()
                .estado(EstadoProveedor.RECHAZADA_PROVEEDOR)
                .codigo("14")
                .mensaje("Transaccion no registrada en proveedor")
                .build();
        when(gestoPagoSendTxService.verificarTransaccion(any())).thenReturn(noExiste);

        TransaccionServicio reversada = transaccionMock(EstatusTransaccion.REVERSADA);
        when(persistenceService.reversarCargo(eq(1L), eq(EstatusTransaccion.REVERSADA), eq("14"), anyString()))
                .thenReturn(reversada);

        PagoServicioResponse resp = servicio.conciliarTransaccion("9988776655");
        assertEquals(EstatusTransaccion.REVERSADA, resp.getEstatus());
        verify(persistenceService).reversarCargo(eq(1L), eq(EstatusTransaccion.REVERSADA), eq("14"), anyString());
    }

    @Test
    @DisplayName("Consulta UPC de transaccion ajena devuelve 403 AccessDenied")
    void consultarTransaccionPorUpc_otroCliente_lanzaAccessDenied() {
        when(authenticatedUserService.obtenerUsuarioAutenticadoActivo()).thenReturn(usuarioCliente);
        when(authenticatedUserService.esAdmin(usuarioCliente)).thenReturn(false);

        Cliente otroCliente = new Cliente();
        otroCliente.setId(999);
        TransaccionServicio txAjena = transaccionMock(EstatusTransaccion.EXITOSA);
        txAjena.setCliente(otroCliente);
        when(transaccionRepository.findByUpc("9988776655")).thenReturn(Optional.of(txAjena));

        assertThrows(AccessDeniedException.class, () -> servicio.consultarTransaccionPorUpc("9988776655"));
    }

    @Test
    @DisplayName("Consulta historial con cuenta ajena lanza AccessDeniedException")
    void obtenerHistorialCompras_cuentaAjena_lanzaAccessDenied() {
        when(authenticatedUserService.obtenerUsuarioAutenticadoActivo()).thenReturn(usuarioCliente);
        when(authenticatedUserService.esCliente(usuarioCliente)).thenReturn(true);

        Cliente ajeno = new Cliente();
        ajeno.setId(999);
        Cuenta cuentaAjena = new Cuenta();
        cuentaAjena.setNumeroCuenta("9999999999");
        cuentaAjena.setCliente(ajeno);
        when(cuentaRepository.findWithClienteByNumeroCuenta("9999999999")).thenReturn(Optional.of(cuentaAjena));

        Pageable p = PageRequest.of(0, 10);
        assertThrows(AccessDeniedException.class,
                () -> servicio.obtenerHistorialCompras(null, null, "9999999999", null, p));
    }

    @Test
    @DisplayName("Consulta historial del cliente exitosa con paginacion PageResponse")
    void obtenerHistorialCompras_exitoso() {
        when(authenticatedUserService.obtenerUsuarioAutenticadoActivo()).thenReturn(usuarioCliente);
        when(authenticatedUserService.esCliente(usuarioCliente)).thenReturn(true);

        TransaccionServicio tx = transaccionMock(EstatusTransaccion.EXITOSA);
        when(transaccionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tx), PageRequest.of(0, 10), 1));

        PageResponse<HistorialCompraResponse> response = servicio.obtenerHistorialCompras(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), null, EstatusTransaccion.EXITOSA, PageRequest.of(0, 10)
        );

        assertNotNull(response);
        assertEquals(1, response.getTotalElementos());
        assertEquals("9988776655", response.getContenido().get(0).getUpc());
    }
}
