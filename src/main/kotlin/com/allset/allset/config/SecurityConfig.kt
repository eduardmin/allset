package com.allset.allset.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.jwt.JwtDecoder
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
                    "/health",
                    "/uploads/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**"
                ).permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
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
        val granted = JwtGrantedAuthoritiesConverter().apply { setAuthorityPrefix("ROLE_") }
        return JwtAuthenticationConverter().apply { setJwtGrantedAuthoritiesConverter(granted) }
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        logger.info("🔑 Configuring JWT Decoder with Google's public keys...")
        return NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build()
    }

    // Permissive CORS for local dev — adjust origins as needed
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val cfg = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:3000", "http://localhost:8080")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", cfg)
        }
    }
}
