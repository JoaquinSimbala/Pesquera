package com.empresa.pesquera.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "asignaciones")
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String especie;

    @Column(nullable = false)
    private Double kilos;

    @Column(nullable = false)
    private Double tiempoObjetivo;

    @Column(nullable = false)
    private LocalDate fechaRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "asignacion_trabajador",
        joinColumns = @JoinColumn(name = "asignacion_id"),
        inverseJoinColumns = @JoinColumn(name = "trabajador_id")
    )
    private Set<Trabajador> trabajadores = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

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

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Set<Trabajador> getTrabajadores() {
        return trabajadores;
    }

    public void setTrabajadores(Set<Trabajador> trabajadores) {
        this.trabajadores = trabajadores;
    }
}
