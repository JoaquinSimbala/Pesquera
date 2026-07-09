package com.empresa.pesquera.infra.security;

import com.empresa.pesquera.domain.entity.Usuario;
import com.empresa.pesquera.infra.persistence.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityHelper {

    private final UsuarioRepository usuarioRepository;

    public SecurityHelper(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario getUsuarioActual() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return usuarioRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
