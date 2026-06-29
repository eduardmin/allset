package com.allset.allset.service

import com.allset.allset.model.ApiErrorLog
import com.allset.allset.repository.ApiErrorLogRepository
import com.allset.allset.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant

@Service
class ApiErrorLogService(
    private val apiErrorLogRepository: ApiErrorLogRepository,
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(ApiErrorLogService::class.java)

    /**
     * Best-effort persistence of an API error. Never throws: a failure to log
     * must not break the error response sent back to the client.
     */
    fun logError(
        ex: Throwable,
        request: HttpServletRequest?,
        status: Int,
        error: String,
        message: String
    ) {
        try {
            val (userId, userEmail, userSub) = resolveUser()

            val entry = ApiErrorLog(
                timestamp = Instant.now(),
                status = status,
                error = error,
                message = message,
                path = request?.requestURI ?: "",
                method = request?.method ?: "",
                userId = userId,
                userEmail = userEmail,
                userSub = userSub,
                exceptionType = ex::class.java.name,
                stackTrace = stackTraceToString(ex),
                queryString = request?.queryString,
                clientIp = request?.let { clientIp(it) },
                userAgent = request?.getHeader("User-Agent")
            )
            apiErrorLogRepository.save(entry)
        } catch (loggingFailure: Exception) {
            log.warn("Failed to persist API error log", loggingFailure)
        }
    }

    /**
     * Reads the authenticated user straight from the JWT in the SecurityContext.
     * Intentionally avoids AuthenticationService.getCurrentUserIdOrNull() because
     * that method has user-creation side effects we don't want during error logging.
     */
    private fun resolveUser(): Triple<String?, String?, String?> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
                ?: return Triple(null, null, null)
            val jwt = authentication.principal as? Jwt ?: return Triple(null, null, null)

            val sub = jwt.getClaim<String>("sub")
            val email = jwt.getClaim<String>("email")
            val userId = sub?.let { userRepository.findBySub(it)?.id }

            Triple(userId, email, sub)
        } catch (e: Exception) {
            Triple(null, null, null)
        }
    }

    private fun clientIp(request: HttpServletRequest): String {
        val forwarded = request.getHeader("X-Forwarded-For")
        if (!forwarded.isNullOrBlank()) {
            return forwarded.split(",").first().trim()
        }
        return request.remoteAddr ?: ""
    }

    private fun stackTraceToString(ex: Throwable): String {
        val sw = StringWriter()
        ex.printStackTrace(PrintWriter(sw))
        val full = sw.toString()
        return if (full.length > MAX_STACK_TRACE_LENGTH) {
            full.substring(0, MAX_STACK_TRACE_LENGTH)
        } else {
            full
        }
    }

    companion object {
        private const val MAX_STACK_TRACE_LENGTH = 8000
    }
}
