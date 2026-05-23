package com.empresa.pesquera.infra.security.jwt;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtCookieService {

    private final JwtProperties jwtProperties;

    public JwtCookieService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public ResponseCookie crearCookieJwt(String token, Instant expiration) {
        long maxAgeSeconds = expiration == null
                ? Duration.ofDays(1).toSeconds()
                : Math.max(0, Duration.between(Instant.now(), expiration).toSeconds());

        return ResponseCookie.from(jwtProperties.getCookieName(), token)
                .httpOnly(true)
            .secure(jwtProperties.isCookieSecure())
            .sameSite(jwtProperties.getCookieSameSite())
            .path(jwtProperties.getCookiePath())
                .maxAge(maxAgeSeconds)
                .build();
    }

    public ResponseCookie eliminarCookieJwt() {
        return ResponseCookie.from(jwtProperties.getCookieName(), "")
                .httpOnly(true)
            .secure(jwtProperties.isCookieSecure())
            .sameSite(jwtProperties.getCookieSameSite())
            .path(jwtProperties.getCookiePath())
                .maxAge(0)
                .build();
    }
}