package com.allset.allset.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.core.context.SecurityContextHolder

@Configuration
@EnableWebSecurity
class SecurityConfig {

    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        logger.info("🔐 Initializing Security Configuration...")

        http
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/", "/api/public/**", "/api/login", "/api/error", "/templates", "/health", "/uploads/**").permitAll()
                auth.anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
            .csrf { csrf -> csrf.disable() }
            .logout { logout ->
                logout.logoutSuccessUrl("/").permitAll()
            }

        http.addFilterBefore({ request, response, chain ->
            val authentication = SecurityContextHolder.getContext().authentication
            logger.info("🔑 Authenticated User: $authentication")
            chain.doFilter(request, response)
        }, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter::class.java)

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
        logger.info("🔑 Configuring JWT Decoder with Google's public keys...")
        return NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build()
    }
}
