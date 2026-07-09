package com.empresa.pesquera.web.api;

import com.empresa.pesquera.application.dto.form.RegistroCalidadForm;
import com.empresa.pesquera.application.service.CalidadService;
import com.empresa.pesquera.domain.entity.ControlCalidad;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.empresa.pesquera.infra.security.AuditoriaService;

@RestController
@RequestMapping("/api/calidad")
public class CalidadApiController {

    private final CalidadService calidadService;
    private final AuditoriaService auditoriaService;

    public CalidadApiController(CalidadService calidadService, AuditoriaService auditoriaService) {
        this.calidadService = calidadService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/metricas")
    public ResponseEntity<Map<String, Object>> obtenerMetricas() {
        return ResponseEntity.ok(calidadService.obtenerResumenMetricas());
    }

    @GetMapping("/historial")
    public ResponseEntity<Map<String, Object>> obtenerHistorial(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<ControlCalidad> pageResult = calidadService.listarHistorial(pageable);
        
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("content", pageResult.getContent());
        response.put("totalPages", pageResult.getTotalPages());
        response.put("totalElements", pageResult.getTotalElements());
        response.put("currentPage", pageResult.getNumber());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrarControl(@Valid @RequestBody RegistroCalidadForm form) {
        calidadService.registrarControl(form);
        auditoriaService.registrar("Control de Calidad", 
            "Se registró un control de calidad para el lote " + form.getLoteReferencia() + 
            " (HACCP: " + form.getEstadoHaccp() + ")");
        return ResponseEntity.ok(Map.of("message", "Control de calidad registrado correctamente."));
    }
}
