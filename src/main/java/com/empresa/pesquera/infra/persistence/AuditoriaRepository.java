package com.empresa.pesquera.infra.persistence;

import com.empresa.pesquera.domain.entity.Auditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    Page<Auditoria> findAllByOrderByFechaDesc(Pageable pageable);

    Page<Auditoria> findByUsuarioIdOrderByFechaDesc(Long usuarioId, Pageable pageable);

    Page<Auditoria> findByFechaGreaterThanEqualOrderByFechaDesc(LocalDateTime fechaInicio, Pageable pageable);

    Page<Auditoria> findByUsuarioIdAndFechaGreaterThanEqualOrderByFechaDesc(Long usuarioId, LocalDateTime fechaInicio, Pageable pageable);

    Page<Auditoria> findByFechaBetweenOrderByFechaDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    Page<Auditoria> findByUsuarioIdAndFechaBetweenOrderByFechaDesc(Long usuarioId, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
}
