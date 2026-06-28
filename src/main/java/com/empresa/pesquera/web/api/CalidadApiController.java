package com.empresa.pesquera.web.api;

import com.empresa.pesquera.application.dto.form.RegistroCalidadForm;
import com.empresa.pesquera.application.service.CalidadService;
import com.empresa.pesquera.domain.entity.ControlCalidad;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calidad")
public class CalidadApiController {

    private final CalidadService calidadService;

    public CalidadApiController(CalidadService calidadService) {
        this.calidadService = calidadService;
    }

    @GetMapping("/metricas")
    public ResponseEntity<Map<String, Object>> obtenerMetricas() {
        return ResponseEntity.ok(calidadService.obtenerResumenMetricas());
    }

    @GetMapping("/historial")
    public ResponseEntity<List<ControlCalidad>> obtenerHistorial() {
        return ResponseEntity.ok(calidadService.listarHistorial());
    }

    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrarControl(@Valid @RequestBody RegistroCalidadForm form) {
        calidadService.registrarControl(form);
        return ResponseEntity.ok(Map.of("message", "Control de calidad registrado correctamente."));
    }
}
