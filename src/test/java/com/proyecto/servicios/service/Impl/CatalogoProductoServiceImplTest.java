package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoCatProduct;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.enums.ApiResponseEnum;
import com.proyecto.servicios.exception.CatalogoException;
import com.proyecto.servicios.model.gestopago.catalogo.CatalogoCacheDto;
import com.proyecto.servicios.model.gestopago.catalogo.CatalogoProductoResponse;
import com.proyecto.servicios.model.gestopago.catalogo.ProductoDto;
import com.proyecto.servicios.repositorys.mongo.CatalogoProductoRepository;
import com.proyecto.servicios.service.GestoPagoTokenService;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link CatalogoProductoServiceImpl}.
 *
 * <p>Se cubren los escenarios principales:</p>
 * <ul>
 *   <li>Sincronización exitosa (XML → MongoDB).</li>
 *   <li>Sin token activo — sincronización cancelada.</li>
 *   <li>Servicio externo falla repetidamente — alerta crítica, sin datos guardados.</li>
 *   <li>Consulta exitosa desde MongoDB.</li>
 *   <li>Consulta con MongoDB vacío — lanza {@link CatalogoException} MONGO_NOT_FOUND.</li>
 *   <li>Error inesperado en MongoDB — lanza {@link CatalogoException} MONGO_ERROR.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogoProductoServiceImpl — Pruebas Unitarias")
class CatalogoProductoServiceImplTest {

    @Mock
    private GestoPagoCatProduct catProductClient;

    @Mock
    private CatalogoProductoRepository catalogoRepository;

    @Mock
    private GestoPagoTokenService gestoPagoTokenService;

    @InjectMocks
    private CatalogoProductoServiceImpl service;

    // -------------------------------------------------------------------------
    // XML de prueba con estructura JAXB válida
    // -------------------------------------------------------------------------
    private static final String XML_VALIDO = """
            <?xml version="1.0" encoding="UTF-8"?>
            <productos>
                <producto>
                    <sku>PROD-001</sku>
                    <nombre>Recarga Telcel 50</nombre>
                    <descripcion>Recarga de tiempo aire Telcel $50</descripcion>
                    <precio>50.00</precio>
                    <categoria>Recargas</categoria>
                    <activo>true</activo>
                    <proveedor>Telcel</proveedor>
                </producto>
                <producto>
                    <sku>PROD-002</sku>
                    <nombre>Recarga Movistar 100</nombre>
                    <descripcion>Recarga de tiempo aire Movistar $100</descripcion>
                    <precio>100.00</precio>
                    <categoria>Recargas</categoria>
                    <activo>true</activo>
                    <proveedor>Movistar</proveedor>
                </producto>
            </productos>
            """;

    @BeforeEach
    void configurar() {
        // Inyectamos los valores de @Value por reflexión (no se cargan en tests unitarios)
        ReflectionTestUtils.setField(service, "idDistribuidor", 83);
        ReflectionTestUtils.setField(service, "codigoDispositivo", "GPS83-TPV-17");
    }

    // =========================================================================
    // Tests de sincronizarCatalogo()
    // =========================================================================

    @Test
    @DisplayName("Sincronización exitosa: XML recibido y guardado en MongoDB")
    void sincronizarCatalogo_cuandoServicioResponde_debeGuardarEnMongo() {
        // Arrange
        GestoPagoToken tokenEntity = buildTokenEntity("token-test-abc");
        when(gestoPagoTokenService.obtenerTokenActivo(83, "GPS83-TPV-17"))
                .thenReturn(Optional.of(tokenEntity));
        when(catProductClient.getProductList("Bearer token-test-abc"))
                .thenReturn(XML_VALIDO);
        when(catalogoRepository.save(any(CatalogoCacheDto.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        service.sincronizarCatalogo();

        // Assert
        verify(catalogoRepository, times(1)).deleteAll();
        verify(catalogoRepository, times(1)).save(argThat((CatalogoCacheDto cache) ->
                cache.getProductos() != null &&
                cache.getProductos().size() == 2 &&
                cache.getTotalProductos() == 2
        ));
    }

    @Test
    @DisplayName("Sin token activo: sincronización debe cancelarse sin llamar al cliente")
    void sincronizarCatalogo_sinTokenActivo_debeCancelarSinLlamarCliente() {
        // Arrange
        when(gestoPagoTokenService.obtenerTokenActivo(83, "GPS83-TPV-17"))
                .thenReturn(Optional.empty());

        // Act
        service.sincronizarCatalogo();

        // Assert
        verifyNoInteractions(catProductClient);
        verifyNoInteractions(catalogoRepository);
    }

    @Test
    @DisplayName("Servicio externo falla 3 veces: no debe guardar en MongoDB")
    void sincronizarCatalogo_cuandoServicioFalla3Veces_noDebeGuardarEnMongo() {
        // Arrange
        GestoPagoToken tokenEntity = buildTokenEntity("token-test-xyz");
        when(gestoPagoTokenService.obtenerTokenActivo(83, "GPS83-TPV-17"))
                .thenReturn(Optional.of(tokenEntity));

        // Usamos RuntimeException genérica — el catch (Exception e) del service la captura
        // y reintenta, igual que con FeignException de tipo no-401.
        when(catProductClient.getProductList(anyString()))
                .thenThrow(new RuntimeException("Servicio no disponible (503)"));

        // Act — el test tardará ~4s por los 2 sleeps de 2s entre reintentos
        service.sincronizarCatalogo();

        // Assert
        verify(catProductClient, times(3)).getProductList("Bearer token-test-xyz");
        verify(catalogoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Error de autenticación (401): no debe reintentar")
    void sincronizarCatalogo_cuandoError401_noDebeReintentar() {
        // Arrange
        GestoPagoToken tokenEntity = buildTokenEntity("token-invalido");
        when(gestoPagoTokenService.obtenerTokenActivo(83, "GPS83-TPV-17"))
                .thenReturn(Optional.of(tokenEntity));

        // FeignException.Unauthorized requiere un Request real (no se puede mock() directamente)
        Request feignRequest = Request.create(
                Request.HttpMethod.GET,
                "http://gestopago.portalventas.net/sistema/service/getProductList.do",
                Collections.emptyMap(),
                Request.Body.empty(),
                null
        );
        FeignException.Unauthorized unauthorized = new FeignException.Unauthorized(
                "401 Unauthorized", feignRequest, null, Collections.emptyMap());
        when(catProductClient.getProductList(anyString())).thenThrow(unauthorized);

        // Act
        service.sincronizarCatalogo();

        // Assert — solo debe haber 1 intento, no 3
        verify(catProductClient, times(1)).getProductList("Bearer token-invalido");
        verify(catalogoRepository, never()).save(any());
    }

    // =========================================================================
    // Tests de obtenerCatalogo()
    // =========================================================================

    @Test
    @DisplayName("Consulta exitosa: MongoDB tiene datos y devuelve respuesta 200")
    void obtenerCatalogo_cuandoExisteDatosEnMongo_debeRetornarRespuesta200() {
        // Arrange
        ProductoDto prod1 = new ProductoDto();
        prod1.setSku("PROD-001");
        prod1.setNombre("Recarga Telcel 50");

        CatalogoCacheDto cacheDto = new CatalogoCacheDto();
        cacheDto.setProductos(List.of(prod1));
        cacheDto.setTotalProductos(1);
        cacheDto.setFechaActualizacion(LocalDateTime.now());

        when(catalogoRepository.findTopByOrderByFechaActualizacionDesc())
                .thenReturn(Optional.of(cacheDto));

        // Act
        CatalogoProductoResponse respuesta = service.obtenerCatalogo();

        // Assert
        assertThat(respuesta).isNotNull();
        assertThat(respuesta.getCodigo()).isEqualTo(200);
        assertThat(respuesta.getProductos()).hasSize(1);
        assertThat(respuesta.getProductos().get(0).getSku()).isEqualTo("PROD-001");
        assertThat(respuesta.getTotalProductos()).isEqualTo(1);
    }

    @Test
    @DisplayName("Catálogo vacío en MongoDB: debe lanzar CatalogoException MONGO_NOT_FOUND")
    void obtenerCatalogo_cuandoMongoVacio_debeLanzarCatalogoExceptionNotFound() {
        // Arrange
        when(catalogoRepository.findTopByOrderByFechaActualizacionDesc())
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.obtenerCatalogo())
                .isInstanceOf(CatalogoException.class)
                .satisfies(ex -> {
                    CatalogoException ce = (CatalogoException) ex;
                    assertThat(ce.getResponseEnum()).isEqualTo(ApiResponseEnum.MONGO_NOT_FOUND);
                    assertThat(ce.getResponseEnum().getCodigo()).isEqualTo(404);
                });
    }

    @Test
    @DisplayName("Error de MongoDB: debe lanzar CatalogoException MONGO_ERROR")
    void obtenerCatalogo_cuandoMongoLanzaExcepcion_debeLanzarCatalogoExceptionMongoError() {
        // Arrange
        when(catalogoRepository.findTopByOrderByFechaActualizacionDesc())
                .thenThrow(new RuntimeException("Conexión MongoDB perdida"));

        // Act & Assert
        assertThatThrownBy(() -> service.obtenerCatalogo())
                .isInstanceOf(CatalogoException.class)
                .satisfies(ex -> {
                    CatalogoException ce = (CatalogoException) ex;
                    assertThat(ce.getResponseEnum()).isEqualTo(ApiResponseEnum.MONGO_ERROR);
                    assertThat(ce.getResponseEnum().getCodigo()).isEqualTo(500);
                });
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private GestoPagoToken buildTokenEntity(String tokenValue) {
        GestoPagoToken t = new GestoPagoToken();
        t.setToken(tokenValue);
        t.setIdDistribuidor(83);
        t.setCodigoDispositivo("GPS83-TPV-17");
        t.setActivo(true);
        return t;
    }
}
