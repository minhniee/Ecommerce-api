package com.example.auth_shop.config;

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
 * SwaggerConfig - Configuration for Swagger/OpenAPI documentation
 * 
 * This configuration sets up the OpenAPI documentation for the application.
 * Access the Swagger UI at: http://localhost:8082/swagger-ui.html
 * Access the API docs at: http://localhost:8082/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8082}")
    private String serverPort;

    @Value("${api.prefix:/api/v1}")
    private String apiPrefix;

    /**
     * OpenAPI configuration bean
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Shop API")
                        .version("1.0.0")
                        .description("""
                                RESTful API documentation for Auth Shop Application
                                
                                This API provides endpoints for:
                                - User authentication and authorization (JWT-based)
                                - Product management
                                - Category management
                                - Order management
                                - Cart and CartItem operations
                                
                                ## Authentication
                                Most endpoints require JWT authentication. To use protected endpoints:
                                1. Login via `/api/v1/auth/login` to get a JWT token
                                2. Click the "Authorize" button at the top of this page
                                3. Enter: `Bearer {your-jwt-token}` (include the word "Bearer" and a space)
                                4. Click "Authorize" and "Close"
                                5. Now you can test protected endpoints
                                """)
                        .contact(new Contact()
                                .name("Auth Shop Team")
                                .email("support@authshop.com")
                                .url("https://www.authshop.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.authshop.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authorization header using the Bearer scheme. Example: \\\"Authorization: Bearer {token}\\\"")));
    }
}



