package com.elocate.elocate.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 * Provides API documentation accessible at /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public OpenAPI elocateOpenAPI() {
        // Security scheme for JWT Bearer token
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("Enter your JWT token (without 'Bearer ' prefix)");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                .info(new Info()
                        .title("Elocate API")
                        .description("E-waste recycling platform API documentation. " +
                                "This API provides endpoints for user management, device recycling, " +
                                "facility management, and wallet operations.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Elocate Team")
                                .email("kasumalesh@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url(contextPath.equals("/") ? "" : contextPath)
                                .description("Current Server"),
                        new Server()
                                .url("https://elocate-server-m2zi.onrender.com/elocate")
                                .description("Production Server (Render)"),
                        new Server()
                                .url("http://localhost:8080/elocate")
                                .description("Local Development Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}
