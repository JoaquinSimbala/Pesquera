package com.empresa.pesquera.application.dto.form;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegistroCalidadForm {

    @NotBlank(message = "El código de lote es obligatorio.")
    private String loteReferencia;

    @NotNull(message = "La temperatura es obligatoria.")
    @DecimalMin(value = "-10.0", message = "La temperatura no puede ser menor a -10°C.")
    @DecimalMax(value = "30.0", message = "La temperatura no puede ser mayor a 30°C.")
    private Double temperatura;

    @NotNull(message = "El nivel de pH es obligatorio.")
    @DecimalMin(value = "0.0", message = "El pH mínimo es 0.0.")
    @DecimalMax(value = "14.0", message = "El pH máximo es 14.0.")
    private Double ph;

    @NotNull(message = "Debe indicar si el personal cumple con la higiene.")
    private Boolean higienePersonal;

    @NotNull(message = "Debe indicar si los equipos cumplen con la limpieza.")
    private Boolean limpiezaEquipos;

    @NotBlank(message = "Debe seleccionar un estado HACCP.")
    private String estadoHaccp;

    private String observaciones;

    public String getLoteReferencia() {
        return loteReferencia;
    }

    public void setLoteReferencia(String loteReferencia) {
        this.loteReferencia = loteReferencia;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Double getPh() {
        return ph;
    }

    public void setPh(Double ph) {
        this.ph = ph;
    }

    public Boolean getHigienePersonal() {
        return higienePersonal;
    }

    public void setHigienePersonal(Boolean higienePersonal) {
        this.higienePersonal = higienePersonal;
    }

    public Boolean getLimpiezaEquipos() {
        return limpiezaEquipos;
    }

    public void setLimpiezaEquipos(Boolean limpiezaEquipos) {
        this.limpiezaEquipos = limpiezaEquipos;
    }

    public String getEstadoHaccp() {
        return estadoHaccp;
    }

    public void setEstadoHaccp(String estadoHaccp) {
        this.estadoHaccp = estadoHaccp;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
