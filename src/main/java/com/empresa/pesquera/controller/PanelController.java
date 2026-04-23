package com.empresa.pesquera.controller;

import com.empresa.pesquera.model.CalculoCarga;
import com.empresa.pesquera.service.CalculoService;
import com.empresa.pesquera.service.AsignacionService;
import com.empresa.pesquera.repository.TrabajadorRepository;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/gerente")
public class PanelController {

    private final CalculoService calculoService;
    private final AsignacionService asignacionService;
    private final TrabajadorRepository trabajadorRepository;

    public PanelController(CalculoService calculoService, AsignacionService asignacionService, TrabajadorRepository trabajadorRepository) {
        this.calculoService = calculoService;
        this.asignacionService = asignacionService;
        this.trabajadorRepository = trabajadorRepository;
    }

    /**
     * Carga inicial del panel del gerente.
     */
    @GetMapping
    public String gerente(Model model) {
        model.addAttribute("calculo", new CalculoCarga());
        // Enviamos el nombre del módulo para que el navbar ilumine el botón activo.
        model.addAttribute("modulo", "carga");
        return "panel-gerente";
    }

    /**
     * Procesa los datos del formulario y realiza la validación contra personal disponible.
     */
    @PostMapping("/calcular")
    public String procesarCalculoGerente(@Valid @ModelAttribute("calculo") CalculoCarga calculo,
                                         BindingResult bindingResult,
                                         Model model) {
        model.addAttribute("modulo", "carga");

        if (bindingResult.hasErrors()) {
            return "panel-gerente";
        }

        // Llamada al motor de cálculo.
        Map<String, Integer> necesarios = calculoService.calcularPersonalPulpo(calculo);

        // Datos reales de la base de datos de empleados por rol.
        Map<String, Integer> disponibles = new LinkedHashMap<>();
        disponibles.put("Apoyos", trabajadorRepository.findByRolOperativoAndDisponibleTrue("Apoyos").size());
        disponibles.put("Limpieza", trabajadorRepository.findByRolOperativoAndDisponibleTrue("Limpieza").size());
        disponibles.put("Clasificado", trabajadorRepository.findByRolOperativoAndDisponibleTrue("Clasificado").size());
        disponibles.put("Envasado", trabajadorRepository.findByRolOperativoAndDisponibleTrue("Envasado").size());

        // Pasamos toda la información a la vista (Thymeleaf).
        model.addAttribute("necesarios", necesarios);
        model.addAttribute("disponibles", disponibles);
        model.addAttribute("calculo", calculo);
        model.addAttribute("modulo", "carga");

        return "panel-gerente";
    }

    /**
     * Ruta para el Módulo 2. Recibe los datos del cálculo y genera la asignación automática.
     */
    @PostMapping("/asignacion/generar")
    public String generarAsignacion(@ModelAttribute("calculo") CalculoCarga calculo, Model model) {
        // Llamamos al servicio de asignación para obtener al mejor personal disponible
        AsignacionService.AsignacionResultado resultado = asignacionService.sugerirAsignacionGlobal(calculo);

        model.addAttribute("resultado", resultado);
        model.addAttribute("calculo", calculo);
        model.addAttribute("modulo", "asignacion");
        return "panel-asignacion";
    }

    /**
     * Ruta GET normal en caso de que quieran acceder sin cálculo
     */
    @GetMapping("/asignacion")
    public String moduloAsignacion(Model model) {
        model.addAttribute("modulo", "asignacion");
        return "panel-asignacion";
    }
}