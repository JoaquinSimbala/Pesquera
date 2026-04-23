package com.empresa.pesquera.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ItemLiquidacionForm {

    @NotBlank(message = "Rol requerido.")
    private String rolOperativo;

    @NotNull(message = "Selecciona un trabajador.")
    private Long trabajadorId;

    @NotBlank(message = "Selecciona un tipo de proceso.")
    private String tipoProceso;

    @NotNull(message = "Los kilos son obligatorios.")
    @DecimalMin(value = "0.1", message = "Los kilos deben ser mayores a 0.")
    @DecimalMax(value = "1000000.0", message = "Los kilos superan el limite permitido.")
    private Double kilosProcesados;

    @NotNull(message = "La tarifa es obligatoria.")
    @DecimalMin(value = "0.01", message = "La tarifa debe ser mayor a 0.")
    @DecimalMax(value = "1000.0", message = "La tarifa supera el limite permitido.")
    private Double tarifaPorKilo;

    @NotNull(message = "La fecha de produccion es obligatoria.")
    private LocalDate fechaProduccion;

    public String getRolOperativo() {
        return rolOperativo;
    }

    public void setRolOperativo(String rolOperativo) {
        this.rolOperativo = rolOperativo;
    }

    public Long getTrabajadorId() {
        return trabajadorId;
    }

    public void setTrabajadorId(Long trabajadorId) {
        this.trabajadorId = trabajadorId;
    }

    public String getTipoProceso() {
        return tipoProceso;
    }

    public void setTipoProceso(String tipoProceso) {
        this.tipoProceso = tipoProceso;
    }

    public Double getKilosProcesados() {
        return kilosProcesados;
    }

    public void setKilosProcesados(Double kilosProcesados) {
        this.kilosProcesados = kilosProcesados;
    }

    public Double getTarifaPorKilo() {
        return tarifaPorKilo;
    }

    public void setTarifaPorKilo(Double tarifaPorKilo) {
        this.tarifaPorKilo = tarifaPorKilo;
    }

    public LocalDate getFechaProduccion() {
        return fechaProduccion;
    }

    public void setFechaProduccion(LocalDate fechaProduccion) {
        this.fechaProduccion = fechaProduccion;
    }
}
