package com.allset.allset.controller

import com.allset.allset.model.Template
import com.allset.allset.service.TemplateService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/templates")
class TemplateController(private val templateService: TemplateService) {
    @GetMapping
    fun getTemplates(): List<Template> {
        return templateService.getTemplates()
    }

    @GetMapping("/{id}")
    fun getTemplateById(@PathVariable id: String): Template {
        return templateService.getTemplateById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found")
    }
}