package com.empresa.pesquera.service;

import com.empresa.pesquera.model.CalculoCarga;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalculoService {

    private static class EtapaProceso {
        private final String rol;
        private final double rendimiento;

        private EtapaProceso(String rol, double rendimiento) {
            this.rol = rol;
            this.rendimiento = rendimiento;
        }
    }

    public Map<String, Integer> calcularPersonalPulpo(CalculoCarga datos) {

        if (datos.getKilos() == null || datos.getTiempoObjetivo() == null) {
            return new LinkedHashMap<>();
        }
        double tiempoEfectivo = datos.getTiempoObjetivo() - 1;

        if (tiempoEfectivo <= 0)
            return new LinkedHashMap<>();

        List<EtapaProceso> etapas = new ArrayList<>();
        etapas.add(new EtapaProceso("Apoyos", 500.0));
        etapas.add(new EtapaProceso("Limpieza", 60.0));
        etapas.add(new EtapaProceso("Clasificado", 250.0));
        etapas.add(new EtapaProceso("Envasado", 150.0));

        Map<String, Integer> resultados = new LinkedHashMap<>();

        for (EtapaProceso etapa : etapas) {
            int personas = (int) Math.ceil(datos.getKilos() / (etapa.rendimiento * tiempoEfectivo));
            resultados.put(etapa.rol, personas);
        }

        return resultados;
    }
}