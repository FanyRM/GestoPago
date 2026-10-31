package com.proyecto.servicios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.entity.gestopago.EstatusTransaccion;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.exception.SaldoInsuficienteException;
import com.proyecto.servicios.model.gestopago.sendtx.PagoServicioResponse;
import com.proyecto.servicios.model.gestopago.sendtx.SendTxRequest;
import com.proyecto.servicios.service.PagoServicioService;
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
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de PagoServicioController")
class PagoServicioControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private PagoServicioService pagoServicioService;

    @InjectMocks
    private PagoServicioController pagoServicioController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pagoServicioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /servicios/pagos - Debe retornar 201 Created cuando el pago es exitoso")
    void realizarPago_exitoso() throws Exception {
        SendTxRequest request = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("100.00"))
                .telefono("5512345678")
                .claveIdempotencia("tx-post-001")
                .build();

        PagoServicioResponse response = PagoServicioResponse.builder()
                .transaccionId(1L)
                .upc("9988776655")
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .nombreServicio("Telcel")
                .idProducto(14302)
                .nombreProducto("Recarga 100")
                .monto(new BigDecimal("100.00"))
                .saldoDisponible(new BigDecimal("900.00"))
                .estatus(EstatusTransaccion.EXITOSA)
                .folioProveedor("FOL-12345")
                .numAutorizacion("AUTH-6789")
                .fechaOperacion(LocalDateTime.now())
                .build();

        when(pagoServicioService.procesarPagoServicio(any(SendTxRequest.class), any()))
                .thenReturn(response);

        mockMvc.perform(post("/servicios/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transaccionId").value(1))
                .andExpect(jsonPath("$.upc").value("9988776655"))
                .andExpect(jsonPath("$.estatus").value("EXITOSA"))
                .andExpect(jsonPath("$.numAutorizacion").value("AUTH-6789"));
    }

    @Test
    @DisplayName("POST /servicios/pagos - Debe retornar 200 OK cuando la respuesta es idempotente")
    void realizarPago_idempotente() throws Exception {
        SendTxRequest request = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("100.00"))
                .telefono("5512345678")
                .claveIdempotencia("tx-post-001")
                .build();

        PagoServicioResponse response = PagoServicioResponse.builder()
                .transaccionId(1L)
                .upc("9988776655")
                .numeroCuenta("1234567890")
                .estatus(EstatusTransaccion.EXITOSA)
                .respuestaIdempotente(true)
                .build();

        when(pagoServicioService.procesarPagoServicio(any(SendTxRequest.class), any()))
                .thenReturn(response);

        mockMvc.perform(post("/servicios/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuestaIdempotente").value(true));
    }

    @Test
    @DisplayName("POST /servicios/pagos - Debe retornar 422 Unprocessable Entity cuando hay saldo insuficiente")
    void realizarPago_saldoInsuficiente() throws Exception {
        SendTxRequest request = SendTxRequest.builder()
                .numeroCuenta("1234567890")
                .idServicio(2284)
                .idProducto(14302)
                .montoPago(new BigDecimal("100.00"))
                .telefono("5512345678")
                .claveIdempotencia("tx-post-001")
                .build();

        when(pagoServicioService.procesarPagoServicio(any(SendTxRequest.class), any()))
                .thenThrow(new SaldoInsuficienteException("Saldo disponible insuficiente"));

        mockMvc.perform(post("/servicios/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.estatus").value("ERROR"))
                .andExpect(jsonPath("$.mensaje").value("Saldo disponible insuficiente"));
    }

    @Test
    @DisplayName("GET /servicios/pagos/{upc} - Consulta por UPC exitosa")
    void consultarPorUpc_exitoso() throws Exception {
        PagoServicioResponse response = PagoServicioResponse.builder()
                .transaccionId(1L)
                .upc("9988776655")
                .estatus(EstatusTransaccion.EXITOSA)
                .build();

        when(pagoServicioService.consultarTransaccionPorUpc("9988776655")).thenReturn(response);

        mockMvc.perform(get("/servicios/pagos/9988776655"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upc").value("9988776655"))
                .andExpect(jsonPath("$.estatus").value("EXITOSA"));
    }
}
