package com.empresa.pesquera.service;

import com.empresa.pesquera.model.CalculoCarga;
import com.empresa.pesquera.model.ItemLiquidacionForm;
import com.empresa.pesquera.model.LiquidacionPago;
import com.empresa.pesquera.model.PlanLiquidacionForm;
import com.empresa.pesquera.model.Trabajador;
import com.empresa.pesquera.repository.LiquidacionPagoRepository;
import com.empresa.pesquera.repository.TrabajadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LiquidacionService {

    private final LiquidacionPagoRepository liquidacionPagoRepository;
    private final TrabajadorRepository trabajadorRepository;

    public LiquidacionService(LiquidacionPagoRepository liquidacionPagoRepository, TrabajadorRepository trabajadorRepository) {
        this.liquidacionPagoRepository = liquidacionPagoRepository;
        this.trabajadorRepository = trabajadorRepository;
    }

    public List<LiquidacionPago> listarLiquidaciones() {
        return liquidacionPagoRepository.findAllByOrderByFechaRegistroDesc();
    }

    public Map<String, Double> tarifasBase() {
        return Map.of(
                "LAVADO", 1.20,
                "FILETEADO", 1.80
        );
    }

    public Map<String, List<Trabajador>> trabajadoresDisponiblesPorRol() {
        String[] roles = {"Apoyos", "Limpieza", "Clasificado", "Envasado"};
        Map<String, List<Trabajador>> data = new LinkedHashMap<>();
        for (String rol : roles) {
            List<Trabajador> trabajadores = new ArrayList<>(trabajadorRepository.findByRolOperativoAndDisponibleTrue(rol));
            trabajadores.sort(Comparator.comparing(Trabajador::getNombreCompleto));
            data.put(rol, trabajadores);
        }
        return data;
    }

    public PlanLiquidacionForm construirPlanDesdeAsignacion(AsignacionService.AsignacionResultado resultado, CalculoCarga calculo) {
        PlanLiquidacionForm plan = new PlanLiquidacionForm();
        List<ItemLiquidacionForm> items = new ArrayList<>();
        double horasEfectivas = calculo.getTiempoObjetivo() != null ? Math.max(0, calculo.getTiempoObjetivo() - 1) : 0;

        for (Map.Entry<String, List<AsignacionService.TrabajadorConRendimiento>> entry : resultado.getAsignaciones().entrySet()) {
            String rol = entry.getKey();
            for (AsignacionService.TrabajadorConRendimiento candidato : entry.getValue()) {
                ItemLiquidacionForm item = new ItemLiquidacionForm();
                item.setRolOperativo(rol);
                item.setTrabajadorId(candidato.getTrabajador().getId());
                item.setTipoProceso("LAVADO");
                item.setTarifaPorKilo(1.20);
                item.setFechaProduccion(LocalDate.now());
                item.setKilosProcesados(redondear(candidato.getRendimiento() * horasEfectivas));
                items.add(item);
            }
        }

        plan.setItems(items);
        return plan;
    }

    public PlanLiquidacionForm crearFormularioVacio() {
        return new PlanLiquidacionForm();
    }

    @Transactional
    public void registrarLiquidaciones(PlanLiquidacionForm plan) {
        for (ItemLiquidacionForm item : plan.getItems()) {
            Trabajador trabajador = trabajadorRepository.findById(item.getTrabajadorId())
                    .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado."));

            LiquidacionPago pago = new LiquidacionPago();
            pago.setTrabajador(trabajador);
            pago.setTipoProceso(item.getTipoProceso());
            pago.setKilosProcesados(item.getKilosProcesados());
            pago.setTarifaPorKilo(item.getTarifaPorKilo());
            pago.setMontoTotal(calcularMonto(item.getKilosProcesados(), item.getTarifaPorKilo()));
            pago.setFechaProduccion(item.getFechaProduccion());
            pago.setAprobado(false);
            pago.setFechaRegistro(LocalDateTime.now());
            liquidacionPagoRepository.save(pago);
        }
    }

    @Transactional
    public void aprobarLiquidacion(Long id) {
        LiquidacionPago pago = liquidacionPagoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Liquidacion no encontrada."));

        if (Boolean.TRUE.equals(pago.getAprobado())) {
            return;
        }

        pago.setAprobado(true);
        pago.setFechaAprobacion(LocalDateTime.now());
        liquidacionPagoRepository.save(pago);
    }

    public ResumenLiquidacion construirResumen(List<LiquidacionPago> liquidaciones) {
        double total = liquidaciones.stream().mapToDouble(LiquidacionPago::getMontoTotal).sum();
        double aprobado = liquidaciones.stream()
                .filter(LiquidacionPago::getAprobado)
                .mapToDouble(LiquidacionPago::getMontoTotal)
                .sum();
        double pendiente = total - aprobado;

        return new ResumenLiquidacion(
                liquidaciones.size(),
                liquidacionPagoRepository.countByAprobadoFalse(),
                redondear(total),
                redondear(aprobado),
                redondear(pendiente)
        );
    }

    private double calcularMonto(double kilos, double tarifa) {
        return redondear(kilos * tarifa);
    }

    private double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static class ResumenLiquidacion {
        private final int totalRegistros;
        private final long pendientesAprobacion;
        private final double montoTotal;
        private final double montoAprobado;
        private final double montoPendiente;

        public ResumenLiquidacion(int totalRegistros, long pendientesAprobacion, double montoTotal, double montoAprobado, double montoPendiente) {
            this.totalRegistros = totalRegistros;
            this.pendientesAprobacion = pendientesAprobacion;
            this.montoTotal = montoTotal;
            this.montoAprobado = montoAprobado;
            this.montoPendiente = montoPendiente;
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
