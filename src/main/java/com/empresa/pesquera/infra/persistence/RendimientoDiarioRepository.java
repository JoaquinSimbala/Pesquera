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

    @Query("SELECT r FROM RendimientoDiario r JOIN FETCH r.trabajador WHERE r.trabajador.id IN :ids ORDER BY r.fecha DESC")
    List<RendimientoDiario> findByTrabajadorIdsWithJoin(@Param("ids") List<Long> ids);
}
