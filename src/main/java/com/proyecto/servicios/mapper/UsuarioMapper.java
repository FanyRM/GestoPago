package com.proyecto.servicios.mapper;

import com.proyecto.servicios.entity.seguridad.Usuario;
import com.proyecto.servicios.model.seguridad.UsuarioResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface UsuarioMapper {

    @Mapping(target = "clienteId", expression = "java(entity.getCliente() != null && entity.getCliente().getId() != null ? Long.valueOf(entity.getCliente().getId()) : null)")
    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "estatus", ignore = true)
    @Mapping(target = "mensaje", ignore = true)
    @Mapping(target = "data", ignore = true)
    @Mapping(target = "errores", ignore = true)
    UsuarioResponse toResponse(Usuario entity);

    List<UsuarioResponse> toResponses(List<Usuario> entities);
}
