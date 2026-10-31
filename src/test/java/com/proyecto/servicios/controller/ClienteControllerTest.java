package com.proyecto.servicios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.exception.ClienteNoEncontradoException;
import com.proyecto.servicios.exception.CorreoDuplicadoException;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.model.*;
import com.proyecto.servicios.service.IClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Mock
    private IClienteService clienteService;

    @InjectMocks
    private ClienteController clienteController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clienteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /clientes - Registro exitoso devuelve 201 Created y estructura GenericResponse")
    void registrarCliente_exitoso() throws Exception {
        ClienteRequest request = new ClienteRequest();
        request.setNombre("Carlos");
        request.setApellidoPaterno("Hernández");
        request.setApellidoMaterno("López");
        request.setFechaNacimiento(LocalDate.of(1990, 5, 10));
        request.setPassword("Segura123#");
        request.setCurp("HELC900510HMCRSN01");
        request.setRfc("HELC900510AB1");
        request.setSexo('H');
        request.setIdNacionalidad(1);
        request.setIdEstadoCivil(1);
        request.setCorreoElectronico("carlos@mail.com");
        request.setLadaMovil(521);
        request.setTelefonoMovil("1234567890");
        request.setIdOcupacion(1);
        request.setIngresoMensual(new BigDecimal("25000.00"));
        request.setCalle("Av. Reforma");
        request.setNumeroExterior("100");
        request.setColonia("Juárez");
        request.setMunicipio("Cuauhtémoc");
        request.setEstado("CDMX");
        request.setCodigoPostal("06600");
        request.setIdPais(1);

        ClienteResponse response = ClienteResponse.builder()
                .id(1)
                .nombre("Carlos")
                .curp("HELC900510HMCRSN01")
                .rfc("HELC900510AB1")
                .numeroCuentaAsignada("CTA1234567")
                .activo(true)
                .build();

        when(clienteService.registrarCliente(any(ClienteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(200))
                .andExpect(jsonPath("$.estatus").value("EXITO"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.numeroCuentaAsignada").value("CTA1234567"));
    }

    @Test
    @DisplayName("POST /clientes - Error de validación devuelve 400 Bad Request sin llegar al servicio")
    void registrarCliente_validacionFallida() throws Exception {
        ClienteRequest requestInvalido = new ClienteRequest();
        // Campos obligatorios faltantes

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400))
                .andExpect(jsonPath("$.estatus").value("ERROR"))
                .andExpect(jsonPath("$.mensaje").value("Error de validación en los campos de entrada"))
                .andExpect(jsonPath("$.data").isMap());

        verify(clienteService, never()).registrarCliente(any());
    }

    @Test
    @DisplayName("POST /clientes/consultar - Consulta con RequestBody devuelve 200 OK")
    void consultarClientes_conFiltros() throws Exception {
        ClienteConsultaRequest request = ClienteConsultaRequest.builder()
                .curp("HELC900510HMCRSN01")
                .pagina(0)
                .tamano(10)
                .build();

        ClienteResponse response = ClienteResponse.builder()
                .id(1)
                .curp("HELC900510HMCRSN01")
                .build();

        when(clienteService.consultarClientes(any(ClienteConsultaRequest.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(response)));

        mockMvc.perform(post("/clientes/consultar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(200))
                .andExpect(jsonPath("$.estatus").value("EXITO"))
                .andExpect(jsonPath("$.data.contenido[0].id").value(1));
    }

    @Test
    @DisplayName("PUT /clientes - Actualización exitosa con Request DTO devuelve 200 OK")
    void actualizarCliente_exitoso() throws Exception {
        ClienteActualizaRequest request = ClienteActualizaRequest.builder()
                .id(1)
                .nombre("Carlos Actualizado")
                .build();

        ClienteResponse response = ClienteResponse.builder()
                .id(1)
                .nombre("Carlos Actualizado")
                .build();

        when(clienteService.actualizarCliente(any(ClienteActualizaRequest.class))).thenReturn(response);

        mockMvc.perform(put("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(200))
                .andExpect(jsonPath("$.estatus").value("EXITO"))
                .andExpect(jsonPath("$.data.nombre").value("Carlos Actualizado"));
    }

    @Test
    @DisplayName("DELETE /clientes - Baja lógica con Request DTO devuelve 200 OK")
    void desactivarCliente_exitoso() throws Exception {
        ClienteBajaRequest request = ClienteBajaRequest.builder()
                .id(1)
                .build();

        doNothing().when(clienteService).desactivarCliente(1);

        mockMvc.perform(delete("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(200))
                .andExpect(jsonPath("$.estatus").value("EXITO"))
                .andExpect(jsonPath("$.mensaje").value("Cliente desactivado correctamente (baja lógica)"));
    }

    @Test
    @DisplayName("POST /clientes - Correo duplicado devuelve 409 Conflict manejado por @RestControllerAdvice")
    void registrarCliente_correoDuplicado_devuelve409() throws Exception {
        ClienteRequest request = new ClienteRequest();
        request.setNombre("Carlos");
        request.setApellidoPaterno("Hernández");
        request.setApellidoMaterno("López");
        request.setFechaNacimiento(LocalDate.of(1990, 5, 10));
        request.setPassword("Segura123#");
        request.setCurp("HELC900510HMCRSN01");
        request.setRfc("HELC900510AB1");
        request.setSexo('H');
        request.setIdNacionalidad(1);
        request.setIdEstadoCivil(1);
        request.setCorreoElectronico("duplicado@mail.com");
        request.setLadaMovil(521);
        request.setTelefonoMovil("1234567890");
        request.setIdOcupacion(1);
        request.setIngresoMensual(new BigDecimal("25000.00"));
        request.setCalle("Av. Reforma");
        request.setNumeroExterior("100");
        request.setColonia("Juárez");
        request.setMunicipio("Cuauhtémoc");
        request.setEstado("CDMX");
        request.setCodigoPostal("06600");
        request.setIdPais(1);

        when(clienteService.registrarCliente(any()))
                .thenThrow(new CorreoDuplicadoException("Ya existe un cliente registrado con el mismo correo electrónico: duplicado@mail.com"));

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value(409))
                .andExpect(jsonPath("$.estatus").value("ERROR"))
                .andExpect(jsonPath("$.mensaje").value("Ya existe un cliente registrado con el mismo correo electrónico: duplicado@mail.com"));
    }

    @Test
    @DisplayName("PUT /clientes - Cliente inexistente devuelve 404 Not Found")
    void actualizarCliente_inexistente_devuelve404() throws Exception {
        ClienteActualizaRequest request = ClienteActualizaRequest.builder()
                .id(99999)
                .nombre("Inexistente")
                .build();

        when(clienteService.actualizarCliente(any()))
                .thenThrow(new ClienteNoEncontradoException("No se puede actualizar. Cliente no encontrado con ID: 99999"));

        mockMvc.perform(put("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404))
                .andExpect(jsonPath("$.estatus").value("ERROR"))
                .andExpect(jsonPath("$.mensaje").value("No se puede actualizar. Cliente no encontrado con ID: 99999"));
    }
}
