package com.empresa.pesquera.infra.security;

import com.empresa.pesquera.domain.entity.Auditoria;
import com.empresa.pesquera.domain.entity.Usuario;
import com.empresa.pesquera.infra.persistence.AuditoriaRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final SecurityHelper securityHelper;

    public AuditoriaService(AuditoriaRepository auditoriaRepository, SecurityHelper securityHelper) {
        this.auditoriaRepository = auditoriaRepository;
        this.securityHelper = securityHelper;
    }

    public void registrar(String accion, String detalle) {
        Usuario usuario = securityHelper.getUsuarioActual();
        if (usuario != null) {
            registrar(usuario, accion, detalle);
        }
    }

    public void registrar(Usuario usuario, String accion, String detalle) {
        if (usuario != null) {
            Auditoria auditoria = new Auditoria(usuario, accion, detalle, LocalDateTime.now());
            auditoriaRepository.save(auditoria);
        }
    }
}
