package com.empresa.pesquera.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "configuracion_proceso", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"especie_id", "rol_id"})
})
public class ConfiguracionProceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "especie_id", nullable = false)
    private Especie especie;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private RolOperativo rol;

    @Column(nullable = false)
    private Double rendimientoBase;

    @Column(nullable = false)
    private Double tarifaPorKilo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Especie getEspecie() {
        return especie;
    }

    public void setEspecie(Especie especie) {
        this.especie = especie;
    }

    public RolOperativo getRol() {
        return rol;
    }

    public void setRol(RolOperativo rol) {
        this.rol = rol;
    }

    public Double getRendimientoBase() {
        return rendimientoBase;
    }

    public void setRendimientoBase(Double rendimientoBase) {
        this.rendimientoBase = rendimientoBase;
    }

    public Double getTarifaPorKilo() {
        return tarifaPorKilo;
    }

    public void setTarifaPorKilo(Double tarifaPorKilo) {
        this.tarifaPorKilo = tarifaPorKilo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
