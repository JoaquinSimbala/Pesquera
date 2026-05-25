package com.empresa.pesquera.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "control_calidad")
public class ControlCalidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String loteReferencia;

    @Column(nullable = false)
    private Double temperatura;

    @Column(nullable = false)
    private Double ph;

    @Column(nullable = false)
    private Boolean higienePersonal;

    @Column(nullable = false)
    private Boolean limpiezaEquipos;

    @Column(nullable = false)
    private String estadoHaccp;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false)
    private LocalDate fechaRegistro;

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

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
