package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoCatProduct;
import com.proyecto.servicios.enums.ApiResponseEnum;
import com.proyecto.servicios.exception.CatalogoException;
import com.proyecto.servicios.model.gestopago.catalogo.CatalogoCacheDto;
import com.proyecto.servicios.model.gestopago.catalogo.CatalogoProductoResponse;
import com.proyecto.servicios.model.gestopago.catalogo.GestoPagoCatProductResponse;
import com.proyecto.servicios.model.gestopago.catalogo.ProductoXml;
import com.proyecto.servicios.repositorys.mongo.CatalogoProductoRepository;
import com.proyecto.servicios.service.CatalogoProductoService;
import com.proyecto.servicios.service.GestoPagoTokenService;
import feign.FeignException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogoProductoServiceImpl implements CatalogoProductoService {

    private static final int MAX_REINTENTOS = 3;
    private static final long ESPERA_ENTRE_REINTENTOS_MS = 2000L;

    private final GestoPagoCatProduct catProductClient;
    private final CatalogoProductoRepository catalogoRepository;
    private final GestoPagoTokenService gestoPagoTokenService;

    @Value("${gestopago.auth.id-distribuidor}")
    private Integer idDistribuidor;

    @Value("${gestopago.auth.codigo-dispositivo}")
    private String codigoDispositivo;

    @Override
    public void sincronizarCatalogo() {
        String token = obtenerBearerToken();
        if (token == null) {
            return;
        }

        String xmlRespuesta = invocarServicioConReintentos(token);

        if (xmlRespuesta == null) {
            return;
        }

        List<ProductoXml> productos = deserializarXml(xmlRespuesta);

        if (productos == null || productos.isEmpty()) {
            return;
        }

        almacenarEnMongo(productos);
    }

    @Override
    public CatalogoProductoResponse obtenerCatalogo() {
        try {
            CatalogoCacheDto cache = catalogoRepository
                    .findTopByOrderByFechaActualizacionDesc()
                    .orElseThrow(() -> new CatalogoException(ApiResponseEnum.MONGO_NOT_FOUND));

            return CatalogoProductoResponse.builder()
                    .codigo(ApiResponseEnum.OK.getCodigo())
                    .mensaje(ApiResponseEnum.OK.getMensaje())
                    .totalProductos(cache.getTotalProductos())
                    .fechaActualizacion(cache.getFechaActualizacion())
                    .productos(cache.getProductos())
                    .build();

        } catch (CatalogoException e) {
            throw e;
        } catch (Exception e) {
            throw new CatalogoException(ApiResponseEnum.MONGO_ERROR, e);
        }
    }

    private String obtenerBearerToken() {
        return gestoPagoTokenService
                .obtenerTokenActivo(idDistribuidor, codigoDispositivo)
                .map(t -> "Bearer " + t.getToken())
                .orElse(null);
    }

    private String invocarServicioConReintentos(String bearerToken) {
        for (int intento = 1; intento <= MAX_REINTENTOS; intento++) {
            try {
                return catProductClient.getProductList(bearerToken);

            } catch (FeignException.Unauthorized e) {
                return null;
            } catch (Exception e) {
                log.error("Error invoking service", e);
            }

            if (intento < MAX_REINTENTOS) {
                esperarEntreReintentos();
            }
        }
        return null;
    }

    private void esperarEntreReintentos() {
        try {
            Thread.sleep(ESPERA_ENTRE_REINTENTOS_MS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private List<ProductoXml> deserializarXml(String xml) {
        try {
            JAXBContext context = JAXBContext.newInstance(GestoPagoCatProductResponse.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            GestoPagoCatProductResponse response =
                    (GestoPagoCatProductResponse) unmarshaller.unmarshal(new StringReader(xml));

            List<ProductoXml> productos = (response.getProductos() != null) ? response.getProductos().getProductos() : null;
            return productos != null ? productos : Collections.emptyList();

        } catch (JAXBException e) {
            return Collections.emptyList();
        }
    }

    private void almacenarEnMongo(List<ProductoXml> productos) {
        try {
            catalogoRepository.deleteAll();

            CatalogoCacheDto nuevoCache = new CatalogoCacheDto();
            nuevoCache.setProductos(productos);
            nuevoCache.setFechaActualizacion(LocalDateTime.now());
            nuevoCache.setTotalProductos(productos.size());

            catalogoRepository.save(nuevoCache);

        } catch (Exception e) {
            log.error("Error al almacenar en Mongo", e);
        }
    }
}
