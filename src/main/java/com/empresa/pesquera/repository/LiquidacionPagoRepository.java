package com.empresa.pesquera.repository;

import com.empresa.pesquera.model.LiquidacionPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiquidacionPagoRepository extends JpaRepository<LiquidacionPago, Long> {
    List<LiquidacionPago> findAllByOrderByFechaRegistroDesc();
    long countByAprobadoFalse();
}
