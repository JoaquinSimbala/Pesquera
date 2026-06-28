package com.empresa.pesquera.application.service;

import com.empresa.pesquera.application.dto.form.CalculoCarga;
import com.empresa.pesquera.domain.entity.RendimientoDiario;
import com.empresa.pesquera.domain.entity.Trabajador;
import com.empresa.pesquera.infra.persistence.RendimientoDiarioRepository;
import com.empresa.pesquera.infra.persistence.TrabajadorRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AsignacionService {

    private final TrabajadorRepository trabajadorRepository;
    private final RendimientoDiarioRepository rendimientoRepository;

    public AsignacionService(TrabajadorRepository trabajadorRepository,
            RendimientoDiarioRepository rendimientoRepository) {
        this.trabajadorRepository = trabajadorRepository;
        this.rendimientoRepository = rendimientoRepository;
    }

    private static final Map<String, Double> VALORES_GENERICOS = Map.of(
            "Apoyos", 500.0,
            "Limpieza", 60.0,
            "Clasificado", 250.0,
            "Envasado", 150.0);

    public AsignacionResultado sugerirAsignacionGlobal(CalculoCarga calculo) {
        Map<String, List<TrabajadorConRendimiento>> asignaciones = new LinkedHashMap<>();

        if (calculo.getKilos() == null || calculo.getTiempoObjetivo() == null || calculo.getTiempoObjetivo() <= 1) {
            return new AsignacionResultado(asignaciones, false, 0);
        }

        double tiempoEfectivo = calculo.getTiempoObjetivo() - 1;
        double tasaRequeridaGlobal = calculo.getKilos() / tiempoEfectivo;

        String[] roles = { "Apoyos", "Limpieza", "Clasificado", "Envasado" };
        boolean hayDeficit = false;
        double tiempoEfectivoMinimoNecesario = tiempoEfectivo;

        for (String rol : roles) {
            ResultadoRol res = asignarRol(rol, tasaRequeridaGlobal);
            asignaciones.put(rol, res.trabajadores);

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

        return new AsignacionResultado(asignaciones, hayDeficit, horasRecomendadasFijas);
    }

    private ResultadoRol asignarRol(String rol, double tasaRequerida) {
        List<Trabajador> disponibles = trabajadorRepository.findByRolOperativoAndDisponibleTrue(rol);

        if (disponibles.isEmpty()) {
            return new ResultadoRol(new ArrayList<>(), 0);
        }

        List<Long> trabajadorIds = disponibles.stream().map(Trabajador::getId).toList();

        List<RendimientoDiario> historiales = rendimientoRepository.findTop10PerTrabajador(trabajadorIds);

        Map<Long, List<RendimientoDiario>> agrupados = historiales.stream()
                .collect(Collectors.groupingBy(r -> r.getTrabajador().getId()));

        List<TrabajadorConRendimiento> candidatos = new ArrayList<>();
        for (Trabajador t : disponibles) {
            List<RendimientoDiario> top10 = agrupados.getOrDefault(t.getId(), new ArrayList<>());
            double promedio = calcularPromedioDesdeLista(t, top10);
            candidatos.add(new TrabajadorConRendimiento(t, promedio));
        }

        Collections.shuffle(candidatos);

        List<TrabajadorConRendimiento> asignados = new ArrayList<>();
        double sumaCapacidad = 0;

        for (TrabajadorConRendimiento candidato : candidatos) {
            if (sumaCapacidad >= tasaRequerida) {
                break;
            }
            asignados.add(candidato);
            sumaCapacidad += candidato.rendimiento;
        }

        return new ResultadoRol(asignados, sumaCapacidad);
    }

    public double calcularPromedioUltimos10Dias(Trabajador trabajador) {
        List<RendimientoDiario> historial = rendimientoRepository.findTop10ByTrabajadorOrderByFechaDesc(trabajador);
        return calcularPromedioDesdeLista(trabajador, historial);
    }

    public double calcularPromedioDesdeLista(Trabajador trabajador, List<RendimientoDiario> historial) {
        if (historial.isEmpty()) {
            return VALORES_GENERICOS.getOrDefault(trabajador.getRolOperativo(), 30.0);
        }

        double totalKilos = 0;
        double totalHoras = 0;

        for (RendimientoDiario rd : historial) {
            totalKilos += rd.getKilosProcesados();
            totalHoras += rd.getHorasTrabajadas();
        }

        if (totalHoras == 0) {
            return VALORES_GENERICOS.getOrDefault(trabajador.getRolOperativo(), 30.0);
        }
        return totalKilos / totalHoras;
    }

    public static class ResultadoRol {
        List<TrabajadorConRendimiento> trabajadores;
        double capacidadTotal;

        public ResultadoRol(List<TrabajadorConRendimiento> t, double c) {
            this.trabajadores = t;
            this.capacidadTotal = c;
        }
    }

    public static class AsignacionResultado {
        private final Map<String, List<TrabajadorConRendimiento>> asignaciones;
        private final boolean deficitPersonal;
        private final int horasRecomendadas;

        public AsignacionResultado(Map<String, List<TrabajadorConRendimiento>> asignaciones, boolean deficitPersonal,
                int horasRecomendadas) {
            this.asignaciones = asignaciones;
            this.deficitPersonal = deficitPersonal;
            this.horasRecomendadas = horasRecomendadas;
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
