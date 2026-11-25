package com.proyecto.servicios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.entity.catalogo.CatalogoPais;
import com.proyecto.servicios.exception.GlobalExceptionHandler;
import com.proyecto.servicios.model.CatalogoPaisRequest;
import com.proyecto.servicios.service.CatalogoService;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CatalogoControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CatalogoService catalogoService;

    @InjectMocks
    private CatalogoController catalogoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(catalogoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/catalogos/pais - Retorna 200 OK con lista de países")
    void getPaises_exitoso() throws Exception {
        CatalogoPais pais = new CatalogoPais();
        pais.setId(1);
        pais.setDescripcion("México");
        pais.setNacionalidad("Mexicana");
        pais.setLada("52");
        pais.setCodigoIso("MEX");
        pais.setActivo(true);

        when(catalogoService.consultarPaises()).thenReturn(Collections.singletonList(pais));

        mockMvc.perform(get("/api/catalogos/pais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].descripcion").value("México"))
                .andExpect(jsonPath("$[0].nacionalidad").value("Mexicana"))
                .andExpect(jsonPath("$[0].lada").value("52"))
                .andExpect(jsonPath("$[0].codigoIso").value("MEX"));
    }

    @Test
    @DisplayName("POST /api/catalogos/pais - Registro exitoso con lada, país y nacionalidad válidos devuelve 201 Created")
    void registrarPais_exitoso() throws Exception {
        CatalogoPaisRequest request = CatalogoPaisRequest.builder()
                .pais("Alemania")
                .nacionalidad("Alemana")
                .lada("49")
                .codigoIso("DEU")
                .build();

        CatalogoPais guardado = new CatalogoPais();
        guardado.setId(10);
        guardado.setDescripcion("Alemania");
        guardado.setNacionalidad("Alemana");
        guardado.setLada("49");
        guardado.setCodigoIso("DEU");
        guardado.setActivo(true);

        when(catalogoService.registrarPais(any(CatalogoPaisRequest.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/catalogos/pais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(200))
                .andExpect(jsonPath("$.estatus").value("EXITO"))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.descripcion").value("Alemania"))
                .andExpect(jsonPath("$.data.nacionalidad").value("Alemana"))
                .andExpect(jsonPath("$.data.lada").value("49"));
    }

    @Test
    @DisplayName("POST /api/catalogos/pais - Lada con más de 3 dígitos devuelve 400 Bad Request")
    void registrarPais_ladaInvalida_masDeTresDigitos() throws Exception {
        CatalogoPaisRequest request = CatalogoPaisRequest.builder()
                .pais("Alemania")
                .nacionalidad("Alemana")
                .lada("1234") // Más de 3 dígitos
                .codigoIso("DEU")
                .build();

        mockMvc.perform(post("/api/catalogos/pais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400))
                .andExpect(jsonPath("$.estatus").value("ERROR"))
                .andExpect(jsonPath("$.data.lada").value("La lada debe ser un valor numérico y tener entre 1 y 3 dígitos"));

        verify(catalogoService, never()).registrarPais(any());
    }

    @Test
    @DisplayName("POST /api/catalogos/pais - Lada no numérica devuelve 400 Bad Request")
    void registrarPais_ladaInvalida_noNumerica() throws Exception {
        CatalogoPaisRequest request = CatalogoPaisRequest.builder()
                .pais("Alemania")
                .nacionalidad("Alemana")
                .lada("AB") // No numérico
                .codigoIso("DEU")
                .build();

        mockMvc.perform(post("/api/catalogos/pais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400))
                .andExpect(jsonPath("$.estatus").value("ERROR"))
                .andExpect(jsonPath("$.data.lada").value("La lada debe ser un valor numérico y tener entre 1 y 3 dígitos"));

        verify(catalogoService, never()).registrarPais(any());
    }

    @Test
    @DisplayName("POST /api/catalogos/pais - País con menos de 3 caracteres devuelve 400 Bad Request")
    void registrarPais_paisInvalido_muyCorto() throws Exception {
        CatalogoPaisRequest request = CatalogoPaisRequest.builder()
                .pais("Al") // Menos de 3 caracteres
                .nacionalidad("Alemana")
                .lada("49")
                .codigoIso("DEU")
                .build();

        mockMvc.perform(post("/api/catalogos/pais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400))
                .andExpect(jsonPath("$.estatus").value("ERROR"))
                .andExpect(jsonPath("$.data.pais").value("El país debe tener entre 3 y 100 caracteres"));

        verify(catalogoService, never()).registrarPais(any());
    }

    @Test
    @DisplayName("POST /api/catalogos/pais - País con números o caracteres inválidos devuelve 400 Bad Request por regex")
    void registrarPais_paisInvalido_caracteresEspeciales() throws Exception {
        CatalogoPaisRequest request = CatalogoPaisRequest.builder()
                .pais("Pais123#") // Contiene números y caracteres especiales
                .nacionalidad("Alemana")
                .lada("49")
                .codigoIso("DEU")
                .build();

        mockMvc.perform(post("/api/catalogos/pais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400))
                .andExpect(jsonPath("$.estatus").value("ERROR"))
                .andExpect(jsonPath("$.data.pais").value("El país solo debe contener letras y espacios"));

        verify(catalogoService, never()).registrarPais(any());
    }

    @Test
    @DisplayName("POST /api/catalogos/pais - Nacionalidad con menos de 3 caracteres devuelve 400 Bad Request")
    void registrarPais_nacionalidadInvalida_muyCorta() throws Exception {
        CatalogoPaisRequest request = CatalogoPaisRequest.builder()
                .pais("Alemania")
                .nacionalidad("Al") // Menos de 3 caracteres
                .lada("49")
                .codigoIso("DEU")
                .build();

        mockMvc.perform(post("/api/catalogos/pais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400))
                .andExpect(jsonPath("$.estatus").value("ERROR"))
                .andExpect(jsonPath("$.data.nacionalidad").value("La nacionalidad debe tener entre 3 y 100 caracteres"));

        verify(catalogoService, never()).registrarPais(any());
    }

    @Test
    @DisplayName("POST /api/catalogos/pais - Nacionalidad con números o símbolos devuelve 400 Bad Request por regex")
    void registrarPais_nacionalidadInvalida_regex() throws Exception {
        CatalogoPaisRequest request = CatalogoPaisRequest.builder()
                .pais("Alemania")
                .nacionalidad("Alemana123") // Números en nacionalidad
                .lada("49")
                .codigoIso("DEU")
                .build();

        mockMvc.perform(post("/api/catalogos/pais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400))
                .andExpect(jsonPath("$.estatus").value("ERROR"))
                .andExpect(jsonPath("$.data.nacionalidad").value("La nacionalidad solo debe contener letras y espacios"));

        verify(catalogoService, never()).registrarPais(any());
    }
}
