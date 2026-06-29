package com.empresa.pesquera.application.dto;

import java.util.List;
import java.util.Map;

public class AsignacionResponseDto {
    private boolean deficitPersonal;
    private int horasRecomendadas;
    private Map<String, List<TrabajadorAsignadoDto>> asignaciones;

    public AsignacionResponseDto() {}

    public AsignacionResponseDto(boolean deficitPersonal, int horasRecomendadas, Map<String, List<TrabajadorAsignadoDto>> asignaciones) {
        this.deficitPersonal = deficitPersonal;
        this.horasRecomendadas = horasRecomendadas;
        this.asignaciones = asignaciones;
    }

    public boolean isDeficitPersonal() {
        return deficitPersonal;
    }

    public void setDeficitPersonal(boolean deficitPersonal) {
        this.deficitPersonal = deficitPersonal;
    }

    public int getHorasRecomendadas() {
        return horasRecomendadas;
    }

    public void setHorasRecomendadas(int horasRecomendadas) {
        this.horasRecomendadas = horasRecomendadas;
    }

    public Map<String, List<TrabajadorAsignadoDto>> getAsignaciones() {
        return asignaciones;
    }

    public void setAsignaciones(Map<String, List<TrabajadorAsignadoDto>> asignaciones) {
        this.asignaciones = asignaciones;
    }

    public static class TrabajadorAsignadoDto {
        private Long id;
        private String nombreCompleto;
        private String dni;
        private double rendimiento;

        public TrabajadorAsignadoDto() {}

        public TrabajadorAsignadoDto(Long id, String nombreCompleto, String dni, double rendimiento) {
            this.id = id;
            this.nombreCompleto = nombreCompleto;
            this.dni = dni;
            this.rendimiento = rendimiento;
        }

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

        public double getRendimiento() {
            return rendimiento;
        }

        public void setRendimiento(double rendimiento) {
            this.rendimiento = rendimiento;
        }
    }
}
