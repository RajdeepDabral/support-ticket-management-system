package com.supportticket.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI supportTicketOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Support Ticket Management API")
                        .description("REST API for creating, updating, searching, and transitioning support tickets.")
                        .version("v1"));
    }
}
