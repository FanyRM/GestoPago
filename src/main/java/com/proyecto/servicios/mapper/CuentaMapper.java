package com.proyecto.servicios.mapper;

import com.proyecto.servicios.entity.sf.Cuenta;
import com.proyecto.servicios.model.banco.CuentaResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CuentaMapper {

    @Mapping(target = "id", expression = "java(entity.getId() != null ? Long.valueOf(entity.getId()) : null)")
    @Mapping(target = "clienteId", expression = "java(entity.getCliente() != null && entity.getCliente().getId() != null ? Long.valueOf(entity.getCliente().getId()) : null)")
    @Mapping(target = "clienteNombre", expression = "java(entity.getCliente() != null ? (entity.getCliente().getNombre() != null ? entity.getCliente().getNombre() : \"\") + \" \" + (entity.getCliente().getApellidoPaterno() != null ? entity.getCliente().getApellidoPaterno() : \"\") : null)")
    CuentaResponse toResponse(Cuenta entity);

    List<CuentaResponse> toResponses(List<Cuenta> entities);
}
