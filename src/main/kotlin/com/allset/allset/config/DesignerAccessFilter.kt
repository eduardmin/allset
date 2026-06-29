package com.allset.allset.config

import com.allset.allset.model.UserRole
import com.allset.allset.repository.UserRepository
import com.allset.allset.service.AuthenticationService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class DesignerAccessFilter(
    private val userRepository: UserRepository,
    private val authenticationService: AuthenticationService
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(DesignerAccessFilter::class.java)

    // Auth-required but NOT designer-gated: a plain USER must be able to read their
    // status and submit an application before they have the DESIGNER role.
    private val openPaths = setOf("/designer/status", "/designer/apply")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val uri = request.requestURI
        if (!uri.startsWith("/designer")) {
            filterChain.doFilter(request, response)
            return
        }
        if (uri in openPaths) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val userId = authenticationService.getCurrentUserIdOrNull()
            if (userId == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required")
                return
            }

            val user = userRepository.findById(userId).orElse(null)
            if (user == null || (user.role != UserRole.DESIGNER && user.role != UserRole.ADMIN)) {
                log.warn("Designer filter - access denied. Role is: ${user?.role}")
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Designer access required")
                return
            }

            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            log.error("Designer filter - error during access check", e)
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error checking designer access: ${e.message}"
            )
        }
    }
}
