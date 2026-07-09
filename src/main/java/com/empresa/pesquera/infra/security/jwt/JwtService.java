package com.empresa.pesquera.infra.security.jwt;

import com.empresa.pesquera.domain.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private static final String ROLE_CLAIM = "rol";

    private final JwtProperties jwtProperties;
    private final Environment env;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties, Environment env) {
        this.jwtProperties = jwtProperties;
        this.env = env;
        this.secretKey = resolverClaveFirma(jwtProperties.getSecret());
    }

    public String generarToken(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
        String rol = obtenerRolPrincipal(usuario, authorities);
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plusMillis(jwtProperties.getExpirationMs());

        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim(ROLE_CLAIM, rol)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        Object rol = extraerClaims(token).get(ROLE_CLAIM);
        return rol == null ? "" : rol.toString();
    }

    public Instant extraerExpiracion(String token) {
        Date expiration = extraerClaims(token).getExpiration();
        return expiration == null ? null : expiration.toInstant();
    }

    public boolean esTokenValido(String token) {
        try {
            Claims claims = extraerClaims(token);
            return claims.getSubject() != null && claims.getExpiration() != null && claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    private String obtenerRolPrincipal(Usuario usuario, Collection<? extends GrantedAuthority> authorities) {
        if (usuario.getRol() != null && !usuario.getRol().isBlank()) {
            return usuario.getRol().trim();
        }

        if (authorities != null) {
            for (GrantedAuthority authority : authorities) {
                if (authority != null && authority.getAuthority() != null && authority.getAuthority().startsWith("ROLE_")) {
                    return authority.getAuthority().substring(5);
                }
            }
        }

        return "";
    }

    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey resolverClaveFirma(String secret) {
        if (secret == null || secret.isBlank()) {
            boolean isProd = java.util.Arrays.asList(env.getActiveProfiles()).contains("prod");
            if (isProd) {
                logger.error("FATAL: JWT_SECRET no está configurado en el perfil de producción (prod).");
                throw new IllegalStateException("JWT_SECRET es obligatorio para arrancar en perfil de producción (prod).");
            }
            logger.warn("JWT_SECRET no está configurado; se generó una clave temporal para desarrollo.");
            return generarClaveTemporal();
        }

        byte[] keyBytes = decodificarClave(secret);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes para firmar con HS256.");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey generarClaveTemporal() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            keyGenerator.init(256);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No se pudo generar una clave temporal para JWT.", ex);
        }
    }

    private byte[] decodificarClave(String value) {
        try {
            return Decoders.BASE64.decode(value);
        } catch (Exception ex) {
            try {
                return Base64.getDecoder().decode(value);
            } catch (Exception ignored) {
                return value.getBytes(StandardCharsets.UTF_8);
            }
        }
    }
}
