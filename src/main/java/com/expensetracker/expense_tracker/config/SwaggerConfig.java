package com.expensetracker.expense_tracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Expense Tracker API")
                        .version("1.0.0")
                        .description("A personal expense tracking application built with Spring Boot")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your.email@example.com")));
    }
}