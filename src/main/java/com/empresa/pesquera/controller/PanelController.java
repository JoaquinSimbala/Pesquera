package com.empresa.pesquera.controller;

import com.empresa.pesquera.model.CalculoCarga;
import com.empresa.pesquera.model.PlanLiquidacionForm;
import com.empresa.pesquera.repository.TrabajadorRepository;
import com.empresa.pesquera.service.AsignacionService;
import com.empresa.pesquera.service.CalculoService;
import com.empresa.pesquera.service.LiquidacionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/gerente")
public class PanelController {

    private final CalculoService calculoService;
    private final AsignacionService asignacionService;
    private final TrabajadorRepository trabajadorRepository;
    private final LiquidacionService liquidacionService;

    public PanelController(CalculoService calculoService,
                           AsignacionService asignacionService,
                           TrabajadorRepository trabajadorRepository,
                           LiquidacionService liquidacionService) {
        this.calculoService = calculoService;
        this.asignacionService = asignacionService;
        this.trabajadorRepository = trabajadorRepository;
        this.liquidacionService = liquidacionService;
    }

    @GetMapping
    public String gerente(Model model) {
        model.addAttribute("calculo", new CalculoCarga());
        model.addAttribute("modulo", "carga");
        return "panel-gerente";
    }

    @PostMapping("/calcular")
    public String procesarCalculoGerente(@Valid @ModelAttribute("calculo") CalculoCarga calculo,
                                         BindingResult bindingResult,
                                         Model model) {
        model.addAttribute("modulo", "carga");

        if (bindingResult.hasErrors()) {
            return "panel-gerente";
        }

        Map<String, Integer> necesarios = calculoService.calcularPersonalPulpo(calculo);
        Map<String, Integer> disponibles = new LinkedHashMap<>();
        disponibles.put("Apoyos", trabajadorRepository.findByRolOperativoAndDisponibleTrue("Apoyos").size());
        disponibles.put("Limpieza", trabajadorRepository.findByRolOperativoAndDisponibleTrue("Limpieza").size());
        disponibles.put("Clasificado", trabajadorRepository.findByRolOperativoAndDisponibleTrue("Clasificado").size());
        disponibles.put("Envasado", trabajadorRepository.findByRolOperativoAndDisponibleTrue("Envasado").size());

        model.addAttribute("necesarios", necesarios);
        model.addAttribute("disponibles", disponibles);
        model.addAttribute("calculo", calculo);
        model.addAttribute("modulo", "carga");

        return "panel-gerente";
    }

    @PostMapping("/asignacion/generar")
    public String generarAsignacion(@ModelAttribute("calculo") CalculoCarga calculo, Model model) {
        AsignacionService.AsignacionResultado resultado = asignacionService.sugerirAsignacionGlobal(calculo);

        model.addAttribute("resultado", resultado);
        model.addAttribute("calculo", calculo);
        model.addAttribute("modulo", "asignacion");
        return "panel-asignacion";
    }

    @PostMapping("/asignacion/liquidaciones")
    public String enviarAsignacionALiquidacion(@ModelAttribute("calculo") CalculoCarga calculo,
                                               RedirectAttributes redirectAttributes) {
        AsignacionService.AsignacionResultado resultado = asignacionService.sugerirAsignacionGlobal(calculo);
        PlanLiquidacionForm plan = liquidacionService.construirPlanDesdeAsignacion(resultado, calculo);
        redirectAttributes.addFlashAttribute("planForm", plan);
        redirectAttributes.addFlashAttribute("origenModulo2", true);
        return "redirect:/gerente/liquidaciones";
    }

    @GetMapping("/asignacion")
    public String moduloAsignacion(Model model) {
        model.addAttribute("modulo", "asignacion");
        return "panel-asignacion";
    }
}
