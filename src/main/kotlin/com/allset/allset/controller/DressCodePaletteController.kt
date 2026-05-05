package com.allset.allset.controller

import com.allset.allset.model.DressCodePalette
import com.allset.allset.service.DressCodePaletteService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/dress-code-palettes")
class DressCodePaletteController(
    private val dressCodePaletteService: DressCodePaletteService
) {

    @GetMapping
    fun getAll(): List<DressCodePalette> = dressCodePaletteService.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String): DressCodePalette = dressCodePaletteService.getById(id)
}
