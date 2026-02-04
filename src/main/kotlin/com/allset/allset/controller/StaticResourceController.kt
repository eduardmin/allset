package com.allset.allset.controller

import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class StaticResourceController {

    private val logger = LoggerFactory.getLogger(StaticResourceController::class.java)

    @GetMapping("/static/templates/list")
    fun listTemplates(): ResponseEntity<Map<String, Any>> {
        val templates = listOf("classic_elegance.png", "modern_romance.png", "rustic_love_story.png")
        val result = templates.map { name ->
            val resource = ClassPathResource("static/templates/$name")
            name to mapOf(
                "exists" to resource.exists(),
                "path" to "static/templates/$name"
            )
        }.toMap()
        
        return ResponseEntity.ok(result)
    }

    @GetMapping("/static/templates/{imageName}")
    fun getTemplateImage(@PathVariable imageName: String): ResponseEntity<Resource> {
        logger.info("🖼️ Requesting template image: $imageName")
        
        return try {
            val resourcePath = "static/templates/$imageName"
            logger.info("📂 Looking for resource at: $resourcePath")
            
            val resource: Resource = ClassPathResource(resourcePath)
            logger.info("📍 Resource exists: ${resource.exists()}, URI: ${resource.uri}")
            
            if (!resource.exists()) {
                logger.warn("❌ Resource not found: $resourcePath")
                return ResponseEntity.notFound().build()
            }

            val contentType = when {
                imageName.endsWith(".png") -> MediaType.IMAGE_PNG
                imageName.endsWith(".jpg") || imageName.endsWith(".jpeg") -> MediaType.IMAGE_JPEG
                imageName.endsWith(".gif") -> MediaType.IMAGE_GIF
                else -> MediaType.APPLICATION_OCTET_STREAM
            }

            logger.info("✅ Serving image: $imageName with content type: $contentType")
            
            ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(resource)
        } catch (e: Exception) {
            logger.error("💥 Error serving template image: $imageName", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}


