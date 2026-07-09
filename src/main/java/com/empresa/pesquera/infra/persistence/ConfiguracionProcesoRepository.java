package com.empresa.pesquera.infra.persistence;

import com.empresa.pesquera.domain.entity.ConfiguracionProceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracionProcesoRepository extends JpaRepository<ConfiguracionProceso, Long> {

    @Query("SELECT c FROM ConfiguracionProceso c WHERE c.especie.nombre = :especie AND c.rol.nombre = :rol")
    Optional<ConfiguracionProceso> findByEspecieAndRol(@Param("especie") String especie, @Param("rol") String rol);

    @Query("SELECT c FROM ConfiguracionProceso c WHERE c.especie.nombre = :especie")
    List<ConfiguracionProceso> findByEspecie(@Param("especie") String especie);

    @Query("SELECT c FROM ConfiguracionProceso c WHERE c.especie.id = :especieId AND c.rol.id = :rolId")
    Optional<ConfiguracionProceso> findByEspecieIdAndRolId(@Param("especieId") Long especieId, @Param("rolId") Long rolId);

    @Query("SELECT c FROM ConfiguracionProceso c WHERE c.especie.id = :especieId")
    List<ConfiguracionProceso> findByEspecieId(@Param("especieId") Long especieId);
}
