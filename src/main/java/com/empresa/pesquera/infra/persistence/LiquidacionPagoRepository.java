package com.empresa.pesquera.infra.persistence;

import com.empresa.pesquera.domain.entity.LiquidacionPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LiquidacionPagoRepository extends JpaRepository<LiquidacionPago, Long> {
    @EntityGraph(attributePaths = {"trabajador", "trabajador.rolOperativo"})
    Page<LiquidacionPago> findAllByOrderByFechaRegistroDesc(Pageable pageable);

    long countByAprobadoFalse();

    @Query("SELECT COALESCE(SUM(l.montoTotal), 0) FROM LiquidacionPago l")
    Double sumarMontoTotalGeneral();

    @Query("SELECT COALESCE(SUM(l.montoTotal), 0) FROM LiquidacionPago l WHERE l.aprobado = true")
    Double sumarMontoAprobadoGeneral();
}
