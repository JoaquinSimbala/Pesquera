package com.empresa.pesquera.application.service;

import com.empresa.pesquera.application.dto.form.RegistroInventarioForm;
import com.empresa.pesquera.application.dto.form.RegistroLoteForm;
import com.empresa.pesquera.domain.entity.InventarioDistribucion;
import com.empresa.pesquera.domain.entity.LoteProduccion;
import com.empresa.pesquera.infra.persistence.InventarioDistribucionRepository;
import com.empresa.pesquera.infra.persistence.LoteProduccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class InventarioService {

    private final InventarioDistribucionRepository distribucionRepository;
    private final LoteProduccionRepository loteRepository;

    public InventarioService(InventarioDistribucionRepository distribucionRepository, LoteProduccionRepository loteRepository) {
        this.distribucionRepository = distribucionRepository;
        this.loteRepository = loteRepository;
    }

    @Transactional
    public void registrarIngresoLote(RegistroLoteForm form) {
        if (loteRepository.findByCodigoLote(form.getCodigoLote()).isPresent()) {
            throw new IllegalArgumentException("El lote ya existe en el almacen.");
        }
        LoteProduccion lote = new LoteProduccion();
        lote.setCodigoLote(form.getCodigoLote());
        lote.setKilosIniciales(form.getKilosIniciales());
        lote.setFechaRegistro(LocalDate.now());
        loteRepository.save(lote);
    }

    public Map<String, Double> obtenerLotesDisponibles() {
        Map<String, Double> disponibles = new LinkedHashMap<>();
        List<LoteProduccion> lotes = loteRepository.findAll();

        for (LoteProduccion lote : lotes) {
            Double distribuidos = distribucionRepository.sumarKilosPorLote(lote.getCodigoLote());
            Double restante = lote.getKilosIniciales() - distribuidos;
            if (restante > 0) {
                disponibles.put(lote.getCodigoLote(), restante);
            }
        }
        return disponibles;
    }

    public List<String> obtenerDestinos() {
        List<String> destinosDB = distribucionRepository.findDistinctDestinos();
        if (destinosDB.isEmpty()) {
            return Arrays.asList("Supermercados", "Mercado Mayorista", "Exportacion", "Mercado Local");
        }
        return destinosDB;
    }

    @Transactional
    public void registrarDistribucion(RegistroInventarioForm form) {
        LoteProduccion lote = loteRepository.findByCodigoLote(form.getLoteReferencia())
                .orElseThrow(() -> new IllegalArgumentException("El lote ingresado no existe."));

        Double yaDistribuidos = distribucionRepository.sumarKilosPorLote(form.getLoteReferencia());
        Double stockDisponible = lote.getKilosIniciales() - yaDistribuidos;

        if (form.getKilosTotales() > stockDisponible) {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + stockDisponible + " kg.");
        }

        InventarioDistribucion inv = new InventarioDistribucion();
        inv.setLoteReferencia(form.getLoteReferencia());
        inv.setKilosTotales(form.getKilosTotales());
        inv.setDestino(form.getDestino());
        inv.setFechaRegistro(LocalDate.now());
        distribucionRepository.save(inv);
    }

    public List<InventarioDistribucion> listarHistorial() {
        return distribucionRepository.findAllByOrderByFechaRegistroDesc();
    }

    public Map<String, Double> obtenerResumenMetricas() {
        Map<String, Double> metricas = new LinkedHashMap<>();
        LocalDate ultimoMes = LocalDate.now().minusMonths(1);
        List<String> destinos = obtenerDestinos();

        for (String dest : destinos) {
            metricas.put(dest, distribucionRepository.sumarKilosPorDestinoYFecha(dest, ultimoMes));
        }
        return metricas;
    }
}