package com.empresa.pesquera.infra.persistence;

import com.empresa.pesquera.domain.entity.InventarioDistribucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InventarioDistribucionRepository extends JpaRepository<InventarioDistribucion, Long> {

    List<InventarioDistribucion> findAllByOrderByFechaRegistroDesc();

    @Query("SELECT COALESCE(SUM(i.kilosTotales), 0) FROM InventarioDistribucion i WHERE i.destino = :destino AND i.fechaRegistro >= :fecha")
    Double sumarKilosPorDestinoYFecha(@Param("destino") String destino, @Param("fecha") LocalDate fecha);

    @Query("SELECT DISTINCT i.destino FROM InventarioDistribucion i")
    List<String> findDistinctDestinos();

    @Query("SELECT COALESCE(SUM(i.kilosTotales), 0) FROM InventarioDistribucion i WHERE i.loteReferencia = :lote")
    Double sumarKilosPorLote(@Param("lote") String lote);
}