package com.proyecto.servicios.controller;

import com.proyecto.servicios.enums.ApiResponseEnum;
import com.proyecto.servicios.exception.CatalogoException;
import com.proyecto.servicios.model.gestopago.catalogo.CatalogoProductoResponse;
import com.proyecto.servicios.model.gestopago.catalogo.ProductoXml;
import com.proyecto.servicios.service.CatalogoProductoService;
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

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogoProductoController — Pruebas Unitarias")
class CatalogoProductoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CatalogoProductoService catalogoProductoService;

    @InjectMocks
    private CatalogoProductoController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /gestopago/catalogo/productos — Consulta exitosa desde MongoDB")
    void obtenerCatalogo_exitoso() throws Exception {
        ProductoXml producto = new ProductoXml();
        producto.setIdProducto("101");
        producto.setNombreProducto("Tiempo Aire Telcel $100");

        CatalogoProductoResponse response = CatalogoProductoResponse.builder()
                .codigo(200)
                .mensaje("Operación realizada correctamente")
                .totalProductos(1)
                .fechaActualizacion(LocalDateTime.now())
                .productos(Collections.singletonList(producto))
                .build();

        when(catalogoProductoService.obtenerCatalogo()).thenReturn(response);

        mockMvc.perform(get("/gestopago/catalogo/productos")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(200))
                .andExpect(jsonPath("$.totalProductos").value(1))
                .andExpect(jsonPath("$.productos[0].idProducto").value("101"));

        verify(catalogoProductoService, never()).sincronizarCatalogo();
    }

    @Test
    @DisplayName("GET /gestopago/catalogo/productos?forzar=true — Sincroniza y luego consulta")
    void obtenerCatalogo_forzarSincronizacion() throws Exception {
        CatalogoProductoResponse response = CatalogoProductoResponse.builder()
                .codigo(200)
                .mensaje("Operación realizada correctamente")
                .totalProductos(0)
                .build();

        when(catalogoProductoService.obtenerCatalogo()).thenReturn(response);

        mockMvc.perform(get("/gestopago/catalogo/productos?forzar=true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(catalogoProductoService, times(1)).sincronizarCatalogo();
        verify(catalogoProductoService, times(1)).obtenerCatalogo();
    }

    @Test
    @DisplayName("POST /gestopago/catalogo/sincronizar — Sincronización manual bajo demanda")
    void sincronizarManualmente_exitoso() throws Exception {
        CatalogoProductoResponse response = CatalogoProductoResponse.builder()
                .codigo(200)
                .mensaje("Operación realizada correctamente")
                .totalProductos(5)
                .build();

        when(catalogoProductoService.obtenerCatalogo()).thenReturn(response);

        mockMvc.perform(post("/gestopago/catalogo/sincronizar")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(200))
                .andExpect(jsonPath("$.totalProductos").value(5));

        verify(catalogoProductoService, times(1)).sincronizarCatalogo();
        verify(catalogoProductoService, times(1)).obtenerCatalogo();
    }

    @Test
    @DisplayName("GET /gestopago/catalogo/productos — Retorna 404 cuando MongoDB está vacío")
    void obtenerCatalogo_cuandoNoHayEnCache_retorna404() throws Exception {
        when(catalogoProductoService.obtenerCatalogo())
                .thenThrow(new CatalogoException(ApiResponseEnum.MONGO_NOT_FOUND));

        mockMvc.perform(get("/gestopago/catalogo/productos")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }
}
