package com.empresa.pesquera.web.mvc;

import com.empresa.pesquera.application.dto.form.RegistroCalidadForm;
import com.empresa.pesquera.application.service.CalidadService;
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
@RequestMapping("/supervisor/calidad")
public class CalidadController {

    private final CalidadService calidadService;

    public CalidadController(CalidadService calidadService) {
        this.calidadService = calidadService;
    }

    @GetMapping
    public String verPanel(Model model) {
        if (!model.containsAttribute("calidadForm")) {
            model.addAttribute("calidadForm", new RegistroCalidadForm());
        }
        cargarDatosVista(model);
        return "pages/panel-calidad";
    }

    @PostMapping("/registrar")
    public String registrarControl(@Valid @ModelAttribute("calidadForm") RegistroCalidadForm form,
                                    BindingResult result,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            cargarDatosVista(model);
            return "pages/panel-calidad";
        }

        calidadService.registrarControl(form);
        redirectAttributes.addFlashAttribute("ok", "Control de calidad registrado correctamente.");
        return "redirect:/supervisor/calidad";
    }

    private void cargarDatosVista(Model model) {
        model.addAttribute("historial", calidadService.listarHistorial());
        model.addAttribute("metricas", calidadService.obtenerResumenMetricas());
        model.addAttribute("modulo", "calidad");
    }
}
