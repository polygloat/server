package io.tolgee.security

import io.tolgee.configuration.tolgee.TolgeeProperties
import io.tolgee.exceptions.AuthenticationException
import io.tolgee.service.UserAccountService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerExceptionResolver
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class DisabledAuthenticationFilter @Autowired constructor(
  private val configuration: TolgeeProperties,
  @param:Qualifier("handlerExceptionResolver")
  private val resolver: HandlerExceptionResolver,
  private val userAccountService: UserAccountService,
  private val authenticationProvider: AuthenticationProvider

) : OncePerRequestFilter() {
  @Throws(ServletException::class, IOException::class)
  override fun doFilterInternal(
    req: HttpServletRequest,
    res: HttpServletResponse,
    filterChain: FilterChain
  ) {
    try {
      if (!this.configuration.authentication.enabled) {
        SecurityContextHolder.getContext().authentication =
          authenticationProvider.getAuthentication(userAccountService.implicitUser)
      }
      filterChain.doFilter(req, res)
    } catch (e: AuthenticationException) {
      resolver.resolveException(req, res, null, e)
    }
  }
}
