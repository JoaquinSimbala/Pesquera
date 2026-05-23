package com.empresa.pesquera.infra.persistence;

import com.empresa.pesquera.domain.entity.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrabajadorRepository extends JpaRepository<Trabajador, Long> {
    List<Trabajador> findByRolOperativoAndDisponibleTrue(String rolOperativo);
}
