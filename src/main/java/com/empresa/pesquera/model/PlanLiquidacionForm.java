package com.empresa.pesquera.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

public class PlanLiquidacionForm {

    @Valid
    @NotEmpty(message = "No hay trabajadores asignados para liquidar.")
    private List<ItemLiquidacionForm> items = new ArrayList<>();

    public List<ItemLiquidacionForm> getItems() {
        return items;
    }

    public void setItems(List<ItemLiquidacionForm> items) {
        this.items = items;
    }
}
