package com.empresa.pesquera.infra.persistence;

import com.empresa.pesquera.domain.entity.CostoOperacional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CostoOperacionalRepository extends JpaRepository<CostoOperacional, Long> {

    List<CostoOperacional> findAllByOrderByFechaCostoDescFechaRegistroDesc();

    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM CostoOperacional c")
    Double sumarTotalGeneral();

    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM CostoOperacional c WHERE c.fechaCosto >= :fechaInicio")
    Double sumarDesdeFecha(@Param("fechaInicio") LocalDate fechaInicio);

    @Query("SELECT c.categoria, COALESCE(SUM(c.monto), 0) FROM CostoOperacional c WHERE c.fechaCosto >= :fechaInicio GROUP BY c.categoria ORDER BY c.categoria")
    List<Object[]> sumarPorCategoriaDesdeFecha(@Param("fechaInicio") LocalDate fechaInicio);
}
