package com.empresa.pesquera.application.service;

import com.empresa.pesquera.application.dto.form.RegistroInventarioForm;
import com.empresa.pesquera.domain.entity.InventarioDistribucion;
import com.empresa.pesquera.infra.persistence.InventarioDistribucionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class InventarioService {

    private final InventarioDistribucionRepository repository;

    private final Map<String, Double> lotesProduccion = Map.of(
            "LOTE-10", 5000.0,
            "LOTE-11", 3500.0,
            "LOTE-12", 1200.0
    );

    public InventarioService(InventarioDistribucionRepository repository) {
        this.repository = repository;
    }

    public Map<String, Double> obtenerLotesDisponibles() {
        Map<String, Double> disponibles = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : lotesProduccion.entrySet()) {
            Double distribuidos = repository.sumarKilosPorLote(entry.getKey());
            Double restante = entry.getValue() - distribuidos;
            if (restante > 0) {
                disponibles.put(entry.getKey(), restante);
            }
        }
        return disponibles;
    }

    public List<String> obtenerDestinos() {
        List<String> destinosDB = repository.findDistinctDestinos();
        if (destinosDB.isEmpty()) {
            return Arrays.asList("Supermercados", "Mercado Mayorista", "Exportacion", "Mercado Local");
        }
        return destinosDB;
    }

    @Transactional
    public void registrarDistribucion(RegistroInventarioForm form) {
        Double limiteLote = lotesProduccion.get(form.getLoteReferencia());
        if (limiteLote == null) {
            throw new IllegalArgumentException("El lote ingresado no existe o no tiene stock.");
        }

        Double yaDistribuidos = repository.sumarKilosPorLote(form.getLoteReferencia());
        Double stockDisponible = limiteLote - yaDistribuidos;

        if (form.getKilosTotales() > stockDisponible) {
            throw new IllegalArgumentException("Stock insuficiente. El " + form.getLoteReferencia() + " solo tiene " + stockDisponible + " kg disponibles.");
        }

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
        List<String> destinos = obtenerDestinos();

        for (String dest : destinos) {
            metricas.put(dest, repository.sumarKilosPorDestinoYFecha(dest, ultimoMes));
        }
        return metricas;
    }
}