package com.allset.allset.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@RestController
@RequestMapping("/photos")
class PhotoUploadController {

    private val uploadDir = Paths.get("uploads").toAbsolutePath().normalize()

    init {
        Files.createDirectories(uploadDir)
    }

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadPhoto(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body("File is empty")
        }

        val originalName = file.originalFilename ?: "image"
        val fileName = UUID.randomUUID().toString() + "_" + originalName
        val targetPath = uploadDir.resolve(fileName)

        file.inputStream.use { input ->
            Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
        }

        val fileUrl = "/uploads/$fileName"
        return ResponseEntity.ok(fileUrl)
    }
}
