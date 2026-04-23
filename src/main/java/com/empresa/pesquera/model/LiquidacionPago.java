package com.empresa.pesquera.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "liquidaciones_pago")
public class LiquidacionPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trabajador_id", nullable = false)
    private Trabajador trabajador;

    @Column(nullable = false, length = 20)
    private String tipoProceso;

    @Column(nullable = false)
    private Double kilosProcesados;

    @Column(nullable = false)
    private Double tarifaPorKilo;

    @Column(nullable = false)
    private Double montoTotal;

    @Column(nullable = false)
    private LocalDate fechaProduccion;

    @Column(nullable = false)
    private Boolean aprobado;

    private LocalDateTime fechaAprobacion;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Trabajador getTrabajador() {
        return trabajador;
    }

    public void setTrabajador(Trabajador trabajador) {
        this.trabajador = trabajador;
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

    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public LocalDate getFechaProduccion() {
        return fechaProduccion;
    }

    public void setFechaProduccion(LocalDate fechaProduccion) {
        this.fechaProduccion = fechaProduccion;
    }

    public Boolean getAprobado() {
        return aprobado;
    }

    public void setAprobado(Boolean aprobado) {
        this.aprobado = aprobado;
    }

    public LocalDateTime getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDateTime fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
