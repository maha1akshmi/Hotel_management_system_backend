package com.example.hms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // JWT Bearer auth scheme
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter your JWT token (without 'Bearer ' prefix)");

        return new OpenAPI()
                .info(new Info()
                        .title("Hotel Management System API")
                        .description("REST API for the Hotel Booking System — Admin Dashboard Module. "
                                + "Provides endpoints for managing users, hotels, bookings, and viewing system statistics.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Team HMS")
                                .email("team@hms.dev"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerScheme))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"));
    }
}
