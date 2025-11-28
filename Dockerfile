# Etapa de construcción (Build)
FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /workspace/app

# Copiar archivos necesarios para maven
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# Dar permisos de ejecución al wrapper y descargar dependencias
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Copiar el código fuente
COPY src src

# Compilar la aplicación saltando tests para acelerar el deploy
RUN ./mvnw clean package -DskipTests

# Etapa final (Run)
FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
# Copiar el jar generado desde la etapa de construcción
COPY --from=build /workspace/app/target/*.jar app.jar

# Render asigna el puerto automáticamente en la variable PORT
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]