# Etapa de compilación
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copiamos los archivos de gradle
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# Descargamos las dependencias de gradle
RUN ./gradlew dependencies --no-daemon

# Copiamos el código fuente y compilamos
COPY src/ src/
RUN ./gradlew clean build -x test --no-daemon

# Etapa de ejecución
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copiamos el JAR generado desde la etapa de compilación
COPY --from=build /app/build/libs/*.jar app.jar

# Exponemos el puerto (por defecto Spring Boot usa el 8080)
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
