package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.gestopago.EstatusTransaccion;
import com.proyecto.servicios.entity.gestopago.TransaccionServicio;
import com.proyecto.servicios.entity.sf.Cliente;
import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.enums.EstatusCuenta;
import com.proyecto.servicios.exception.CuentaNoOperativaException;
import com.proyecto.servicios.exception.DuplicateResourceException;
import com.proyecto.servicios.exception.ResourceNotFoundException;
import com.proyecto.servicios.exception.SaldoInsuficienteException;
import com.proyecto.servicios.model.gestopago.catalogo.ProductoXml;
import com.proyecto.servicios.model.gestopago.sendtx.GestoPagoTxResult;
import com.proyecto.servicios.repositorys.gestopago.TransaccionServicioRepository;
import com.proyecto.servicios.repositorys.sf.CuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de TransaccionPersistenceService")
class TransaccionPersistenceServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private TransaccionServicioRepository transaccionRepository;

    @InjectMocks
    private TransaccionPersistenceService persistenceService;

    private Cuenta cuenta;
    private Cliente cliente;
    private ProductoXml producto;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(persistenceService, "duplicateWindowSeconds", 60L);

        cliente = new Cliente();
        cliente.setId(1);
        cliente.setActivo(true);

        cuenta = new Cuenta();
        cuenta.setId(10);
        cuenta.setNumeroCuenta("1234567890");
        cuenta.setSaldo(new BigDecimal("1000.00"));
        cuenta.setEstatus(EstatusCuenta.ACTIVA);
        cuenta.setCliente(cliente);

        producto = new ProductoXml();
        producto.setIdServicio("2284");
        producto.setIdProducto("14302");
        producto.setServicio("Telcel");
        producto.setNombreProducto("Recarga 100");
        producto.setPrecio("100.00");
    }

    @Test
    @DisplayName("Debe debitar saldo preventivamente y guardar la transaccion en EN_PROCESO")
    void reservarYDebitarSaldo_exitoso() {
        when(cuentaRepository.findByNumeroCuentaForUpdate("1234567890")).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.findByClaveIdempotencia("tx-key-1")).thenReturn(Optional.empty());
        when(transaccionRepository.existeOperacionDuplicadaReciente(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(false);
        when(transaccionRepository.existsByUpc(anyString())).thenReturn(false);
        when(transaccionRepository.save(any(TransaccionServicio.class))).thenAnswer(inv -> inv.getArgument(0));

        TransaccionServicio tx = persistenceService.reservarYDebitarSaldo(
                1,
                "1234567890",
                "tx-key-1",
                producto,
                2284,
                14302,
                new BigDecimal("100.00"),
                "5512345678",
                null
        );

        assertNotNull(tx);
        assertEquals(EstatusTransaccion.EN_PROCESO, tx.getEstatus());
        assertEquals(new BigDecimal("1000.00"), tx.getSaldoAnterior());
        assertEquals(new BigDecimal("900.00"), tx.getSaldoPosterior());
        assertEquals(new BigDecimal("900.00"), cuenta.getSaldo());
        verify(cuentaRepository).save(cuenta);
        verify(transaccionRepository).save(any(TransaccionServicio.class));
    }

    @Test
    @DisplayName("Debe lanzar SaldoInsuficienteException si el saldo disponible es menor al monto")
    void reservarYDebitarSaldo_saldoInsuficiente() {
        cuenta.setSaldo(new BigDecimal("50.00"));
        when(cuentaRepository.findByNumeroCuentaForUpdate("1234567890")).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.findByClaveIdempotencia("tx-key-2")).thenReturn(Optional.empty());

        assertThrows(SaldoInsuficienteException.class, () ->
                persistenceService.reservarYDebitarSaldo(
                        1,
                        "1234567890",
                        "tx-key-2",
                        producto,
                        2284,
                        14302,
                        new BigDecimal("100.00"),
                        "5512345678",
                        null
                ));

        verify(cuentaRepository, never()).save(any());
        verify(transaccionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar CuentaNoOperativaException si la cuenta bancaria esta inactiva")
    void reservarYDebitarSaldo_cuentaInactiva() {
        cuenta.setEstatus(EstatusCuenta.INACTIVA);
        when(cuentaRepository.findByNumeroCuentaForUpdate("1234567890")).thenReturn(Optional.of(cuenta));

        assertThrows(CuentaNoOperativaException.class, () ->
                persistenceService.reservarYDebitarSaldo(
                        1,
                        "1234567890",
                        "tx-key-3",
                        producto,
                        2284,
                        14302,
                        new BigDecimal("100.00"),
                        "5512345678",
                        null
                ));
    }

    @Test
    @DisplayName("Debe lanzar DuplicateResourceException si se detecta operacion identica reciente")
    void reservarYDebitarSaldo_operacionDuplicadaReciente() {
        when(cuentaRepository.findByNumeroCuentaForUpdate("1234567890")).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.findByClaveIdempotencia("tx-key-4")).thenReturn(Optional.empty());
        when(transaccionRepository.existeOperacionDuplicadaReciente(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                persistenceService.reservarYDebitarSaldo(
                        1,
                        "1234567890",
                        "tx-key-4",
                        producto,
                        2284,
                        14302,
                        new BigDecimal("100.00"),
                        "5512345678",
                        null
                ));
    }

    @Test
    @DisplayName("Debe restituir el saldo a la cuenta al reversar cargo")
    void reversarCargo_exitoso() {
        cuenta.setSaldo(new BigDecimal("900.00"));

        TransaccionServicio tx = new TransaccionServicio();
        tx.setId(50L);
        tx.setNumeroCuenta("1234567890");
        tx.setMonto(new BigDecimal("100.00"));
        tx.setEstatus(EstatusTransaccion.EN_PROCESO);

        when(transaccionRepository.findById(50L)).thenReturn(Optional.of(tx));
        when(cuentaRepository.findByNumeroCuentaForUpdate("1234567890")).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransaccionServicio reversada = persistenceService.reversarCargo(
                50L, EstatusTransaccion.RECHAZADA, "ERR_99", "Rechazo de prueba");

        assertEquals(EstatusTransaccion.RECHAZADA, reversada.getEstatus());
        assertEquals(new BigDecimal("1000.00"), cuenta.getSaldo());
        verify(cuentaRepository).save(cuenta);
        verify(transaccionRepository).save(tx);
    }
}
