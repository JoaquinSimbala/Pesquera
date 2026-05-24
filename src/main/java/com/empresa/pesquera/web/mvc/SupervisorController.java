package com.empresa.pesquera.web.mvc;

import com.empresa.pesquera.application.dto.form.RegistroInventarioForm;
import com.empresa.pesquera.application.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/supervisor")
public class SupervisorController {

    private final InventarioService inventarioService;

    public SupervisorController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public String verPanel(Model model) {
        model.addAttribute("inventarioForm", new RegistroInventarioForm());
        cargarDatosVista(model);
        return "pages/panel-supervisor";
    }

    @PostMapping("/registrar")
    public String registrarEnvio(@Valid @ModelAttribute("inventarioForm") RegistroInventarioForm form,
                                 BindingResult result,
                                 Model model) {
        if (result.hasErrors()) {
            cargarDatosVista(model);
            return "pages/panel-supervisor";
        }

        inventarioService.registrarDistribucion(form);
        return "redirect:/supervisor";
    }

    private void cargarDatosVista(Model model) {
        model.addAttribute("historial", inventarioService.listarHistorial());
        model.addAttribute("metricas", inventarioService.obtenerResumenMetricas());
    }
}
