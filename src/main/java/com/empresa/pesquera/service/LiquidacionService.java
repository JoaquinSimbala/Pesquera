package com.empresa.pesquera.service;

import com.empresa.pesquera.model.*;
import com.empresa.pesquera.repository.LiquidacionPagoRepository;
import com.empresa.pesquera.repository.TrabajadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class LiquidacionService {

    private final LiquidacionPagoRepository liquidacionPagoRepository;
    private final TrabajadorRepository trabajadorRepository;

    public LiquidacionService(LiquidacionPagoRepository liquidacionPagoRepository, TrabajadorRepository trabajadorRepository) {
        this.liquidacionPagoRepository = liquidacionPagoRepository;
        this.trabajadorRepository = trabajadorRepository;
    }

    public Map<String, Double> tarifasOficiales() {
        return Map.of("Apoyos", 0.80, "Limpieza", 1.50, "Clasificado", 1.00, "Envasado", 0.90);
    }

    public List<LiquidacionPago> listarLiquidaciones() {
        return liquidacionPagoRepository.findAllByOrderByFechaRegistroDesc();
    }

    public PlanLiquidacionForm construirPlanDesdeAsignacion(AsignacionService.AsignacionResultado resultado, CalculoCarga calculo) {
        PlanLiquidacionForm plan = new PlanLiquidacionForm();
        List<ItemLiquidacionForm> items = new ArrayList<>();
        double horas = calculo.getTiempoObjetivo() != null ? Math.max(0, calculo.getTiempoObjetivo() - 1) : 0;

        for (Map.Entry<String, List<AsignacionService.TrabajadorConRendimiento>> entry : resultado.getAsignaciones().entrySet()) {
            for (AsignacionService.TrabajadorConRendimiento t : entry.getValue()) {
                ItemLiquidacionForm item = new ItemLiquidacionForm();
                item.setRolOperativo(entry.getKey());
                item.setTrabajadorId(t.getTrabajador().getId());
                item.setNombreTrabajador(t.getTrabajador().getNombreCompleto());
                item.setKilosProcesados(redondear(t.getRendimiento() * horas));
                items.add(item);
            }
        }
        plan.setItems(items);
        return plan;
    }

    @Transactional
    public void registrarLiquidaciones(PlanLiquidacionForm plan) {
        Map<String, Double> tarifas = tarifasOficiales();
        for (ItemLiquidacionForm item : plan.getItems()) {
            Trabajador t = trabajadorRepository.findById(item.getTrabajadorId()).orElseThrow();
            double tarifa = tarifas.getOrDefault(t.getRolOperativo(), 1.0);
            LiquidacionPago pago = new LiquidacionPago();
            pago.setTrabajador(t);
            pago.setKilosProcesados(item.getKilosProcesados());
            pago.setTarifaPorKilo(tarifa);
            pago.setMontoTotal(redondear(item.getKilosProcesados() * tarifa));
            pago.setFechaProduccion(LocalDate.now());
            pago.setAprobado(false);
            pago.setFechaRegistro(LocalDateTime.now());
            liquidacionPagoRepository.save(pago);
        }
    }

    @Transactional
    public void aprobarLiquidacion(Long id) {
        LiquidacionPago p = liquidacionPagoRepository.findById(id).orElseThrow();
        p.setAprobado(true);
        p.setFechaAprobacion(LocalDateTime.now());
        liquidacionPagoRepository.save(p);
    }

    public ResumenLiquidacion construirResumen(List<LiquidacionPago> list) {
        double tot = list.stream().mapToDouble(LiquidacionPago::getMontoTotal).sum();
        double apr = list.stream().filter(LiquidacionPago::getAprobado).mapToDouble(LiquidacionPago::getMontoTotal).sum();
        return new ResumenLiquidacion(list.size(), liquidacionPagoRepository.countByAprobadoFalse(), redondear(tot), redondear(apr), redondear(tot - apr));
    }

    private double redondear(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public PlanLiquidacionForm crearFormularioVacio() {
        return new PlanLiquidacionForm();
    }

    public Map<String, List<Trabajador>> trabajadoresDisponiblesPorRol() {
        String[] roles = {"Apoyos", "Limpieza", "Clasificado", "Envasado"};
        Map<String, List<Trabajador>> data = new LinkedHashMap<>();
        for (String rol : roles) {
            data.put(rol, trabajadorRepository.findByRolOperativoAndDisponibleTrue(rol));
        }
        return data;
    }

    public static class ResumenLiquidacion {
        private final int totalRegistros;
        private final long pendientesAprobacion;
        private final double montoTotal;
        private final double montoAprobado;
        private final double montoPendiente;

        public ResumenLiquidacion(int tr, long pa, double mt, double ma, double mp) {
            this.totalRegistros = tr;
            this.pendientesAprobacion = pa;
            this.montoTotal = mt;
            this.montoAprobado = ma;
            this.montoPendiente = mp;
        }

        public int getTotalRegistros() {
            return totalRegistros;
        }

        public long getPendientesAprobacion() {
            return pendientesAprobacion;
        }

        public double getMontoTotal() {
            return montoTotal;
        }

        public double getMontoAprobado() {
            return montoAprobado;
        }

        public double getMontoPendiente() {
            return montoPendiente;
        }
    }
}