package com.allset.allset.controller

import com.allset.allset.model.Template
import com.allset.allset.service.TemplateService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/templates")
class TemplateController(private val templateService: TemplateService) {
    @GetMapping
    fun getTemplates(): List<Template> {
        return templateService.getTemplates()
    }
}