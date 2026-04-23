package com.empresa.pesquera.repository;

import com.empresa.pesquera.model.RendimientoDiario;
import com.empresa.pesquera.model.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RendimientoDiarioRepository extends JpaRepository<RendimientoDiario, Long> {
    List<RendimientoDiario> findTop10ByTrabajadorOrderByFechaDesc(Trabajador trabajador);
}
