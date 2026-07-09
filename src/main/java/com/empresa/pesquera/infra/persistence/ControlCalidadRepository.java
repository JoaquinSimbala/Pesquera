package com.empresa.pesquera.infra.persistence;

import com.empresa.pesquera.domain.entity.ControlCalidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ControlCalidadRepository extends JpaRepository<ControlCalidad, Long> {
    Page<ControlCalidad> findAllByOrderByFechaRegistroDesc(Pageable pageable);

    long countByEstadoHaccpIgnoreCase(String estadoHaccp);

    @Query("SELECT COUNT(c) FROM ControlCalidad c WHERE c.temperatura > 4.5 OR c.ph < 6.0 OR c.ph > 7.0")
    long countAlertasCriticas();
}
