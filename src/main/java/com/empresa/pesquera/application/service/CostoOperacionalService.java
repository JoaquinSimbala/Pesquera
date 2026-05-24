package com.empresa.pesquera.application.service;

import com.empresa.pesquera.application.dto.form.RegistroCostoForm;
import com.empresa.pesquera.domain.entity.CostoOperacional;
import com.empresa.pesquera.infra.persistence.CostoOperacionalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CostoOperacionalService {

    private final CostoOperacionalRepository repository;

    public CostoOperacionalService(CostoOperacionalRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void registrarCosto(RegistroCostoForm form) {
        CostoOperacional costo = new CostoOperacional();
        costo.setCategoria(form.getCategoria());
        costo.setConcepto(form.getConcepto());
        costo.setMonto(redondear(form.getMonto()));
        costo.setFechaCosto(form.getFechaCosto());
        costo.setDescripcion(form.getDescripcion());
        costo.setFechaRegistro(LocalDateTime.now());
        repository.save(costo);
    }

    public List<CostoOperacional> listarCostos() {
        return repository.findAllByOrderByFechaCostoDescFechaRegistroDesc();
    }

    public ResumenCostos construirResumen() {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        Map<String, Double> porCategoria = new LinkedHashMap<>();

        for (Object[] fila : repository.sumarPorCategoriaDesdeFecha(inicioMes)) {
            porCategoria.put((String) fila[0], redondear((Double) fila[1]));
        }

        return new ResumenCostos(
                redondear(repository.sumarDesdeFecha(inicioMes)),
                redondear(repository.sumarTotalGeneral()),
                porCategoria);
    }

    public List<String> categorias() {
        return List.of("Insumos", "Servicios", "Mantenimiento", "Transporte", "Otros");
    }

    private double redondear(Double valor) {
        if (valor == null) {
            return 0.0;
        }
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static class ResumenCostos {
        private final double totalMes;
        private final double totalGeneral;
        private final Map<String, Double> porCategoria;

        public ResumenCostos(double totalMes, double totalGeneral, Map<String, Double> porCategoria) {
            this.totalMes = totalMes;
            this.totalGeneral = totalGeneral;
            this.porCategoria = porCategoria;
        }

        public double getTotalMes() {
            return totalMes;
        }

        public double getTotalGeneral() {
            return totalGeneral;
        }

        public Map<String, Double> getPorCategoria() {
            return porCategoria;
        }
    }
}
