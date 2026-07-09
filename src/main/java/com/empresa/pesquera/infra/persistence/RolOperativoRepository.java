package com.empresa.pesquera.infra.persistence;

import com.empresa.pesquera.domain.entity.RolOperativo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolOperativoRepository extends JpaRepository<RolOperativo, Long> {
    Optional<RolOperativo> findByNombre(String nombre);
}
