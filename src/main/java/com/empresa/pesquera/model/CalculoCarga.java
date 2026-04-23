package com.empresa.pesquera.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class CalculoCarga {
    @NotNull(message = "Los kilos son obligatorios.")
    @DecimalMin(value = "1.0", message = "Los kilos deben ser mayores a 0.")
    @DecimalMax(value = "1000000.0", message = "Los kilos superan el limite permitido.")
    private Double kilos;

    @NotNull(message = "El tiempo objetivo es obligatorio.")
    @DecimalMin(value = "1.0", message = "El tiempo objetivo debe ser mayor o igual a 1 hora.")
    @DecimalMax(value = "24.0", message = "El tiempo objetivo no puede superar 24 horas.")
    private Double tiempoObjetivo;

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
