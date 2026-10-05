package org.example.studentmanagementsystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /** Name of the security scheme. Controllers refer to it with @SecurityRequirement. */
    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI studentOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Student Management System API")
                        .description("Students, departments, courses, JWT login, photo upload, "
                                + "pagination, search and Redis caching")
                        .version("1.0"))
                // Declares "Authorization: Bearer <JWT>" so Swagger UI shows the Authorize button
                .components(new Components().addSecuritySchemes(BEARER_AUTH,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}