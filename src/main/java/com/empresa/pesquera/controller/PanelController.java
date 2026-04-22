package com.empresa.pesquera.controller;

import com.empresa.pesquera.model.CalculoCarga;
import com.empresa.pesquera.service.CalculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class PanelController {

    @Autowired
    private CalculoService calculoService;

    /**
     * Carga inicial del panel del gerente.
     */
    @GetMapping("/gerente")
    public String gerente(Model model) {
        model.addAttribute("calculo", new CalculoCarga());
        // Enviamos el nombre del módulo para que el navbar ilumine el botón activo.
        model.addAttribute("modulo", "carga");
        return "panel-gerente";
    }

    /**
     * Procesa los datos del formulario y realiza la validación contra personal disponible.
     */
    @PostMapping("/gerente/calcular")
    public String procesarCalculoGerente(CalculoCarga calculo, Model model) {
        // Llamada al motor de cálculo.
        Map<String, Integer> necesarios = calculoService.calcularPersonalPulpo(calculo);

        // MOCK DATA: Simulación temporal de la base de datos de empleados por rol.
        // Esto permite validar si hay suficiente gente antes de que el Módulo 2 sea real.
        Map<String, Integer> disponibles = new LinkedHashMap<>();
        disponibles.put("Apoyos", 10);
        disponibles.put("Limpieza", 50);
        disponibles.put("Clasificado", 15);
        disponibles.put("Envasado", 20);

        // Pasamos toda la información a la vista (Thymeleaf).
        model.addAttribute("necesarios", necesarios);
        model.addAttribute("disponibles", disponibles);
        model.addAttribute("calculo", calculo);
        model.addAttribute("modulo", "carga");

        return "panel-gerente";
    }

    /**
     * Ruta para el Módulo 2. Por ahora solo muestra una página de construcción.
     */
    @GetMapping("/gerente/asignacion")
    public String moduloAsignacion(Model model) {
        model.addAttribute("modulo", "asignacion");
        return "panel-asignacion";
    }
}