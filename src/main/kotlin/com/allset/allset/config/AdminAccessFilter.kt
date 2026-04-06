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
class AdminAccessFilter(
    private val userRepository: UserRepository,
    private val authenticationService: AuthenticationService
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(AdminAccessFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (!request.requestURI.startsWith("/admin")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val userId = authenticationService.getCurrentUserIdOrNull()
            log.info("Admin filter - resolved userId: $userId")

            if (userId == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required")
                return
            }

            val user = userRepository.findById(userId).orElse(null)
            log.info("Admin filter - user found: ${user != null}, email: ${user?.email}, role: ${user?.role}")

            if (user == null || user.role != UserRole.ADMIN) {
                log.warn("Admin filter - access denied. Role is: ${user?.role}")
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required")
                return
            }

            log.info("Admin filter - admin access granted for: ${user.email}")
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            log.error("Admin filter - error during admin check", e)
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error checking admin access: ${e.message}")
        }
    }
}
