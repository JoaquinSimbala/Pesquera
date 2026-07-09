package com.empresa.pesquera.web.api;

import com.empresa.pesquera.application.dto.form.RegistroInventarioForm;
import com.empresa.pesquera.application.dto.form.RegistroLoteForm;
import com.empresa.pesquera.application.service.InventarioService;
import com.empresa.pesquera.domain.entity.InventarioDistribucion;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

import com.empresa.pesquera.infra.security.AuditoriaService;

@RestController
@RequestMapping("/api/inventario")
public class InventarioApiController {

    private final InventarioService inventarioService;
    private final AuditoriaService auditoriaService;

    public InventarioApiController(InventarioService inventarioService, AuditoriaService auditoriaService) {
        this.inventarioService = inventarioService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> obtenerDashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<InventarioDistribucion> pageResult = inventarioService.listarHistorial(pageable);
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("historial", pageResult.getContent());
        response.put("totalPages", pageResult.getTotalPages());
        response.put("totalElements", pageResult.getTotalElements());
        response.put("currentPage", pageResult.getNumber());
        
        response.put("metricas", inventarioService.obtenerResumenMetricas());
        response.put("destinos", inventarioService.obtenerDestinos());
        response.put("lotesDisponibles", inventarioService.obtenerLotesDisponibles());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ingreso-lote")
    public ResponseEntity<?> registrarIngreso(@Valid @RequestBody RegistroLoteForm form) {
        try {
            inventarioService.registrarIngresoLote(form);
            auditoriaService.registrar("Ingreso de Lote", 
                "Se ingresó el lote " + form.getCodigoLote() + " al almacén con " + form.getKilosIniciales() + " kg.");
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarSalida(@Valid @RequestBody RegistroInventarioForm form) {
        try {
            inventarioService.registrarDistribucion(form);
            auditoriaService.registrar("Distribución de Inventario", 
                "Se distribuyeron " + form.getKilosTotales() + " kg del lote " + form.getLoteReferencia() + 
                " hacia el destino: " + form.getDestino());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}