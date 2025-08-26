package com.allset.allset.controller

import com.allset.allset.model.ColorPalette
import com.allset.allset.service.ColorPaletteService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/color-palettes")
class ColorPaletteController(
    private val colorPaletteService: ColorPaletteService
) {

    @GetMapping
    fun getAllColorPalettes(): List<ColorPalette> {
        return colorPaletteService.getAll()
    }

    @GetMapping("/{id}")
    fun getColorPaletteById(@PathVariable id: String): ColorPalette? {
        return colorPaletteService.getById(id)
    }

    @GetMapping("/batch")
    fun getColorPalettesByIds(@RequestParam ids: List<String>): List<ColorPalette> {
        return colorPaletteService.getByIds(ids)
    }
}
