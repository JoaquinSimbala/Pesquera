package com.empresa.pesquera.infra.persistence;

import com.empresa.pesquera.domain.entity.LiquidacionPago;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiquidacionPagoRepository extends JpaRepository<LiquidacionPago, Long> {
    @EntityGraph(attributePaths = "trabajador")
    List<LiquidacionPago> findAllByOrderByFechaRegistroDesc();

    long countByAprobadoFalse();
}
