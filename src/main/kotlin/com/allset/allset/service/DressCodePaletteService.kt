package com.allset.allset.service

import com.allset.allset.model.DressCodePalette
import com.allset.allset.repository.DressCodePaletteRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class DressCodePaletteService(
    private val repository: DressCodePaletteRepository
) {

    fun getAll(): List<DressCodePalette> = repository.findAll()

    fun getById(id: String): DressCodePalette {
        return repository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Dress code palette not found")
        }
    }

    fun create(palette: DressCodePalette): DressCodePalette {
        return repository.save(palette.copy(id = null))
    }

    fun update(id: String, update: DressCodePalette): DressCodePalette {
        val existing = getById(id)
        val updated = existing.copy(
            name = update.name,
            description = update.description,
            colors = update.colors
        )
        return repository.save(updated)
    }

    fun delete(id: String) {
        val palette = getById(id)
        repository.delete(palette)
    }
}
