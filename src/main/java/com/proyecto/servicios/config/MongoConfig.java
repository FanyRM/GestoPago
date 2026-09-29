package com.proyecto.servicios.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Configuración para la conexión y repositorios de MongoDB.
 * La conexión (URI) es manejada automáticamente por Spring Boot a través de application.properties.
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.proyecto.servicios.repositorys.mongo")
public class MongoConfig {

    // En caso de requerir configuraciones adicionales avanzadas (como conversores de tipos, 
    // validadores personalizados o múltiples bases de datos Mongo), se pueden declarar los Beans aquí.

}
