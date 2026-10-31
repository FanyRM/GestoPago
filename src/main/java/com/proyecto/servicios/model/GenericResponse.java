package com.proyecto.servicios.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse<T> {

    private Integer codigo;
    private String estatus;
    private String mensaje;
    private T data;

    public static <T> GenericResponse<T> exito(T data, String mensaje) {
        return GenericResponse.<T>builder()
                .codigo(200)
                .estatus("EXITO")
                .mensaje(mensaje)
                .data(data)
                .build();
    }

    public static <T> GenericResponse<T> exito(String mensaje) {
        return GenericResponse.<T>builder()
                .codigo(200)
                .estatus("EXITO")
                .mensaje(mensaje)
                .build();
    }

    public static <T> GenericResponse<T> error(Integer codigo, String mensaje) {
        return GenericResponse.<T>builder()
                .codigo(codigo)
                .estatus("ERROR")
                .mensaje(mensaje)
                .build();
    }

    public static <T> GenericResponse<T> error(Integer codigo, String mensaje, T data) {
        return GenericResponse.<T>builder()
                .codigo(codigo)
                .estatus("ERROR")
                .mensaje(mensaje)
                .data(data)
                .build();
    }
}
