package com.allset.allset.controller

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

    @GetMapping("/static/templates/{imageName}")
    fun getTemplateImage(@PathVariable imageName: String): ResponseEntity<Resource> {
        return try {
            val resource: Resource = ClassPathResource("static/templates/$imageName")
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build()
            }

            val contentType = when {
                imageName.endsWith(".png") -> MediaType.IMAGE_PNG
                imageName.endsWith(".jpg") || imageName.endsWith(".jpeg") -> MediaType.IMAGE_JPEG
                imageName.endsWith(".gif") -> MediaType.IMAGE_GIF
                else -> MediaType.APPLICATION_OCTET_STREAM
            }

            ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(resource)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}

