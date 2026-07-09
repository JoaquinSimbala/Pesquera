package com.empresa.pesquera.application.service;

import com.empresa.pesquera.domain.entity.*;
import com.empresa.pesquera.infra.persistence.*;
import com.empresa.pesquera.infra.security.SecurityHelper;
import com.empresa.pesquera.application.dto.form.ItemLiquidacionForm;
import com.empresa.pesquera.application.dto.form.PlanLiquidacionForm;
import com.empresa.pesquera.application.dto.form.CalculoCarga;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ConfiguracionProcesoRepository configuracionProcesoRepository;
    private final AsignacionRepository asignacionRepository;
    private final RendimientoDiarioRepository rendimientoRepository;
    private final EspecieRepository especieRepository;
    private final RolOperativoRepository rolOperativoRepository;
    private final SecurityHelper securityHelper;

    public LiquidacionService(LiquidacionPagoRepository liquidacionPagoRepository,
                             TrabajadorRepository trabajadorRepository,
                             ConfiguracionProcesoRepository configuracionProcesoRepository,
                             AsignacionRepository asignacionRepository,
                             RendimientoDiarioRepository rendimientoRepository,
                             EspecieRepository especieRepository,
                             RolOperativoRepository rolOperativoRepository,
                             SecurityHelper securityHelper) {
        this.liquidacionPagoRepository = liquidacionPagoRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.configuracionProcesoRepository = configuracionProcesoRepository;
        this.asignacionRepository = asignacionRepository;
        this.rendimientoRepository = rendimientoRepository;
        this.especieRepository = especieRepository;
        this.rolOperativoRepository = rolOperativoRepository;
        this.securityHelper = securityHelper;
    }

    public Map<String, Double> obtenerTarifasPorEspecie(String especie) {
        List<ConfiguracionProceso> configs = configuracionProcesoRepository.findByEspecie(especie);
        Map<String, Double> tarifas = new LinkedHashMap<>();
        for (ConfiguracionProceso cfg : configs) {
            tarifas.put(cfg.getRol().getNombre(), cfg.getTarifaPorKilo());
        }
        
        
        if (tarifas.isEmpty()) {
            tarifas.put("Apoyos", 0.05);
            tarifas.put("Limpieza", 0.15);
            tarifas.put("Clasificado", 0.12);
            tarifas.put("Envasado", 0.10);
        }
        return tarifas;
    }

    public Map<String, Double> tarifasOficiales() {
        return obtenerTarifasPorEspecie("Pulpo");
    }

    public Page<LiquidacionPago> listarLiquidaciones(Pageable pageable) {
        return liquidacionPagoRepository.findAllByOrderByFechaRegistroDesc(pageable);
    }

    @Transactional
    public void registrarUnaLiquidacion(Long trabajadorId, Double kilosProcesados, String especie) {
        Especie esp = especieRepository.findByNombre(especie)
                .orElseThrow(() -> new IllegalArgumentException("Especie no encontrada: " + especie));
        registrarUnaLiquidacionConEspecieId(trabajadorId, kilosProcesados, esp.getId());
    }

    @Transactional
    public void registrarUnaLiquidacionConEspecieId(Long trabajadorId, Double kilosProcesados, Long especieId) {
        Usuario usuarioActual = securityHelper.getUsuarioActual();
        Trabajador t = trabajadorRepository.findById(trabajadorId)
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado."));

        double tarifa = configuracionProcesoRepository.findByEspecieIdAndRolId(especieId, t.getRolOperativo().getId())
                .map(ConfiguracionProceso::getTarifaPorKilo)
                .orElseGet(() -> {
                    return switch (t.getRolOperativo().getNombre()) {
                        case "Apoyos" -> 0.05;
                        case "Limpieza" -> 0.15;
                        case "Clasificado" -> 0.12;
                        default -> 0.10;
                    };
                });

        LiquidacionPago pago = new LiquidacionPago();
        pago.setTrabajador(t);
        pago.setKilosProcesados(kilosProcesados);
        pago.setTarifaPorKilo(tarifa);
        pago.setMontoTotal(redondear(kilosProcesados * tarifa));
        pago.setFechaProduccion(LocalDate.now());
        pago.setAprobado(false);
        pago.setTipoProceso("PRODUCCION");
        pago.setFechaRegistro(LocalDateTime.now());
        pago.setUsuario(usuarioActual);

        liquidacionPagoRepository.save(pago);

        
        double horasTrabajadas = 8.0; 
        Optional<Asignacion> asignacionOpt = asignacionRepository.findByTrabajadorId(trabajadorId);
        if (asignacionOpt.isPresent()) {
            double tiempoObj = asignacionOpt.get().getTiempoObjetivo();
            horasTrabajadas = Math.max(1.0, tiempoObj - 1.0);
        }

        
        registrarRendimientoYActualizarPromedio(t, kilosProcesados, horasTrabajadas);
    }

    @Transactional
    public void registrarRendimientoYActualizarPromedio(Trabajador t, double kilos, double horas) {
        RendimientoDiario rd = new RendimientoDiario();
        rd.setTrabajador(t);
        rd.setFecha(LocalDate.now());
        rd.setHorasTrabajadas(horas);
        rd.setKilosProcesados(kilos);
        rendimientoRepository.save(rd);

        
        List<RendimientoDiario> historial = rendimientoRepository.findTop10ByTrabajadorOrderByFechaDesc(t);
        double totalKilos = 0;
        double totalHoras = 0;
        for (RendimientoDiario r : historial) {
            totalKilos += r.getKilosProcesados();
            totalHoras += r.getHorasTrabajadas();
        }
        
        double nuevoPromedio = totalHoras > 0 ? (totalKilos / totalHoras) : (kilos / horas);
        nuevoPromedio = Math.round(nuevoPromedio * 100.0) / 100.0;
        
        t.setRendimientoPromedio(nuevoPromedio);
        trabajadorRepository.save(t);
    }

    @Transactional
    public void registrarLoteDeLiquidaciones(String especie, List<Map<String, Object>> trabajadores) {
        Especie esp = especieRepository.findByNombre(especie)
                .orElseThrow(() -> new IllegalArgumentException("Especie no encontrada: " + especie));

        for (Map<String, Object> item : trabajadores) {
            Long trabajadorId = Long.valueOf(item.get("trabajadorId").toString());
            Double kilos = Double.valueOf(item.get("kilosProcesados").toString());
            
            
            registrarUnaLiquidacionConEspecieId(trabajadorId, kilos, esp.getId());
            
            
            Trabajador t = trabajadorRepository.findById(trabajadorId).orElseThrow();
            t.setDisponible(true);
            trabajadorRepository.save(t);

            
            Optional<Asignacion> asignacionOpt = asignacionRepository.findByTrabajadorId(trabajadorId);
            if (asignacionOpt.isPresent()) {
                Asignacion asignacion = asignacionOpt.get();
                asignacion.getTrabajadores().remove(t);
                if (asignacion.getTrabajadores().isEmpty()) {
                    asignacionRepository.delete(asignacion);
                } else {
                    asignacionRepository.save(asignacion);
                }
            }
        }
    }

    @Transactional
    public void registrarLiquidaciones(PlanLiquidacionForm plan) {
        String especie = plan.getEspecie() != null ? plan.getEspecie() : "Pulpo";
        Especie esp = especieRepository.findByNombre(especie)
                .orElseThrow(() -> new IllegalArgumentException("Especie no encontrada: " + especie));

        for (ItemLiquidacionForm item : plan.getItems()) {
            Trabajador t = trabajadorRepository.findById(item.getTrabajadorId()).orElseThrow();
            double tarifa = configuracionProcesoRepository.findByEspecieIdAndRolId(esp.getId(), t.getRolOperativo().getId())
                    .map(ConfiguracionProceso::getTarifaPorKilo)
                    .orElseGet(() -> {
                        return switch (t.getRolOperativo().getNombre()) {
                            case "Apoyos" -> 0.05;
                            case "Limpieza" -> 0.15;
                            case "Clasificado" -> 0.12;
                            default -> 0.10;
                        };
                    });
            LiquidacionPago pago = new LiquidacionPago();
            pago.setTrabajador(t);
            pago.setKilosProcesados(item.getKilosProcesados());
            pago.setTarifaPorKilo(tarifa);
            pago.setMontoTotal(redondear(item.getKilosProcesados() * tarifa));
            pago.setFechaProduccion(LocalDate.now());
            pago.setAprobado(false);
            pago.setTipoProceso("PRODUCCION");
            pago.setFechaRegistro(LocalDateTime.now());
            pago.setUsuario(securityHelper.getUsuarioActual());
            liquidacionPagoRepository.save(pago);
        }
    }

    public PlanLiquidacionForm construirPlanDesdeAsignacion(AsignacionService.AsignacionResultado resultado,
            CalculoCarga calculo) {
        PlanLiquidacionForm plan = new PlanLiquidacionForm();
        plan.setEspecie(calculo.getEspecie() != null ? calculo.getEspecie() : "Pulpo");
        List<ItemLiquidacionForm> items = new ArrayList<>();
        double horas = calculo.getTiempoObjetivo() != null ? Math.max(0, calculo.getTiempoObjetivo() - 1) : 0;

        for (Map.Entry<String, List<AsignacionService.TrabajadorConRendimiento>> entry : resultado.getAsignaciones()
                .entrySet()) {
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

    public PlanLiquidacionForm crearFormularioVacio() {
        return new PlanLiquidacionForm();
    }

    public Map<String, List<Trabajador>> trabajadoresDisponiblesPorRol() {
        String[] roles = { "Apoyos", "Limpieza", "Clasificado", "Envasado" };
        Map<String, List<Trabajador>> data = new LinkedHashMap<>();
        for (String rol : roles) {
            data.put(rol, trabajadorRepository.findByRolOperativoAndDisponibleTrue(rol));
        }
        return data;
    }

    @Transactional
    public void aprobarLiquidacion(Long id) {
        LiquidacionPago p = liquidacionPagoRepository.findById(id).orElseThrow();
        p.setAprobado(true);
        p.setFechaAprobacion(LocalDateTime.now());
        liquidacionPagoRepository.save(p);
    }

    public ResumenLiquidacion construirResumen() {
        double tot = liquidacionPagoRepository.sumarMontoTotalGeneral();
        double apr = liquidacionPagoRepository.sumarMontoAprobadoGeneral();
        long totalRegistros = liquidacionPagoRepository.count();
        long pendientesPa = liquidacionPagoRepository.countByAprobadoFalse();
        return new ResumenLiquidacion((int) totalRegistros, pendientesPa, redondear(tot),
                redondear(apr), redondear(tot - apr));
    }

    public List<Especie> listarEspecies() {
        return especieRepository.findAll();
    }

    private double redondear(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
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