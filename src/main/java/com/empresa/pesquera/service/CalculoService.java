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

    /**
     * Calcula la cantidad de personal intermitente necesario por rol.
     *
     * @param datos Objeto con los kilos y el tiempo objetivo.
     * @return Un mapa con el nombre del rol y la cantidad de personas calculada.
     */
    public Map<String, Integer> calcularPersonalPulpo(CalculoCarga datos) {

        if (datos.getKilos() == null || datos.getTiempoObjetivo() == null) {
            return new LinkedHashMap<>();
        }

        // REGLA DE NEGOCIO: Se resta 1 hora de improductividad promedio al tiempo objetivo.
        double tiempoEfectivo = datos.getTiempoObjetivo() - 1;

        // Validación de seguridad para evitar divisiones por cero o negativos.
        if (tiempoEfectivo <= 0) return new LinkedHashMap<>();

        // Catálogo de etapas: nos permite recorrer los procesos con una estructura uniforme.
        List<EtapaProceso> etapas = new ArrayList<>();
        etapas.add(new EtapaProceso("Apoyos", 500.0));
        etapas.add(new EtapaProceso("Limpieza", 60.0));
        etapas.add(new EtapaProceso("Clasificado", 250.0));
        etapas.add(new EtapaProceso("Envasado", 150.0));

        // Usamos LinkedHashMap para mantener el orden de los procesos al mostrar en la tabla.
        Map<String, Integer> resultados = new LinkedHashMap<>();

        // FÓRMULA: Personas = Kilos / (Rendimiento * TiempoEfectivo)
        // El for convierte la lista de etapas en resultados listos para la vista.
        for (EtapaProceso etapa : etapas) {
            int personas = (int) Math.ceil(datos.getKilos() / (etapa.rendimiento * tiempoEfectivo));
            resultados.put(etapa.rol, personas);
        }

        return resultados;
    }
}