package com.allset.allset.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Serve uploaded files from file system
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/")
        
        // Serve template images from classpath resources
        registry.addResourceHandler("/templates/**")
            .addResourceLocations("classpath:/static/templates/")
    }
}
