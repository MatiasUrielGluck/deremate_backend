package com.matiasugluck.deremate_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Deremate Backend API",
                version = "1.0",
                description = "Documentación de la API para Deremate Backend"
        ),
        servers = {
                @Server(url = "http://localhost:4002", description = "Servidor Local"),
                @Server(url = "https://sebastian-zafra.com", description = "Servidor de Producción")
        },
        security = @SecurityRequirement(name = "bearerAuth") // Aplica la seguridad globalmente
)
@SecurityScheme(
        name = "bearerAuth", // Nombre de tu esquema de seguridad (lo usarás en @SecurityRequirement)
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT", // Formato del token (opcional, para documentación)
        scheme = "bearer", // Tipo de esquema HTTP
        in = SecuritySchemeIn.HEADER, // Dónde se envía el token (en la cabecera)
        description = "JWT Authorization header using the Bearer scheme. Example: \"Bearer {token}\""
)
public class OpenApiConfig {
    // No necesitas métodos aquí si todo está definido con anotaciones.
    // Opcionalmente, puedes definir un Bean de OpenAPI si necesitas más personalización programática.
}