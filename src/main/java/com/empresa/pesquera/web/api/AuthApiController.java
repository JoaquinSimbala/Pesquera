package com.empresa.pesquera.web.api;

import com.empresa.pesquera.application.dto.auth.JwtAuthResponse;
import com.empresa.pesquera.application.dto.auth.JwtLoginRequest;
import com.empresa.pesquera.domain.entity.Usuario;
import com.empresa.pesquera.infra.persistence.UsuarioRepository;
import com.empresa.pesquera.infra.security.jwt.JwtCookieService;
import com.empresa.pesquera.infra.security.jwt.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final JwtCookieService jwtCookieService;

    public AuthApiController(AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            JwtService jwtService,
            JwtCookieService jwtCookieService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.jwtCookieService = jwtCookieService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody JwtLoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));

            Usuario usuario = usuarioRepository.findByUsername(request.username())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            String token = jwtService.generarToken(usuario, authentication.getAuthorities());
            ResponseCookie cookieJwt = jwtCookieService.crearCookieJwt(token, jwtService.extraerExpiracion(token));
            JwtAuthResponse response = new JwtAuthResponse(
                    "Bearer",
                    usuario.getUsername(),
                    usuario.getRol(),
                    jwtService.extraerExpiracion(token));

            return ResponseEntity.status(HttpStatus.OK)
                    .header("Set-Cookie", cookieJwt.toString())
                    .body(response);
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Usuario o contraseña incorrectos"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        ResponseCookie cookieJwt = jwtCookieService.eliminarCookieJwt();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Sesión cerrada");

        return ResponseEntity.ok()
                .header("Set-Cookie", cookieJwt.toString())
                .body(response);
    }
}
