package com.empresa.pesquera.controller;

import com.empresa.pesquera.model.LiquidacionPago;
import com.empresa.pesquera.model.PlanLiquidacionForm;
import com.empresa.pesquera.service.LiquidacionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/gerente/liquidaciones")
public class LiquidacionController {

    private final LiquidacionService liquidacionService;

    public LiquidacionController(LiquidacionService liquidacionService) {
        this.liquidacionService = liquidacionService;
    }

    @GetMapping
    public String verModulo(Model model) {
        if (!model.containsAttribute("planForm")) {
            model.addAttribute("planForm", liquidacionService.crearFormularioVacio());
        }
        if (!model.containsAttribute("origenModulo2")) {
            model.addAttribute("origenModulo2", false);
        }
        cargarDatosVista(model);
        return "panel-liquidaciones";
    }

    @PostMapping("/registrar-lote")
    public String registrarLote(@Valid @ModelAttribute("planForm") PlanLiquidacionForm planForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("origenModulo2", true);
            cargarDatosVista(model);
            return "panel-liquidaciones";
        }

        liquidacionService.registrarLiquidaciones(planForm);
        redirectAttributes.addFlashAttribute("ok", "Liquidaciones registradas correctamente.");
        return "redirect:/gerente/liquidaciones";
    }

    @PostMapping("/{id}/aprobar")
    public String aprobar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        liquidacionService.aprobarLiquidacion(id);
        redirectAttributes.addFlashAttribute("ok", "Pago aprobado.");
        return "redirect:/gerente/liquidaciones";
    }

    private void cargarDatosVista(Model model) {
        List<LiquidacionPago> liquidaciones = liquidacionService.listarLiquidaciones();
        model.addAttribute("trabajadoresPorRol", liquidacionService.trabajadoresDisponiblesPorRol());
        model.addAttribute("liquidaciones", liquidaciones);
        model.addAttribute("resumen", liquidacionService.construirResumen(liquidaciones));
        model.addAttribute("tarifas", liquidacionService.tarifasBase());
        model.addAttribute("modulo", "liquidacion");
    }
}
