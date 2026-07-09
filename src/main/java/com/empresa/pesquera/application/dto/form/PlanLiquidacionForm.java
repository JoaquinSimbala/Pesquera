package com.empresa.pesquera.application.dto.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

public class PlanLiquidacionForm {

    @jakarta.validation.constraints.NotBlank(message = "La especie es obligatoria.")
    private String especie;

    @Valid
    @NotEmpty(message = "No hay trabajadores asignados para liquidar.")
    private List<ItemLiquidacionForm> items = new ArrayList<>();

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public List<ItemLiquidacionForm> getItems() {
        return items;
    }

    public void setItems(List<ItemLiquidacionForm> items) {
        this.items = items;
    }
}
