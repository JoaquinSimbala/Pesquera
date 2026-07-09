package com.empresa.pesquera.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "inventario_distribucion")
public class InventarioDistribucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String loteReferencia;

    @Column(nullable = false)
    private Double kilosTotales;

    @Column(nullable = false)
    private String destino;

    @Column(nullable = false)
    private LocalDate fechaRegistro;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}