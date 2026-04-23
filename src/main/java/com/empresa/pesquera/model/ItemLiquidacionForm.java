package com.empresa.pesquera.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ItemLiquidacionForm {

    @NotBlank(message = "Rol requerido.")
    private String rolOperativo;

    @NotNull(message = "Selecciona un trabajador.")
    private Long trabajadorId;

    private String nombreTrabajador;

    @NotNull(message = "Los kilos son obligatorios.")
    @DecimalMin(value = "0.1", message = "Los kilos deben ser mayores a 0.")
    @DecimalMax(value = "1000000.0", message = "Los kilos superan el limite permitido.")
    private Double kilosProcesados;

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

    public String getNombreTrabajador() {
        return nombreTrabajador;
    }

    public void setNombreTrabajador(String nombreTrabajador) {
        this.nombreTrabajador = nombreTrabajador;
    }

    public Double getKilosProcesados() {
        return kilosProcesados;
    }

    public void setKilosProcesados(Double kilosProcesados) {
        this.kilosProcesados = kilosProcesados;
    }
}