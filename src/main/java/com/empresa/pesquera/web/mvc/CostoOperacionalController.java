package com.empresa.pesquera.web.mvc;

import com.empresa.pesquera.application.dto.form.RegistroCostoForm;
import com.empresa.pesquera.application.service.CostoOperacionalService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/gerente/costos")
public class CostoOperacionalController {

    private final CostoOperacionalService costoService;

    public CostoOperacionalController(CostoOperacionalService costoService) {
        this.costoService = costoService;
    }

    @GetMapping
    public String verModulo(Model model) {
        if (!model.containsAttribute("costoForm")) {
            model.addAttribute("costoForm", new RegistroCostoForm());
        }
        cargarDatosVista(model);
        return "pages/panel-costos";
    }

    @PostMapping("/registrar")
    public String registrarCosto(@Valid @ModelAttribute("costoForm") RegistroCostoForm form,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            cargarDatosVista(model);
            return "pages/panel-costos";
        }

        costoService.registrarCosto(form);
        redirectAttributes.addFlashAttribute("ok", "Costo operacional registrado correctamente.");
        return "redirect:/gerente/costos";
    }

    private void cargarDatosVista(Model model) {
        model.addAttribute("costos", costoService.listarCostos());
        model.addAttribute("resumen", costoService.construirResumen());
        model.addAttribute("categorias", costoService.categorias());
        model.addAttribute("modulo", "costos");
    }
}
