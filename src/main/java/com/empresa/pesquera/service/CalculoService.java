package com.empresa.pesquera.service;

import com.empresa.pesquera.model.CalculoCarga;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CalculoService {

    /**
     * Calcula la cantidad de personal intermitente necesario por rol.
     *
     * @param datos Objeto con los kilos y el tiempo objetivo.
     * @return Un mapa con el nombre del rol y la cantidad de personas calculada.
     */
    public Map<String, Integer> calcularPersonalPulpo(CalculoCarga datos) {

        // REGLA DE NEGOCIO: Se resta 1 hora de improductividad promedio al tiempo objetivo.
        double tiempoEfectivo = datos.getTiempoObjetivo() - 1;

        // Validación de seguridad para evitar divisiones por cero o negativos.
        if (tiempoEfectivo <= 0) return new LinkedHashMap<>();

        // RENDIMIENTOS ESTÁNDAR (Kg/persona/hora) definidos para la planta.
        double rendApoyo = 500.0;
        double rendLimpieza = 60.0;     // El cuello de botella del proceso.
        double rendClasificado = 250.0;
        double rendEnvasado = 150.0;

        // Usamos LinkedHashMap para mantener el orden de los procesos al mostrar en la tabla.
        Map<String, Integer> resultados = new LinkedHashMap<>();

        // FÓRMULA: Personas = Kilos / (Rendimiento * TiempoEfectivo)
        // Usamos Math.ceil() para redondear siempre al entero superior (no existen medias personas).
        resultados.put("Apoyos", (int) Math.ceil(datos.getKilos() / (rendApoyo * tiempoEfectivo)));
        resultados.put("Limpieza", (int) Math.ceil(datos.getKilos() / (rendLimpieza * tiempoEfectivo)));
        resultados.put("Clasificado", (int) Math.ceil(datos.getKilos() / (rendClasificado * tiempoEfectivo)));
        resultados.put("Envasado", (int) Math.ceil(datos.getKilos() / (rendEnvasado * tiempoEfectivo)));

        return resultados;
    }
}