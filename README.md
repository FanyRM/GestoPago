# GestoPago Integration Service

Este proyecto es un microservicio construido en **Spring Boot 3.3.6** cuyo objetivo es integrarse con el proveedor de servicios "GestoPago" para obtener, sincronizar y exponer el catálogo de productos disponibles para transacciones.

## 🚀 Descripción de la Solución

El sistema no consulta el catálogo de productos en tiempo real al proveedor externo en cada petición de los clientes, ya que el catálogo es una lista de datos que cambia con poca frecuencia. En su lugar, el sistema emplea una estrategia de **Caché Caliente (Hot Cache)**.

El flujo principal consta de dos partes:
1. **Sincronización Programada (Cron Job)**: Un proceso en segundo plano (Scheduler) se ejecuta diariamente (por defecto a las 06:00 AM hora local) para autenticarse, llamar al endpoint XML del catálogo de GestoPago y guardar los datos consolidados en la base de datos.
2. **Consulta Rápida (API REST)**: Los clientes consumen un endpoint local rápido (`GET /gestopago/catalogo/productos`) que devuelve instantáneamente la información desde la base de datos.

## 🏗 Decisiones Técnicas y Arquitectura

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
Se evita el uso de genéricos como `ResponseEntity<?>` y `RuntimeException`.
* **Razón**: Se introdujo un enumerador centralizado (`ApiResponseEnum`) combinado con una excepción personalizada (`CatalogoException`). Esto garantiza que, ante cualquier fallo (caída de MongoDB, fallo de GestoPago), el cliente siempre reciba un modelo JSON consistente con un código y mensaje predecibles.

## 🛠 Stack Tecnológico

- **Java 17**
- **Spring Boot 3.3.6**
  - Spring Web
  - Spring Data JPA (PostgreSQL)
  - Spring Data MongoDB
  - Spring Cloud OpenFeign (Cliente HTTP)
- **Base de Datos**: PostgreSQL 16 & MongoDB.
- **Procesamiento de XML**: Jakarta XML Bind API.

## ⚙️ Uso

Para lanzar la aplicación de manera local, es necesario contar con Java 17 y acceso a los motores de base de datos definidos en el `application.properties`:

```bash
./gradlew clean build
./gradlew bootRun
```
