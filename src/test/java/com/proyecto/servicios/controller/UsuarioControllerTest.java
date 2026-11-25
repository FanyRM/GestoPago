package com.proyecto.servicios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.model.seguridad.UsuarioActualizacionRequest;
import com.proyecto.servicios.model.seguridad.UsuarioBusquedaRequest;
import com.proyecto.servicios.model.seguridad.UsuarioBusquedaResponse;
import com.proyecto.servicios.model.seguridad.UsuarioCreacionRequest;
import com.proyecto.servicios.model.seguridad.UsuarioEstadoRequest;
import com.proyecto.servicios.model.seguridad.UsuarioResponse;
import com.proyecto.servicios.service.UsuarioService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias de UsuarioController")
class UsuarioControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /usuarios crea usuario y retorna 201 Created")
    void crearUsuario_retorna201() throws Exception {
        UsuarioCreacionRequest request = new UsuarioCreacionRequest(1L, "cliente@banco.com", "Banco123@", "ROLE_CLIENTE");
        UsuarioResponse response = new UsuarioResponse();
        response.setId(10L);
        response.setCorreo("cliente@banco.com");
        response.setRol("ROLE_CLIENTE");
        response.setActivo(true);

        when(usuarioService.crear(any(UsuarioCreacionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.correo").value("cliente@banco.com"))
                .andExpect(jsonPath("$.rol").value("ROLE_CLIENTE"));

        verify(usuarioService).crear(any(UsuarioCreacionRequest.class));
    }

    @Test
    @DisplayName("POST /usuarios/buscar consulta usuarios con filtros y retorna 200 OK")
    void buscarUsuarios_retorna200() throws Exception {
        UsuarioBusquedaRequest request = new UsuarioBusquedaRequest();
        request.setRol("ROLE_CLIENTE");

        UsuarioResponse user1 = new UsuarioResponse();
        user1.setId(10L);
        user1.setCorreo("cliente@banco.com");
        user1.setRol("ROLE_CLIENTE");

        UsuarioBusquedaResponse response = new UsuarioBusquedaResponse();
        response.setCodigo(0);
        response.setMensaje("Consulta exitosa");
        response.setUsuarios(List.of(user1));

        when(usuarioService.buscar(any(UsuarioBusquedaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/usuarios/buscar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(0))
                .andExpect(jsonPath("$.usuarios[0].correo").value("cliente@banco.com"));

        verify(usuarioService).buscar(any(UsuarioBusquedaRequest.class));
    }

    @Test
    @DisplayName("PUT /usuarios/actualizar actualiza usuario y retorna 200 OK")
    void actualizarUsuario_retorna200() throws Exception {
        UsuarioActualizacionRequest request = new UsuarioActualizacionRequest();
        request.setId(10L);
        request.setCorreo("nuevo@banco.com");

        UsuarioResponse response = new UsuarioResponse();
        response.setId(10L);
        response.setCorreo("nuevo@banco.com");
        response.setRol("ROLE_CLIENTE");
        response.setActivo(true);

        when(usuarioService.actualizar(any(UsuarioActualizacionRequest.class))).thenReturn(response);

        mockMvc.perform(put("/usuarios/actualizar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("nuevo@banco.com"));

        verify(usuarioService).actualizar(any(UsuarioActualizacionRequest.class));
    }

    @Test
    @DisplayName("PUT /usuarios/estado cambia estatus logico del usuario y retorna 200 OK")
    void cambiarEstado_retorna200() throws Exception {
        UsuarioEstadoRequest request = new UsuarioEstadoRequest(10L, false);

        UsuarioResponse response = new UsuarioResponse();
        response.setId(10L);
        response.setCorreo("cliente@banco.com");
        response.setActivo(false);

        when(usuarioService.cambiarEstado(any(UsuarioEstadoRequest.class))).thenReturn(response);

        mockMvc.perform(put("/usuarios/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        verify(usuarioService).cambiarEstado(any(UsuarioEstadoRequest.class));
    }
}
