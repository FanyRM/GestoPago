# GestoPago Integration Service

Este proyecto es un microservicio construido en **Spring Boot 3.3.6** cuyo objetivo es integrarse con el proveedor de servicios "GestoPago" para obtener, sincronizar y exponer el catálogo de productos disponibles para transacciones.

## Descripción de la Solución

El sistema no consulta el catálogo de productos en tiempo real al proveedor externo en cada petición de los clientes, ya que el catálogo es una lista de datos que cambia con poca frecuencia. En su lugar, el sistema emplea una estrategia de **Caché Caliente (Hot Cache)**.

El flujo principal consta de dos partes:
1. **Sincronización Programada (Cron Job)**: Un proceso en segundo plano (Scheduler) se ejecuta diariamente (por defecto a las 06:00 AM hora local) para autenticarse, llamar al endpoint XML del catálogo de GestoPago y guardar los datos consolidados en la base de datos.
2. **Sincronización Manual (Bajo Demanda)**: Se expone el endpoint `POST /gestopago/catalogo/sincronizar` (o `GET /gestopago/catalogo/productos?forzar=true`) para forzar la descarga inmediata de productos sin esperar al cron job.
3. **Consulta Rápida (API REST)**: Los clientes consumen un endpoint local rápido (`GET /gestopago/catalogo/productos`) que devuelve instantáneamente la información desde la base de datos de caché.

## Decisiones Técnicas y Arquitectura

Se han tomado diversas decisiones de diseño para garantizar la escalabilidad, el rendimiento y la mantenibilidad del código:

### 1. MongoDB como Capa de Caché
Aunque el proyecto utiliza **PostgreSQL** para persistir datos transaccionales y de configuración (como el `GestoPagoToken`), se decidió emplear **MongoDB** para almacenar el catálogo de productos. 
* **Razón**: El catálogo de productos es un documento estructuralmente flexible y extenso. Almacenarlo como un solo documento JSON o una colección no relacional en Mongo permite extracciones extremadamente rápidas (bajas latencias) sin preocuparse por esquemas relacionales complejos ni migraciones de Flyway si el proveedor decide añadir nuevos campos.

### 2. Mapeo XML a Objetos con JAXB
El servicio web de GestoPago retorna la información en formato XML y utiliza fuertemente *Atributos* XML en lugar de *Nodos* (ej. `<producto idProducto="20" precio="100">`).
* **Razón**: En lugar de parsear manualmente el XML, se optó por **Jakarta XML Binding (JAXB)**. Se diseñaron clases específicas (`GestoPagoCatProductResponse`, `ProductoXml`) con anotaciones estandarizadas (`@XmlRootElement`, `@XmlAttribute`) que garantizan una conversión directa, segura y sin fallos del Payload externo hacia objetos Java.

### 3. Separation of Concerns (Separación de Responsabilidades)
El código está rigurosamente segmentado:
- **`controller`**: Recibe la petición HTTP, llama al servicio y envuelve la respuesta de manera controlada. No posee lógica de negocio.
- **`service/Impl`**: Orquesta el flujo, gestiona los reintentos (política de resiliencia) ante el proveedor externo, y delega a la base de datos.
- **`repositorys`**: Se encuentran separados en paquetes (`mongo` / `gestopago` / `sf`) asegurando que los dominios tecnológicos no colisionen.

### 4. Manejo Estructurado de Excepciones
Se implementó un `@RestControllerAdvice` centralizado (`GlobalExceptionHandler`) que mapea semánticamente cada situación de negocio y técnica:
* `400 Bad Request`: Validaciones de entrada y violaciones de reglas de catálogo/negocio.
* `404 Not Found`: Cuentas, clientes o productos inexistentes.
* `409 Conflict`: Intentos de duplicidad por clave de idempotencia o transacciones idénticas en ventana reciente.
* `422 Unprocessable Entity`: Saldo insuficiente o rechazos explícitos del proveedor con restitución automática de fondos.
* `502 Bad Gateway`: Errores de transporte antes del procesamiento del proveedor.
* `504 Gateway Timeout`: Transacciones en espera de conciliación (se preserva el cargo preventivo y folio UPC para evitar cargos duplicados).

### 5. Integración Robusta y Segura con GestoPago (`POST /sistema/service/sendTx.do`)
Para cumplir los estrictos requerimientos de consistencia financiera:
1. **Administración Segura del Token Bearer**:
   * Administrado vía variables de entorno (`GESTOPAGO_BEARER_TOKEN`, `GESTOPAGO_AUTH_PASSWORD`) o renovado de forma segura mediante un cliente Feign dedicado.
   * Almacenado en PostgreSQL con cifrado AES-CBC (`ENC:...`), jamás expuesto en texto plano, logs ni respuestas HTTP.
   * Interceptor Feign dinámico (`GestoPagoSendTxFeignConfig`) que añade el encabezado `Authorization: Bearer <token>`.
2. **Estrategia de Débito en Dos Fases (Reserva & Compensación)**:
   * **Fase 1 (Reserva Preventiva)**: Bloqueo pesimista de la cuenta (`SELECT ... FOR UPDATE`), validación de titularidad, saldo suficiente e idempotencia. Se descuenta el saldo y se registra en `EN_PROCESO` liberando el lock y conexión antes de la llamada de red.
   * **Fase 2 (Invocación Externa)**: Formulario cifrado AES con `random` + `signed`. Cliente Feign con `Retryer.NEVER_RETRY` y timeouts estrictos.
   * **Fase 3 (Resolución de Estatus)**:
     * Si el proveedor aprueba: confirma éxito (`EXITOSA`).
     * Si el proveedor rechaza con certeza: restituye el saldo íntegramente a la cuenta (`RECHAZADA`).
     * Si ocurre Timeout o interrupción de red: **NO asume fallo ni reversa ciegamente**. Pasa a `PENDIENTE_CONCILIACION`.
3. **Mecanismos de Conciliación e Idempotencia**:
   * Verificación inmediata mediante `/sistema/service/verifyTx.do`.
   * Proceso programado (Cron Job cada 5 min) para conciliar transacciones pendientes.
   * Endpoint bajo demanda `POST /servicios/pagos/{upc}/conciliar`.
   * Endpoint de consulta `GET /servicios/pagos/{upc}` e historial paginado `GET /servicios/historial`.
4. **Sanitización de Datos**:
   * `LogSanitizer` enmascara patrones `Bearer`, passwords, claves criptográficas y números de cuenta (`****1234`) en todos los logs.

## Stack Tecnológico

- **Java 17 / 21**
- **Spring Boot 3.3.6**
  - Spring Web
  - Spring Data JPA (PostgreSQL)
  - Spring Data MongoDB
  - Spring Cloud OpenFeign (Cliente HTTP)
- **Base de Datos**: PostgreSQL 16 & MongoDB.
- **Procesamiento de XML**: Jakarta XML Bind API & DocumentBuilder con protección OWASP XXE.

## Uso

Para lanzar la aplicación de manera local:

```bash
./gradlew clean build
./gradlew bootRun
```

