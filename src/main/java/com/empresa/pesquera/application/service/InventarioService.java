package com.empresa.pesquera.application.service;

import com.empresa.pesquera.application.dto.form.RegistroInventarioForm;
import com.empresa.pesquera.domain.entity.InventarioDistribucion;
import com.empresa.pesquera.infra.persistence.InventarioDistribucionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InventarioService {

    private final InventarioDistribucionRepository repository;

    public InventarioService(InventarioDistribucionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void registrarDistribucion(RegistroInventarioForm form) {
        InventarioDistribucion inv = new InventarioDistribucion();
        inv.setLoteReferencia(form.getLoteReferencia());
        inv.setKilosTotales(form.getKilosTotales());
        inv.setDestino(form.getDestino());
        inv.setFechaRegistro(LocalDate.now());
        repository.save(inv);
    }

    public List<InventarioDistribucion> listarHistorial() {
        return repository.findAllByOrderByFechaRegistroDesc();
    }

    public Map<String, Double> obtenerResumenMetricas() {
        Map<String, Double> metricas = new LinkedHashMap<>();

        LocalDate ultimoMes = LocalDate.now().minusMonths(1);

        metricas.put("Supermercados", repository.sumarKilosPorDestinoYFecha("Supermercados", ultimoMes));
        metricas.put("Mercado Mayorista", repository.sumarKilosPorDestinoYFecha("Mercado Mayorista", ultimoMes));
        metricas.put("Exportacion", repository.sumarKilosPorDestinoYFecha("Exportacion", ultimoMes));

        return metricas;
    }
}