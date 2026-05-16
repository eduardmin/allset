package com.allset.allset.config

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(
        ex: ResponseStatusException,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, Any?>> {
        val status = ex.statusCode.value()
        val body = mapOf(
            "timestamp" to Instant.now().toString(),
            "status" to status,
            "error" to (HttpStatus.resolve(status)?.reasonPhrase ?: "Error"),
            "message" to (ex.reason ?: ""),
            "path" to request.requestURI
        )
        return ResponseEntity.status(ex.statusCode).body(body)
    }
}
