package com.identity.api.security;

import com.identity.core.exception.InvalidTokenException;
import com.identity.core.security.AuthenticatedUser;
import com.identity.core.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public JwtAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring("Bearer ".length()).trim();
            if (!token.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    AuthenticatedUser user = tokenService.parseAccessToken(token);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            java.util.List.of()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (InvalidTokenException ignored) {
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
