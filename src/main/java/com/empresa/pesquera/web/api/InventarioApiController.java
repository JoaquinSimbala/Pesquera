package com.empresa.pesquera.web.api;

import com.empresa.pesquera.application.dto.form.RegistroInventarioForm;
import com.empresa.pesquera.application.dto.form.RegistroLoteForm;
import com.empresa.pesquera.application.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/supervisor/inventario")
public class InventarioApiController {

    private final InventarioService inventarioService;

    public InventarioApiController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> obtenerDashboard() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("historial", inventarioService.listarHistorial());
        response.put("metricas", inventarioService.obtenerResumenMetricas());
        response.put("destinos", inventarioService.obtenerDestinos());
        response.put("lotesDisponibles", inventarioService.obtenerLotesDisponibles());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ingreso-lote")
    public ResponseEntity<?> registrarIngreso(@Valid @RequestBody RegistroLoteForm form) {
        try {
            inventarioService.registrarIngresoLote(form);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarSalida(@Valid @RequestBody RegistroInventarioForm form) {
        try {
            inventarioService.registrarDistribucion(form);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}