package com.empresa.pesquera.application.service;

import com.empresa.pesquera.application.dto.form.CalculoCarga;
import com.empresa.pesquera.domain.entity.*;
import com.empresa.pesquera.infra.persistence.*;
import com.empresa.pesquera.infra.security.SecurityHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class AsignacionService {

    private final TrabajadorRepository trabajadorRepository;
    private final ConfiguracionProcesoRepository configuracionProcesoRepository;
    private final AsignacionRepository asignacionRepository;
    private final SecurityHelper securityHelper;
    private final LiquidacionService liquidacionService;

    public AsignacionService(TrabajadorRepository trabajadorRepository,
                             ConfiguracionProcesoRepository configuracionProcesoRepository,
                             AsignacionRepository asignacionRepository,
                             SecurityHelper securityHelper,
                             LiquidacionService liquidacionService) {
        this.trabajadorRepository = trabajadorRepository;
        this.configuracionProcesoRepository = configuracionProcesoRepository;
        this.asignacionRepository = asignacionRepository;
        this.securityHelper = securityHelper;
        this.liquidacionService = liquidacionService;
    }

    public AsignacionResultado sugerirAsignacionGlobal(CalculoCarga calculo) {
        Map<String, List<TrabajadorConRendimiento>> asignaciones = new LinkedHashMap<>();

        if (calculo.getKilos() == null || calculo.getTiempoObjetivo() == null || calculo.getTiempoObjetivo() <= 1 || calculo.getEspecie() == null) {
            return new AsignacionResultado(asignaciones, false, 0, false);
        }

        double tiempoEfectivo = calculo.getTiempoObjetivo() - 1;
        double tasaRequeridaGlobal = calculo.getKilos() / tiempoEfectivo;

        String[] roles = { "Apoyos", "Limpieza", "Clasificado", "Envasado" };
        boolean hayDeficit = false;
        boolean tieneAdvertencia = false;
        double tiempoEfectivoMinimoNecesario = tiempoEfectivo;

        for (String rol : roles) {
            ResultadoRol res = asignarRol(rol, tasaRequeridaGlobal, calculo.getEspecie());
            asignaciones.put(rol, res.trabajadores);

            if (res.bajoEstandarWarning) {
                tieneAdvertencia = true;
            }

            if (res.capacidadTotal < tasaRequeridaGlobal) {
                hayDeficit = true;

                if (res.capacidadTotal > 0) {
                    double tiempoParaEsteRol = calculo.getKilos() / res.capacidadTotal;
                    if (tiempoParaEsteRol > tiempoEfectivoMinimoNecesario) {
                        tiempoEfectivoMinimoNecesario = tiempoParaEsteRol;
                    }
                }
            }
        }

        double horasRecomendadasDouble = hayDeficit ? Math.ceil(tiempoEfectivoMinimoNecesario + 1)
                : calculo.getTiempoObjetivo();
        int horasRecomendadasFijas = (int) horasRecomendadasDouble;

        return new AsignacionResultado(asignaciones, hayDeficit, horasRecomendadasFijas, tieneAdvertencia);
    }

    private ResultadoRol asignarRol(String rol, double tasaRequerida, String especie) {
        List<Trabajador> disponibles = trabajadorRepository.findByRolOperativoAndDisponibleTrue(rol);

        if (disponibles.isEmpty()) {
            return new ResultadoRol(new ArrayList<>(), 0, false);
        }

        
        double standardYield = configuracionProcesoRepository.findByEspecieAndRol(especie, rol)
                .map(ConfiguracionProceso::getRendimientoBase)
                .orElse(150.0);

        int neededCount = (int) Math.ceil(tasaRequerida / standardYield);

        
        List<TrabajadorConRendimiento> eligible = new ArrayList<>();
        List<TrabajadorConRendimiento> belowStandard = new ArrayList<>();

        for (Trabajador t : disponibles) {
            double yld = t.getRendimientoPromedio() != null ? t.getRendimientoPromedio() : standardYield;
            TrabajadorConRendimiento candidate = new TrabajadorConRendimiento(t, yld);
            if (yld >= standardYield) {
                eligible.add(candidate);
            } else {
                belowStandard.add(candidate);
            }
        }

        
        eligible.sort((c1, c2) -> Double.compare(c2.rendimiento, c1.rendimiento));
        belowStandard.sort((c1, c2) -> Double.compare(c2.rendimiento, c1.rendimiento));

        List<TrabajadorConRendimiento> selected = new ArrayList<>();
        boolean tieneBajoEstandar = false;

        if (eligible.size() >= neededCount) {
            
            int optimalTarget = (int) Math.round(neededCount * 0.7);
            int mediumTarget = neededCount - optimalTarget;

            
            int optToSelect = Math.min(optimalTarget, eligible.size());
            for (int i = 0; i < optToSelect; i++) {
                selected.add(eligible.get(i));
            }

            
            List<TrabajadorConRendimiento> remainingEligible = new ArrayList<>(eligible.subList(optToSelect, eligible.size()));

            
            int medToSelect = Math.min(mediumTarget, remainingEligible.size());
            int startIndex = remainingEligible.size() - medToSelect;
            for (int i = startIndex; i < remainingEligible.size(); i++) {
                selected.add(remainingEligible.get(i));
            }
        } else {
            
            selected.addAll(eligible);

            
            int deficit = neededCount - selected.size();
            int toSelectBelow = Math.min(deficit, belowStandard.size());
            for (int i = 0; i < toSelectBelow; i++) {
                selected.add(belowStandard.get(i));
                tieneBajoEstandar = true; 
            }
        }

        double sumaCapacidad = selected.stream().mapToDouble(c -> c.rendimiento).sum();
        return new ResultadoRol(selected, sumaCapacidad, tieneBajoEstandar);
    }

    @Transactional
    public Asignacion guardarAsignacion(CalculoCarga calculo, List<Long> trabajadorIds) {
        Usuario usuarioActual = securityHelper.getUsuarioActual();
        if (usuarioActual == null) {
            throw new IllegalStateException("Debe iniciar sesión para realizar la asignación.");
        }

        List<Trabajador> trabajadores = trabajadorRepository.findAllById(trabajadorIds);
        if (trabajadores.isEmpty()) {
            throw new IllegalArgumentException("La lista de trabajadores asignados no puede estar vacía.");
        }

        Asignacion asignacion = new Asignacion();
        asignacion.setEspecie(calculo.getEspecie());
        asignacion.setKilos(calculo.getKilos());
        asignacion.setTiempoObjetivo(calculo.getTiempoObjetivo());
        asignacion.setFechaRegistro(LocalDate.now());
        asignacion.setUsuario(usuarioActual);

        for (Trabajador t : trabajadores) {
            t.setDisponible(false);
            trabajadorRepository.save(t);
            asignacion.getTrabajadores().add(t);
        }

        return asignacionRepository.save(asignacion);
    }

    public List<Asignacion> obtenerAsignacionesActivas() {
        return asignacionRepository.findAllByOrderByFechaRegistroDesc();
    }

    @Transactional
    public void liberarTrabajadorConLiquidacion(Long trabajadorId, Double kilosProcesados) {
        Trabajador trabajador = trabajadorRepository.findById(trabajadorId)
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado."));
        
        Optional<Asignacion> asignacionOpt = asignacionRepository.findByTrabajadorId(trabajadorId);
        if (asignacionOpt.isEmpty()) {
            throw new IllegalArgumentException("El trabajador no se encuentra en ningún proceso activo.");
        }

        Asignacion asignacion = asignacionOpt.get();

        
        liquidacionService.registrarUnaLiquidacion(trabajadorId, kilosProcesados, asignacion.getEspecie());

        
        trabajador.setDisponible(true);
        trabajadorRepository.save(trabajador);

        
        asignacion.getTrabajadores().remove(trabajador);
        if (asignacion.getTrabajadores().isEmpty()) {
            asignacionRepository.delete(asignacion);
        } else {
            asignacionRepository.save(asignacion);
        }
    }

    public Map<String, Integer> calcularPersonalPulpo(CalculoCarga datos) {
        if (datos.getEspecie() == null) {
            datos.setEspecie("Pulpo");
        }
        return calcularPersonalPorEspecie(datos);
    }

    public Map<String, Integer> calcularPersonalPorEspecie(CalculoCarga datos) {
        if (datos.getKilos() == null || datos.getTiempoObjetivo() == null || datos.getEspecie() == null) {
            return new LinkedHashMap<>();
        }
        double tiempoEfectivo = datos.getTiempoObjetivo() - 1;

        if (tiempoEfectivo <= 0) {
            return new LinkedHashMap<>();
        }

        String[] roles = {"Apoyos", "Limpieza", "Clasificado", "Envasado"};
        Map<String, Integer> resultados = new LinkedHashMap<>();

        for (String rol : roles) {
            double rendimiento = configuracionProcesoRepository.findByEspecieAndRol(datos.getEspecie(), rol)
                    .map(ConfiguracionProceso::getRendimientoBase)
                    .orElseGet(() -> {
                        return switch (rol) {
                            case "Apoyos" -> 500.0;
                            case "Limpieza" -> 60.0;
                            case "Clasificado" -> 250.0;
                            default -> 150.0;
                        };
                    });

            int personas = (int) Math.ceil(datos.getKilos() / (rendimiento * tiempoEfectivo));
            resultados.put(rol, personas);
        }

        return resultados;
    }

    public static class ResultadoRol {
        List<TrabajadorConRendimiento> trabajadores;
        double capacidadTotal;
        boolean bajoEstandarWarning;

        public ResultadoRol(List<TrabajadorConRendimiento> t, double c, boolean b) {
            this.trabajadores = t;
            this.capacidadTotal = c;
            this.bajoEstandarWarning = b;
        }
    }

    public static class AsignacionResultado {
        private final Map<String, List<TrabajadorConRendimiento>> asignaciones;
        private final boolean deficitPersonal;
        private final int horasRecomendadas;
        private final boolean advertenciaRendimiento;

        public AsignacionResultado(Map<String, List<TrabajadorConRendimiento>> asignaciones, boolean deficitPersonal,
                int horasRecomendadas, boolean advertenciaRendimiento) {
            this.asignaciones = asignaciones;
            this.deficitPersonal = deficitPersonal;
            this.horasRecomendadas = horasRecomendadas;
            this.advertenciaRendimiento = advertenciaRendimiento;
        }

        public Map<String, List<TrabajadorConRendimiento>> getAsignaciones() {
            return asignaciones;
        }

        public boolean isDeficitPersonal() {
            return deficitPersonal;
        }

        public int getHorasRecomendadas() {
            return horasRecomendadas;
        }

        public boolean isAdvertenciaRendimiento() {
            return advertenciaRendimiento;
        }
    }

    public static class TrabajadorConRendimiento {
        private final Trabajador trabajador;
        private final double rendimiento;

        public TrabajadorConRendimiento(Trabajador trabajador, double rendimiento) {
            this.trabajador = trabajador;
            this.rendimiento = rendimiento;
        }

        public Trabajador getTrabajador() {
            return trabajador;
        }

        public double getRendimiento() {
            return rendimiento;
        }
    }
}
