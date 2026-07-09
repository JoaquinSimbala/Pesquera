package com.empresa.pesquera.infra.persistence;

import com.empresa.pesquera.domain.entity.Especie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EspecieRepository extends JpaRepository<Especie, Long> {
    Optional<Especie> findByNombre(String nombre);
}
