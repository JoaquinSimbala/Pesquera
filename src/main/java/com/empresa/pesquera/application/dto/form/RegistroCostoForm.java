package com.empresa.pesquera.application.dto.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class RegistroCostoForm {

    @NotBlank(message = "La categoria es obligatoria.")
    private String categoria;

    @NotBlank(message = "El concepto es obligatorio.")
    private String concepto;

    @NotNull(message = "El monto es obligatorio.")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0.")
    private Double monto;

    @NotNull(message = "La fecha del costo es obligatoria.")
    private LocalDate fechaCosto = LocalDate.now();

    private String descripcion;

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public LocalDate getFechaCosto() {
        return fechaCosto;
    }

    public void setFechaCosto(LocalDate fechaCosto) {
        this.fechaCosto = fechaCosto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
