package com.empresa.pesquera.infra.persistence;

import com.empresa.pesquera.domain.entity.Asignacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {
    
    @EntityGraph(attributePaths = {"trabajadores", "trabajadores.rolOperativo", "usuario"})
    List<Asignacion> findAllByOrderByFechaRegistroDesc();

    @Query("SELECT a FROM Asignacion a JOIN a.trabajadores t WHERE t.id = :trabajadorId")
    Optional<Asignacion> findByTrabajadorId(@Param("trabajadorId") Long trabajadorId);
}
