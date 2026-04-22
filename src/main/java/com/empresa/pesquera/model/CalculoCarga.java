package com.empresa.pesquera.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Clase que representa los datos necesarios para el cálculo de personal.
 * Se simplificó para alinearse con la lógica de procesamiento de pulpo.
 */
public class CalculoCarga {
    // Kilos totales de materia prima recibida (Pulpo)
    @NotNull(message = "Los kilos son obligatorios.")
    @DecimalMin(value = "1.0", message = "Los kilos deben ser mayores a 0.")
    @DecimalMax(value = "1000000.0", message = "Los kilos superan el limite permitido.")
    private Double kilos;

    // Tiempo total en el que se espera terminar el procesamiento (Horas)
    @NotNull(message = "El tiempo objetivo es obligatorio.")
    @DecimalMin(value = "1.0", message = "El tiempo objetivo debe ser mayor o igual a 1 hora.")
    @DecimalMax(value = "24.0", message = "El tiempo objetivo no puede superar 24 horas.")
    private Double tiempoObjetivo;

    // Getters y Setters: Permiten que Spring Boot lea y escriba en estas variables
    public Double getKilos() {
        return kilos;
    }

    public void setKilos(Double kilos) {
        this.kilos = kilos;
    }

    public Double getTiempoObjetivo() {
        return tiempoObjetivo;
    }

    public void setTiempoObjetivo(Double tiempoObjetivo) {
        this.tiempoObjetivo = tiempoObjetivo;
    }
}