package com.allset.allset.repository

import com.allset.allset.model.DressCodePalette
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface DressCodePaletteRepository : MongoRepository<DressCodePalette, String>
