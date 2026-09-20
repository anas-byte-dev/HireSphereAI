package com.hiresphere.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenApiConfig - Swagger UI and OpenAPI 3.0 documentation for HireSphere AI.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HireSphere AI - Real-Time Autonomous Talent & Placement API")
                        .version("1.0.0")
                        .description("REST API documentation for HireSphere AI platform featuring persistent real-time database, live SSE event streaming, and Gemini Agentic AI.")
                        .contact(new Contact()
                                .name("HireSphere Engineering Team")
                                .email("support@hiresphere.ai"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
