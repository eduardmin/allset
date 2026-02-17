package com.allset.allset.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig {

    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private lateinit var issuerUri: String

    @Value("\${spring.security.oauth2.resourceserver.jwt.audience}")
    private lateinit var audience: String

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        logger.info("🔐 Initializing Security Configuration...")

        http
            .csrf { it.disable() }
            .cors { } // enable CORS support

            .authorizeHttpRequests { auth ->
                auth.requestMatchers(
                    "/",
                    "/public/**",
                    "/login",
                    "/error",
                    "/templates",
                    "/templates/**",
                    "/static/**",
                    "/color-palettes",
                    "/color-palettes/**",
                    "/health",
                    "/uploads/**",
                    "/promo-codes/preview",
                    "/confirmations/guest",
                    "/confirmations/filters",
                    "/confirmations/invitation/**",
                    "/auth/login",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**"
                ).permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                    jwt.decoder(jwtDecoder())
                }
            }
            .csrf { csrf -> csrf.disable() }

            .logout { it.logoutSuccessUrl("/").permitAll() }

        http.addFilterBefore({ request, response, chain ->
            val authentication = SecurityContextHolder.getContext().authentication
            logger.info("🔑 Authenticated User: $authentication")
            chain.doFilter(request, response)
        }, UsernamePasswordAuthenticationFilter::class.java)

        logger.info("✅ Security configuration loaded successfully.")
        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val grantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_")

        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter)
        return converter
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        logger.info("🔑 Configuring JWT Decoder with Auth0...")
        logger.info("📍 Issuer URI: $issuerUri")
        logger.info("📍 Audience: $audience")
        
        val jwtDecoder = NimbusJwtDecoder.withJwkSetUri("$issuerUri.well-known/jwks.json")
            .build()

        val issuerValidator: OAuth2TokenValidator<Jwt> = JwtValidators.createDefaultWithIssuer(issuerUri)
        val audienceValidator = AudienceValidator(audience)
        
        val validators: OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(
            issuerValidator,
            audienceValidator
        )

        jwtDecoder.setJwtValidator(validators)
        
        return jwtDecoder
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:5173", "http://localhost:3000")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}

// Audience Validator
class AudienceValidator(private val audience: String) : OAuth2TokenValidator<Jwt> {
    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        val audiences = jwt.audience
        return if (audiences.contains(audience)) {
            OAuth2TokenValidatorResult.success()
        } else {
            val error = OAuth2Error(
                OAuth2ErrorCodes.INVALID_TOKEN,
                "The token does not contain the required audience: $audience",
                null
            )
            OAuth2TokenValidatorResult.failure(error)
        }
    }
}
