package com.allset.allset.service

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class Auth0UserInfoService(
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private val issuerUri: String
) {
    private val logger = LoggerFactory.getLogger(Auth0UserInfoService::class.java)
    private val webClient = WebClient.builder().build()

    data class UserInfo(
        val sub: String,
        val email: String?,
        val name: String?,
        @JsonProperty("nickname") val nickname: String?,
        val picture: String?
    )

    fun getUserInfo(accessToken: String): UserInfo? {
        return try {
            val userInfoUrl = "${issuerUri}userinfo"
            logger.info("🔍 Fetching user info from: $userInfoUrl")
            
            val userInfo = webClient.get()
                .uri(userInfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono<UserInfo>()
                .block()
            
            logger.info("✅ UserInfo retrieved: email=${userInfo?.email}, name=${userInfo?.name}")
            userInfo
        } catch (e: Exception) {
            logger.error("❌ Error fetching user info from Auth0", e)
            null
        }
    }
}

