package com.empresa.pesquera.application.service;

import com.empresa.pesquera.domain.entity.ControlCalidad;
import com.empresa.pesquera.domain.entity.CostoOperacional;
import com.empresa.pesquera.domain.entity.InventarioDistribucion;
import com.empresa.pesquera.domain.entity.LiquidacionPago;
import com.empresa.pesquera.domain.entity.LoteProduccion;
import com.empresa.pesquera.infra.persistence.ControlCalidadRepository;
import com.empresa.pesquera.infra.persistence.CostoOperacionalRepository;
import com.empresa.pesquera.infra.persistence.InventarioDistribucionRepository;
import com.empresa.pesquera.infra.persistence.LiquidacionPagoRepository;
import com.empresa.pesquera.infra.persistence.LoteProduccionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GestionService {

    private final LoteProduccionRepository loteRepository;
    private final InventarioDistribucionRepository distribucionRepository;
    private final ControlCalidadRepository calidadRepository;
    private final CostoOperacionalRepository costoRepository;
    private final LiquidacionPagoRepository liquidacionRepository;

    public GestionService(LoteProduccionRepository loteRepository,
                          InventarioDistribucionRepository distribucionRepository,
                          ControlCalidadRepository calidadRepository,
                          CostoOperacionalRepository costoRepository,
                          LiquidacionPagoRepository liquidacionRepository) {
        this.loteRepository = loteRepository;
        this.distribucionRepository = distribucionRepository;
        this.calidadRepository = calidadRepository;
        this.costoRepository = costoRepository;
        this.liquidacionRepository = liquidacionRepository;
    }

    public Map<String, Object> obtenerDashboardGestion() {
        Map<String, Object> dashboard = new LinkedHashMap<>();

        
        List<LoteProduccion> lotes = loteRepository.findAll();
        double totalKilosProcesados = lotes.stream().mapToDouble(LoteProduccion::getKilosIniciales).sum();

        List<InventarioDistribucion> distribuciones = distribucionRepository.findAll();
        double totalKilosDistribuidos = distribuciones.stream().mapToDouble(InventarioDistribucion::getKilosTotales).sum();

        Map<String, Double> distribucionPorDestino = distribuciones.stream()
                .collect(Collectors.groupingBy(
                        InventarioDistribucion::getDestino,
                        Collectors.summingDouble(InventarioDistribucion::getKilosTotales)
                ));

        Map<String, Object> metricaProduccion = new LinkedHashMap<>();
        metricaProduccion.put("kilosProcesados", totalKilosProcesados);
        metricaProduccion.put("kilosDistribuidos", totalKilosDistribuidos);
        metricaProduccion.put("stockEnAlmacen", Math.max(0, totalKilosProcesados - totalKilosDistribuidos));
        metricaProduccion.put("distribucionDestinos", distribucionPorDestino);
        dashboard.put("produccion", metricaProduccion);

        
        List<ControlCalidad> controles = calidadRepository.findAll(PageRequest.of(0, 50)).getContent();
        long totalControles = controles.size();
        long controlesAprobados = controles.stream().filter(c -> "APROBADO".equalsIgnoreCase(c.getEstadoHaccp())).count();
        long controlesObservados = controles.stream().filter(c -> "OBSERVADO".equalsIgnoreCase(c.getEstadoHaccp())).count();
        long controlesRechazados = controles.stream().filter(c -> "RECHAZADO".equalsIgnoreCase(c.getEstadoHaccp())).count();
        
        double tasaAprobacion = totalControles > 0 ? ((double) controlesAprobados / totalControles) * 100 : 0.0;
        double temperaturaPromedio = controles.stream().mapToDouble(ControlCalidad::getTemperatura).average().orElse(0.0);
        double phPromedio = controles.stream().mapToDouble(ControlCalidad::getPh).average().orElse(0.0);
        long alertasCriticas = calidadRepository.countAlertasCriticas();

        Map<String, Object> metricaCalidad = new LinkedHashMap<>();
        metricaCalidad.put("totalControles", totalControles);
        metricaCalidad.put("aprobados", controlesAprobados);
        metricaCalidad.put("observados", controlesObservados);
        metricaCalidad.put("rechazados", controlesRechazados);
        metricaCalidad.put("tasaAprobacion", tasaAprobacion);
        metricaCalidad.put("temperaturaPromedio", temperaturaPromedio);
        metricaCalidad.put("phPromedio", phPromedio);
        metricaCalidad.put("alertasCriticas", alertasCriticas);
        dashboard.put("calidad", metricaCalidad);

        
        Double totalCostos = costoRepository.sumarTotalGeneral();
        List<CostoOperacional> costosList = costoRepository.findAll();
        Map<String, Double> costosPorCategoria = costosList.stream()
                .collect(Collectors.groupingBy(
                        CostoOperacional::getCategoria,
                        Collectors.summingDouble(CostoOperacional::getMonto)
                ));

        Map<String, Object> metricaCostos = new LinkedHashMap<>();
        metricaCostos.put("totalGeneral", totalCostos);
        metricaCostos.put("porCategoria", costosPorCategoria);
        dashboard.put("costos", metricaCostos);

        
        List<LiquidacionPago> liquidaciones = liquidacionRepository.findAll();
        double liquidacionMontoTotal = liquidaciones.stream().mapToDouble(LiquidacionPago::getMontoTotal).sum();
        double liquidacionMontoAprobado = liquidaciones.stream().filter(LiquidacionPago::getAprobado).mapToDouble(LiquidacionPago::getMontoTotal).sum();
        double liquidacionMontoPendiente = liquidaciones.stream().filter(l -> !l.getAprobado()).mapToDouble(LiquidacionPago::getMontoTotal).sum();
        long liquidacionesPendientes = liquidacionRepository.countByAprobadoFalse();

        Map<String, Object> metricaLiquidaciones = new LinkedHashMap<>();
        metricaLiquidaciones.put("montoTotal", liquidacionMontoTotal);
        metricaLiquidaciones.put("montoAprobado", liquidacionMontoAprobado);
        metricaLiquidaciones.put("montoPendiente", liquidacionMontoPendiente);
        metricaLiquidaciones.put("cantidadPendientes", liquidacionesPendientes);
        dashboard.put("liquidaciones", metricaLiquidaciones);

        return dashboard;
    }

    public Map<String, Object> obtenerReporteFiltrado(String tipo, Integer anio, Integer mes, Integer trimestre) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        String rangoTexto = "Histórico Completo";

        if ("anio".equalsIgnoreCase(tipo)) {
            int selAnio = (anio != null) ? anio : LocalDate.now().getYear();
            startDate = LocalDate.of(selAnio, 1, 1);
            endDate = LocalDate.of(selAnio, 12, 31);
            rangoTexto = "Año " + selAnio;
        } else if ("mes".equalsIgnoreCase(tipo)) {
            int selAnio = (anio != null) ? anio : LocalDate.now().getYear();
            int selMes = (mes != null) ? mes : LocalDate.now().getMonthValue();
            startDate = LocalDate.of(selAnio, selMes, 1);
            endDate = startDate.plusMonths(1).minusDays(1);
            
            String nameMes = obtenerNombreMes(selMes);
            rangoTexto = nameMes + " " + selAnio;
        } else if ("trimestre".equalsIgnoreCase(tipo)) {
            int selAnio = (anio != null) ? anio : LocalDate.now().getYear();
            int selTrim = (trimestre != null) ? trimestre : ((LocalDate.now().getMonthValue() - 1) / 3 + 1);
            if (selTrim < 1 || selTrim > 4) selTrim = 1;
            
            int startMonth = (selTrim - 1) * 3 + 1;
            startDate = LocalDate.of(selAnio, startMonth, 1);
            endDate = startDate.plusMonths(3).minusDays(1);
            rangoTexto = selTrim + "º Trimestre " + selAnio;
        }

        final LocalDate start = startDate;
        final LocalDate end = endDate;

        List<LoteProduccion> lotes = loteRepository.findAll();
        if (start != null && end != null) {
            lotes = lotes.stream()
                    .filter(l -> l.getFechaRegistro() != null && !l.getFechaRegistro().isBefore(start) && !l.getFechaRegistro().isAfter(end))
                    .collect(Collectors.toList());
        }
        double totalKilosProcesados = lotes.stream().mapToDouble(LoteProduccion::getKilosIniciales).sum();

        List<InventarioDistribucion> distribuciones = distribucionRepository.findAll();
        if (start != null && end != null) {
            distribuciones = distribuciones.stream()
                    .filter(d -> d.getFechaRegistro() != null && !d.getFechaRegistro().isBefore(start) && !d.getFechaRegistro().isAfter(end))
                    .collect(Collectors.toList());
        }
        double totalKilosDistribuidos = distribuciones.stream().mapToDouble(InventarioDistribucion::getKilosTotales).sum();

        Map<String, Double> distribucionPorDestino = distribuciones.stream()
                .collect(Collectors.groupingBy(
                        InventarioDistribucion::getDestino,
                        Collectors.summingDouble(InventarioDistribucion::getKilosTotales)
                ));

        Map<String, Object> metricaProduccion = new LinkedHashMap<>();
        metricaProduccion.put("kilosProcesados", totalKilosProcesados);
        metricaProduccion.put("kilosDistribuidos", totalKilosDistribuidos);
        metricaProduccion.put("stockEnAlmacen", Math.max(0, totalKilosProcesados - totalKilosDistribuidos));
        metricaProduccion.put("distribucionDestinos", distribucionPorDestino);

        List<ControlCalidad> controles = calidadRepository.findAll();
        if (start != null && end != null) {
            controles = controles.stream()
                    .filter(c -> c.getFechaRegistro() != null && !c.getFechaRegistro().isBefore(start) && !c.getFechaRegistro().isAfter(end))
                    .collect(Collectors.toList());
        }
        long totalControles = controles.size();
        long controlesAprobados = controles.stream().filter(c -> "APROBADO".equalsIgnoreCase(c.getEstadoHaccp())).count();
        long controlesObservados = controles.stream().filter(c -> "OBSERVADO".equalsIgnoreCase(c.getEstadoHaccp())).count();
        long controlesRechazados = controles.stream().filter(c -> "RECHAZADO".equalsIgnoreCase(c.getEstadoHaccp())).count();
        
        double tasaAprobacion = totalControles > 0 ? ((double) controlesAprobados / totalControles) * 100 : 0.0;
        double temperaturaPromedio = controles.stream().mapToDouble(ControlCalidad::getTemperatura).average().orElse(0.0);
        double phPromedio = controles.stream().mapToDouble(ControlCalidad::getPh).average().orElse(0.0);
        long alertasCriticas = controles.stream()
                .filter(c -> c.getTemperatura() != null && c.getPh() != null && (c.getTemperatura() > 4.5 || c.getPh() < 6.0 || c.getPh() > 7.0))
                .count();

        Map<String, Object> metricaCalidad = new LinkedHashMap<>();
        metricaCalidad.put("totalControles", totalControles);
        metricaCalidad.put("aprobados", controlesAprobados);
        metricaCalidad.put("observados", controlesObservados);
        metricaCalidad.put("rechazados", controlesRechazados);
        metricaCalidad.put("tasaAprobacion", tasaAprobacion);
        metricaCalidad.put("temperaturaPromedio", temperaturaPromedio);
        metricaCalidad.put("phPromedio", phPromedio);
        metricaCalidad.put("alertasCriticas", alertasCriticas);

        List<CostoOperacional> costosList = costoRepository.findAll();
        if (start != null && end != null) {
            costosList = costosList.stream()
                    .filter(c -> c.getFechaCosto() != null && !c.getFechaCosto().isBefore(start) && !c.getFechaCosto().isAfter(end))
                    .collect(Collectors.toList());
        }
        double totalCostos = costosList.stream().mapToDouble(CostoOperacional::getMonto).sum();
        Map<String, Double> costosPorCategoria = costosList.stream()
                .collect(Collectors.groupingBy(
                        CostoOperacional::getCategoria,
                        Collectors.summingDouble(CostoOperacional::getMonto)
                ));

        Map<String, Object> metricaCostos = new LinkedHashMap<>();
        metricaCostos.put("totalGeneral", totalCostos);
        metricaCostos.put("porCategoria", costosPorCategoria);

        List<LiquidacionPago> liquidaciones = liquidacionRepository.findAll();
        if (start != null && end != null) {
            liquidaciones = liquidaciones.stream()
                    .filter(l -> l.getFechaProduccion() != null && !l.getFechaProduccion().isBefore(start) && !l.getFechaProduccion().isAfter(end))
                    .collect(Collectors.toList());
        }
        double liquidacionMontoTotal = liquidaciones.stream().mapToDouble(LiquidacionPago::getMontoTotal).sum();
        double liquidacionMontoAprobado = liquidaciones.stream().filter(LiquidacionPago::getAprobado).mapToDouble(LiquidacionPago::getMontoTotal).sum();
        double liquidacionMontoPendiente = liquidaciones.stream().filter(l -> !l.getAprobado()).mapToDouble(LiquidacionPago::getMontoTotal).sum();
        long liquidacionesPendientes = liquidaciones.stream().filter(l -> !l.getAprobado()).count();

        Map<String, Object> metricaLiquidaciones = new LinkedHashMap<>();
        metricaLiquidaciones.put("montoTotal", liquidacionMontoTotal);
        metricaLiquidaciones.put("montoAprobado", liquidacionMontoAprobado);
        metricaLiquidaciones.put("montoPendiente", liquidacionMontoPendiente);
        metricaLiquidaciones.put("cantidadPendientes", liquidacionesPendientes);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("tipo", tipo);
        report.put("anio", anio);
        report.put("mes", mes);
        report.put("trimestre", trimestre);
        report.put("rangoTexto", rangoTexto);
        report.put("produccion", metricaProduccion);
        report.put("calidad", metricaCalidad);
        report.put("costos", metricaCostos);
        report.put("liquidaciones", metricaLiquidaciones);

        return report;
    }

    private String obtenerNombreMes(int mes) {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        if (mes >= 1 && mes <= 12) {
            return meses[mes - 1];
        }
        return "";
    }
}
