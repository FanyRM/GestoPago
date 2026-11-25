package com.proyecto.servicios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.entity.cuentas.EstatusCuenta;
import com.proyecto.servicios.entity.cuentas.TipoCuenta;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.model.banco.CuentaActualizacionRequest;
import com.proyecto.servicios.model.banco.CuentaResponse;
import com.proyecto.servicios.service.CuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias de CuentaController")
class CuentaControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CuentaService cuentaService;

    @InjectMocks
    private CuentaController cuentaController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cuentaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /cuentas/mi-cuenta retorna la cuenta del cliente autenticado (200 OK)")
    void obtenerMiCuenta_retorna200() throws Exception {
        CuentaResponse cuenta = new CuentaResponse();
        cuenta.setNumeroCuenta("1234567890");
        cuenta.setTipoCuenta(TipoCuenta.AHORRO);
        cuenta.setEstatus(EstatusCuenta.ACTIVA);
        cuenta.setSaldo(new BigDecimal("5000.00"));

        when(cuentaService.obtenerMiCuenta()).thenReturn(cuenta);

        mockMvc.perform(get("/cuentas/mi-cuenta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value("1234567890"))
                .andExpect(jsonPath("$.tipoCuenta").value("AHORRO"))
                .andExpect(jsonPath("$.estatus").value("ACTIVA"))
                .andExpect(jsonPath("$.saldo").value(5000.00));

        verify(cuentaService).obtenerMiCuenta();
    }

    @Test
    @DisplayName("GET /cuentas/mi-cuenta/saldo retorna el saldo y estatus (200 OK)")
    void consultarMiSaldo_retorna200() throws Exception {
        CuentaResponse cuenta = new CuentaResponse();
        cuenta.setNumeroCuenta("1234567890");
        cuenta.setTipoCuenta(TipoCuenta.AHORRO);
        cuenta.setEstatus(EstatusCuenta.ACTIVA);
        cuenta.setSaldo(new BigDecimal("2500.00"));

        when(cuentaService.obtenerMiCuenta()).thenReturn(cuenta);

        mockMvc.perform(get("/cuentas/mi-cuenta/saldo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value("1234567890"))
                .andExpect(jsonPath("$.saldo").value(2500.00));

        verify(cuentaService).obtenerMiCuenta();
    }

    @Test
    @DisplayName("GET /cuentas/pendientes retorna cuentas en estatus PENDIENTE_REVISION (200 OK)")
    void obtenerPendientesRevision_retorna200() throws Exception {
        CuentaResponse cuenta = new CuentaResponse();
        cuenta.setNumeroCuenta("9876543210");
        cuenta.setTipoCuenta(TipoCuenta.AHORRO);
        cuenta.setEstatus(EstatusCuenta.PENDIENTE_REVISION);

        when(cuentaService.obtenerPendientesRevision()).thenReturn(List.of(cuenta));

        mockMvc.perform(get("/cuentas/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroCuenta").value("9876543210"))
                .andExpect(jsonPath("$[0].estatus").value("PENDIENTE_REVISION"));

        verify(cuentaService).obtenerPendientesRevision();
    }

    @Test
    @DisplayName("PUT /cuentas/{numeroCuenta}/revision aprueba cuenta y retorna 200 OK")
    void revisarCuenta_retorna200() throws Exception {
        CuentaActualizacionRequest request = new CuentaActualizacionRequest(EstatusCuenta.ACTIVA, "Documentos aprobados");
        CuentaResponse response = new CuentaResponse();
        response.setNumeroCuenta("1234567890");
        response.setEstatus(EstatusCuenta.ACTIVA);
        response.setRevisadoPor("admin@banco.com");
        response.setObservacionRevision("Documentos aprobados");

        when(cuentaService.revisarCuenta(eq("1234567890"), any(CuentaActualizacionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/cuentas/1234567890/revision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estatus").value("ACTIVA"))
                .andExpect(jsonPath("$.revisadoPor").value("admin@banco.com"))
                .andExpect(jsonPath("$.observacionRevision").value("Documentos aprobados"));

        verify(cuentaService).revisarCuenta(eq("1234567890"), any(CuentaActualizacionRequest.class));
    }

    @Test
    @DisplayName("GET /cuentas/{numeroCuenta}/saldo retorna saldo de la cuenta indicada (200 OK)")
    void consultarSaldo_retorna200() throws Exception {
        when(cuentaService.consultarSaldo("1234567890")).thenReturn(new BigDecimal("1500.75"));

        mockMvc.perform(get("/cuentas/1234567890/saldo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value("1234567890"))
                .andExpect(jsonPath("$.saldo").value(1500.75));

        verify(cuentaService).consultarSaldo("1234567890");
    }
}
