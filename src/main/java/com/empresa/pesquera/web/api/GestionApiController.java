package com.empresa.pesquera.web.api;

import com.empresa.pesquera.application.service.GestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/supervisor/gestion")
public class GestionApiController {

    private final GestionService gestionService;

    public GestionApiController(GestionService gestionService) {
        this.gestionService = gestionService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> obtenerDashboard(
            @org.springframework.web.bind.annotation.RequestParam(value = "tipo", defaultValue = "historico") String tipo,
            @org.springframework.web.bind.annotation.RequestParam(value = "anio", required = false) Integer anio,
            @org.springframework.web.bind.annotation.RequestParam(value = "mes", required = false) Integer mes,
            @org.springframework.web.bind.annotation.RequestParam(value = "trimestre", required = false) Integer trimestre) {
        return ResponseEntity.ok(gestionService.obtenerReporteFiltrado(tipo, anio, mes, trimestre));
    }

    @GetMapping("/reporte")
    public ResponseEntity<?> obtenerReporte(
            @org.springframework.web.bind.annotation.RequestParam(value = "tipo", defaultValue = "historico") String tipo,
            @org.springframework.web.bind.annotation.RequestParam(value = "anio", required = false) Integer anio,
            @org.springframework.web.bind.annotation.RequestParam(value = "mes", required = false) Integer mes,
            @org.springframework.web.bind.annotation.RequestParam(value = "trimestre", required = false) Integer trimestre) {
        Map<String, Object> data = gestionService.obtenerReporteFiltrado(tipo, anio, mes, trimestre);
        return ResponseEntity.ok(data);
    }
}
