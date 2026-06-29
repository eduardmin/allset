package com.allset.allset.config

import com.allset.allset.service.ApiErrorLogService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@RestControllerAdvice
class ApiExceptionHandler(
    private val apiErrorLogService: ApiErrorLogService
) {

    private val log = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(
        ex: ResponseStatusException,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, Any?>> {
        val status = ex.statusCode.value()
        val error = HttpStatus.resolve(status)?.reasonPhrase ?: "Error"
        val message = ex.reason ?: ""

        apiErrorLogService.logError(ex, request, status, error, message)

        return ResponseEntity.status(ex.statusCode).body(buildBody(status, error, message, request))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, Any?>> {
        val status = HttpStatus.BAD_REQUEST.value()
        val error = HttpStatus.BAD_REQUEST.reasonPhrase
        val message = ex.bindingResult.fieldErrors.joinToString("; ") {
            "${it.field}: ${it.defaultMessage}"
        }.ifBlank { "Validation failed" }

        apiErrorLogService.logError(ex, request, status, error, message)

        return ResponseEntity.status(status).body(buildBody(status, error, message, request))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, Any?>> {
        val status = HttpStatus.BAD_REQUEST.value()
        val error = HttpStatus.BAD_REQUEST.reasonPhrase
        val message = ex.message ?: "Invalid request"

        apiErrorLogService.logError(ex, request, status, error, message)

        return ResponseEntity.status(status).body(buildBody(status, error, message, request))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, Any?>> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        val error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
        val message = ex.message ?: "Unexpected error"

        log.error("Unhandled exception for ${request.method} ${request.requestURI}", ex)
        apiErrorLogService.logError(ex, request, status, error, message)

        // Do not leak internal details to the client.
        return ResponseEntity.status(status).body(buildBody(status, error, "Internal server error", request))
    }

    private fun buildBody(
        status: Int,
        error: String,
        message: String,
        request: HttpServletRequest
    ): Map<String, Any?> = mapOf(
        "timestamp" to Instant.now().toString(),
        "status" to status,
        "error" to error,
        "message" to message,
        "path" to request.requestURI
    )
}
