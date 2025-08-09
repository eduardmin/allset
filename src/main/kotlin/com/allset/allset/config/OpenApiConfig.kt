package com.allset.allset.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val bearerScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("Paste Google JWT: Authorization: Bearer <token>")

        return OpenAPI()
            .info(
                Info()
                    .title("AllSet Wedding Invitations API")
                    .description("API for invitations, templates, confirmations, users")
                    .version("v1.0.0")
                    .contact(Contact().name("AllSet").email("hello@allset.example"))
                    .license(License().name("Apache 2.0"))
            )
            .components(Components().addSecuritySchemes("bearerAuth", bearerScheme))
            .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
    }
}
