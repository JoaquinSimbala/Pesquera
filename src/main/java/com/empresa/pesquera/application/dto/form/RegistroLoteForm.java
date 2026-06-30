package com.empresa.pesquera.application.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegistroLoteForm {

    @NotBlank
    private String codigoLote;

    @NotNull
    @DecimalMin(value = "1.0")
    private Double kilosIniciales;

    public String getCodigoLote() { return codigoLote; }
    public void setCodigoLote(String codigoLote) { this.codigoLote = codigoLote; }
    public Double getKilosIniciales() { return kilosIniciales; }
    public void setKilosIniciales(Double kilosIniciales) { this.kilosIniciales = kilosIniciales; }
}