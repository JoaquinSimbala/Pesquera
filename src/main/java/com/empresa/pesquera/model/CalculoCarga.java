package com.empresa.pesquera.model;

/**
 * Clase que representa los datos necesarios para el cálculo de personal.
 * Se simplificó para alinearse con la lógica de procesamiento de pulpo.
 */
public class CalculoCarga {
    // Kilos totales de materia prima recibida (Pulpo)
    private double kilos;

    // Tiempo total en el que se espera terminar el procesamiento (Horas)
    private double tiempoObjetivo;

    // Getters y Setters: Permiten que Spring Boot lea y escriba en estas variables
    public double getKilos() {
        return kilos;
    }

    public void setKilos(double kilos) {
        this.kilos = kilos;
    }

    public double getTiempoObjetivo() {
        return tiempoObjetivo;
    }

    public void setTiempoObjetivo(double tiempoObjetivo) {
        this.tiempoObjetivo = tiempoObjetivo;
    }
}