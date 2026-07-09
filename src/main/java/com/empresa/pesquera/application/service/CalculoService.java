package com.empresa.pesquera.application.service;

import com.empresa.pesquera.application.dto.form.CalculoCarga;
import com.empresa.pesquera.domain.entity.ConfiguracionProceso;
import com.empresa.pesquera.infra.persistence.ConfiguracionProcesoRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CalculoService {

    private final ConfiguracionProcesoRepository configuracionProcesoRepository;

    public CalculoService(ConfiguracionProcesoRepository configuracionProcesoRepository) {
        this.configuracionProcesoRepository = configuracionProcesoRepository;
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

    public Map<String, Integer> calcularPersonalPulpo(CalculoCarga datos) {
        if (datos.getEspecie() == null) {
            datos.setEspecie("Pulpo");
        }
        return calcularPersonalPorEspecie(datos);
    }
}