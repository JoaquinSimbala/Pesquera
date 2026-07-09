package com.empresa.pesquera.infra.persistence;

import com.empresa.pesquera.domain.entity.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrabajadorRepository extends JpaRepository<Trabajador, Long> {

    @Query("SELECT t FROM Trabajador t WHERE t.rolOperativo.nombre = :rol AND t.disponible = true")
    List<Trabajador> findByRolOperativoAndDisponibleTrue(@Param("rol") String rolOperativo);

    @Query("SELECT t FROM Trabajador t WHERE t.rolOperativo.id = :rolId AND t.disponible = true")
    List<Trabajador> findByRolOperativoIdAndDisponibleTrue(@Param("rolId") Long rolId);
}
