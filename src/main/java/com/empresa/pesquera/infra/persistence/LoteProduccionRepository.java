package com.empresa.pesquera.infra.persistence;

import com.empresa.pesquera.domain.entity.LoteProduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoteProduccionRepository extends JpaRepository<LoteProduccion, Long> {
    Optional<LoteProduccion> findByCodigoLote(String codigoLote);
}