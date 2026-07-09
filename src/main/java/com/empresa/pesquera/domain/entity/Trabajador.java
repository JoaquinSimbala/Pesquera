package com.empresa.pesquera.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "trabajadores")
public class Trabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_operativo_id", nullable = false)
    private RolOperativo rolOperativo;

    private Boolean disponible;

    private Double rendimientoPromedio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public RolOperativo getRolOperativo() {
        return rolOperativo;
    }

    public void setRolOperativo(RolOperativo rolOperativo) {
        this.rolOperativo = rolOperativo;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public Double getRendimientoPromedio() {
        return rendimientoPromedio;
    }

    public void setRendimientoPromedio(Double rendimientoPromedio) {
        this.rendimientoPromedio = rendimientoPromedio;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
