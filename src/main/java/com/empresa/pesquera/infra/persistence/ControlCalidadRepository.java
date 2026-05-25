package com.empresa.pesquera.infra.persistence;

import com.empresa.pesquera.domain.entity.ControlCalidad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ControlCalidadRepository extends JpaRepository<ControlCalidad, Long> {
    List<ControlCalidad> findAllByOrderByFechaRegistroDesc();
}
