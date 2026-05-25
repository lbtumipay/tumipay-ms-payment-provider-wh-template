# ==========================================================
# Stage 1 — Build Native Image
# ==========================================================
# En esta primera etapa se construye el ejecutable nativo
# del microservicio utilizando GraalVM Native Image.
# Este proceso compila la aplicación Spring Boot en un
# binario optimizado que ya no necesita una JVM para ejecutarse.

FROM ghcr.io/graalvm/native-image-community:25 AS builder

# Establece el directorio de trabajo dentro del contenedor
# donde se copiará el código fuente y se ejecutará el proceso
# de compilación del microservicio.
WORKDIR /build


# Instala Maven dentro de la imagen de GraalVM para poder
# ejecutar el proceso de construcción del proyecto.
# Luego se limpia el cache del gestor de paquetes para
# reducir el tamaño final de la capa.
RUN microdnf install -y maven \
    && microdnf clean all

# ----------------------------------------------------------
# Cache de dependencias Maven
# ----------------------------------------------------------
# Primero se copia únicamente el archivo pom.xml para que
# Docker pueda cachear la descarga de dependencias.
# Esto acelera significativamente builds posteriores
# cuando el código cambia pero las dependencias no.
COPY pom.xml .

# Descarga todas las dependencias del proyecto en modo offline.
# -B  : modo batch (para CI/CD)
# -ntp: evita mostrar progreso de descarga para reducir logs
RUN mvn -B -ntp dependency:go-offline

# ----------------------------------------------------------
# Copia del código fuente
# ----------------------------------------------------------
# Una vez descargadas las dependencias se copia el código
# del microservicio al contenedor.
# Esto invalida la cache solo cuando cambia el código fuente.
COPY src ./src

# Copia la configuración de Lombok necesaria para el proceso
# de compilación del proyecto.
COPY lombok.config .

# ----------------------------------------------------------
# Compilación del ejecutable nativo
# ----------------------------------------------------------
# Compila la aplicación utilizando GraalVM Native Image.
# El resultado será un ejecutable binario optimizado que
# se genera dentro de la carpeta /build/target.
#
# - clean: limpia builds anteriores
# - native:compile: genera el ejecutable nativo
# -DskipTests: omite la ejecución de pruebas para acelerar el build
RUN mvn -B -ntp clean native:compile -DskipTests


# ==========================================================
# Stage 2 — Runtime
# ==========================================================
# En esta segunda etapa se construye la imagen final que
# ejecutará el microservicio en producción.
#
# Se utiliza una imagen distroless que contiene únicamente
# las librerías necesarias para ejecutar el binario.
# Esto reduce considerablemente el tamaño del contenedor
# y mejora la seguridad al eliminar herramientas innecesarias.

FROM gcr.io/distroless/cc-debian12

# Define el directorio de trabajo donde se ejecutará
# el microservicio dentro del contenedor.
WORKDIR /app

# Copia el ejecutable nativo generado en la etapa anterior
# (builder) hacia la imagen final de runtime.
# Solo se copia el binario necesario, evitando incluir
# código fuente o herramientas de compilación.
COPY --from=builder /build/target/microservice /app/

# Ejecuta el contenedor con un usuario no privilegiado
# por motivos de seguridad.
# Esto evita que el proceso tenga permisos de root
# dentro del contenedor.
USER nonroot:nonroot

# Expone el puerto en el que el microservicio escuchará
# conexiones HTTP dentro del contenedor.
EXPOSE 8000

# Define el ejecutable principal que se lanzará cuando
# el contenedor inicie.
ENTRYPOINT ["/app/microservice"]

# Argumentos por defecto que se envían al ejecutable
# al iniciar el contenedor.
# En este caso se define el puerto del servidor HTTP
# de Spring Boot.
CMD ["--server.port=8000"]