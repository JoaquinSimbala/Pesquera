package com.empresa.pesquera.application.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegistroInventarioForm {

    @NotBlank(message = "El código de lote es obligatorio.")
    private String loteReferencia;

    @NotNull(message = "Los kilos son obligatorios.")
    @DecimalMin(value = "0.1", message = "Debe registrar al menos 0.1 kg.")
    private Double kilosTotales;

    @NotBlank(message = "Debe seleccionar un destino.")
    private String destino;

    public String getLoteReferencia() {
        return loteReferencia;
    }

    public void setLoteReferencia(String loteReferencia) {
        this.loteReferencia = loteReferencia;
    }

    public Double getKilosTotales() {
        return kilosTotales;
    }

    public void setKilosTotales(Double kilosTotales) {
        this.kilosTotales = kilosTotales;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }
}
