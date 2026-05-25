package com.empresa.pesquera.application.service;

import com.empresa.pesquera.application.dto.form.RegistroCalidadForm;
import com.empresa.pesquera.domain.entity.ControlCalidad;
import com.empresa.pesquera.infra.persistence.ControlCalidadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalidadService {

    private final ControlCalidadRepository repository;

    public CalidadService(ControlCalidadRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void registrarControl(RegistroCalidadForm form) {
        ControlCalidad cc = new ControlCalidad();
        cc.setLoteReferencia(form.getLoteReferencia());
        cc.setTemperatura(form.getTemperatura());
        cc.setPh(form.getPh());
        cc.setHigienePersonal(form.getHigienePersonal());
        cc.setLimpiezaEquipos(form.getLimpiezaEquipos());
        cc.setEstadoHaccp(form.getEstadoHaccp());
        cc.setObservaciones(form.getObservaciones());
        cc.setFechaRegistro(LocalDate.now());
        repository.save(cc);
    }

    public List<ControlCalidad> listarHistorial() {
        return repository.findAllByOrderByFechaRegistroDesc();
    }

    public Map<String, Object> obtenerResumenMetricas() {
        List<ControlCalidad> todos = repository.findAll();
        long total = todos.size();
        long aprobados = todos.stream().filter(c -> "APROBADO".equalsIgnoreCase(c.getEstadoHaccp())).count();
        long rechazados = todos.stream().filter(c -> "RECHAZADO".equalsIgnoreCase(c.getEstadoHaccp())).count();
        long observados = todos.stream().filter(c -> "CON OBSERVACIONES".equalsIgnoreCase(c.getEstadoHaccp())).count();

        long alertasCriticas = todos.stream()
                .filter(c -> c.getTemperatura() > 4.5 || c.getPh() < 6.0 || c.getPh() > 7.0)
                .count();

        double aprobadosPct = total > 0 ? ((double) aprobados / total) * 100.0 : 0.0;

        Map<String, Object> metricas = new LinkedHashMap<>();
        metricas.put("total", total);
        metricas.put("aprobados", aprobados);
        metricas.put("rechazados", rechazados);
        metricas.put("observados", observados);
        metricas.put("alertasCriticas", alertasCriticas);
        metricas.put("aprobadosPct", aprobadosPct);

        return metricas;
    }
}
