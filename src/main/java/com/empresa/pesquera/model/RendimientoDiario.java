package com.empresa.pesquera.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "rendimientos_diarios")
public class RendimientoDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajador_id")
    private Trabajador trabajador;

    private LocalDate fecha;
    private Double kilosProcesados;
    private Double horasTrabajadas;

    // Getters and Setters

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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Double getKilosProcesados() {
        return kilosProcesados;
    }

    public void setKilosProcesados(Double kilosProcesados) {
        this.kilosProcesados = kilosProcesados;
    }

    public Double getHorasTrabajadas() {
        return horasTrabajadas;
    }

    public void setHorasTrabajadas(Double horasTrabajadas) {
        this.horasTrabajadas = horasTrabajadas;
    }
}
