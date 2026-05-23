package com.empresa.pesquera.infra.security.jwt;

import com.empresa.pesquera.application.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final String jwtCookieName;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioService usuarioService,
            @Value("${jwt.cookie-name:pesquera_jwt}") String jwtCookieName) {
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
        this.jwtCookieName = jwtCookieName;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/login")
                || path.equals("/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = extraerToken(request);
        if (token != null && !token.isBlank()) {
            if (!jwtService.esTokenValido(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT inválido o expirado");
                return;
            }

            String username = jwtService.extraerUsername(token);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken) {
                UserDetails userDetails = usuarioService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extraerToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            String tokenDesdeCookie = extraerTokenDesdeCookie(request);
            return tokenDesdeCookie;
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }

    private String extraerTokenDesdeCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (var cookie : request.getCookies()) {
            if (cookie != null && jwtCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
