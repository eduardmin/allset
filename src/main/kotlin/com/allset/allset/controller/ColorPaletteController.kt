package com.allset.allset.controller

import com.allset.allset.model.ColorPalette
import com.allset.allset.model.DressCodePalette
import com.allset.allset.service.ColorPaletteService
import com.allset.allset.service.DressCodePaletteService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/color-palettes")
class ColorPaletteController(
    private val colorPaletteService: ColorPaletteService,
    private val dressCodePaletteService: DressCodePaletteService
) {

    @GetMapping
    fun getAllColorPalettes(): List<DressCodePalette> {
        return dressCodePaletteService.getAll()
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
