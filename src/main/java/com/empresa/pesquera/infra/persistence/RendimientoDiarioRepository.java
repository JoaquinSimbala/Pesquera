package com.empresa.pesquera.infra.persistence;

import com.empresa.pesquera.domain.entity.RendimientoDiario;
import com.empresa.pesquera.domain.entity.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RendimientoDiarioRepository extends JpaRepository<RendimientoDiario, Long> {
    List<RendimientoDiario> findTop10ByTrabajadorOrderByFechaDesc(Trabajador trabajador);

    List<RendimientoDiario> findByTrabajadorInOrderByFechaDesc(List<Trabajador> trabajadores);

    @Query(value = "SELECT * FROM (SELECT r.*, ROW_NUMBER() OVER(PARTITION BY trabajador_id ORDER BY fecha DESC) as rn FROM rendimientos_diarios r WHERE trabajador_id IN :trabajadorIds) as sub WHERE rn <= 10", nativeQuery = true)
    List<RendimientoDiario> findTop10PerTrabajador(@Param("trabajadorIds") List<Long> trabajadorIds);
}
