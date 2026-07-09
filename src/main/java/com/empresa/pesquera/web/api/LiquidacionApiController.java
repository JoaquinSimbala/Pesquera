package com.empresa.pesquera.web.api;

import com.empresa.pesquera.application.service.LiquidacionService;
import com.empresa.pesquera.domain.entity.LiquidacionPago;
import com.empresa.pesquera.infra.persistence.TrabajadorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.empresa.pesquera.infra.security.AuditoriaService;

@RestController
@RequestMapping("/api/liquidaciones")
public class LiquidacionApiController {

    private final LiquidacionService liquidacionService;
    private final TrabajadorRepository trabajadorRepository;
    private final AuditoriaService auditoriaService;

    public LiquidacionApiController(LiquidacionService liquidacionService, 
                                    TrabajadorRepository trabajadorRepository,
                                    AuditoriaService auditoriaService) {
        this.liquidacionService = liquidacionService;
        this.trabajadorRepository = trabajadorRepository;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerDatos(
            @RequestParam(value = "especie", required = false, defaultValue = "Pulpo") String especie,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<LiquidacionPago> pageResult = liquidacionService.listarLiquidaciones(pageable);
        LiquidacionService.ResumenLiquidacion resumen = liquidacionService.construirResumen();

        List<Map<String, Object>> liquidaciones = pageResult.getContent().stream().map(l -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", l.getId());
            item.put("trabajadorNombre", l.getTrabajador().getNombreCompleto());
            item.put("trabajadorRol", l.getTrabajador().getRolOperativo().getNombre());
            item.put("kilosProcesados", l.getKilosProcesados());
            item.put("tarifaPorKilo", l.getTarifaPorKilo());
            item.put("montoTotal", l.getMontoTotal());
            item.put("fechaProduccion", l.getFechaProduccion());
            item.put("aprobado", l.getAprobado());
            item.put("fechaRegistro", l.getFechaRegistro());
            return item;
        }).collect(Collectors.toList());

        String[] roles = { "Apoyos", "Limpieza", "Clasificado", "Envasado" };
        Map<String, List<Map<String, Object>>> trabajadoresPorRol = new LinkedHashMap<>();
        for (String rol : roles) {
            List<Map<String, Object>> trabajadoresRol = trabajadorRepository
                    .findByRolOperativoAndDisponibleTrue(rol)
                    .stream()
                    .map(t -> {
                        Map<String, Object> tw = new LinkedHashMap<>();
                        tw.put("id", t.getId());
                        tw.put("nombre", t.getNombreCompleto());
                        tw.put("dni", t.getDni());
                        return tw;
                    })
                    .collect(Collectors.toList());
            trabajadoresPorRol.put(rol, trabajadoresRol);
        }

        Map<String, Object> resumenMap = new LinkedHashMap<>();
        resumenMap.put("totalRegistros", resumen.getTotalRegistros());
        resumenMap.put("pendientesAprobacion", resumen.getPendientesAprobacion());
        resumenMap.put("montoTotal", resumen.getMontoTotal());
        resumenMap.put("montoAprobado", resumen.getMontoAprobado());
        resumenMap.put("montoPendiente", resumen.getMontoPendiente());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("liquidaciones", liquidaciones);
        response.put("resumen", resumenMap);
        response.put("trabajadoresPorRol", trabajadoresPorRol);
        response.put("especies", liquidacionService.listarEspecies());
        response.put("tarifas", liquidacionService.obtenerTarifasPorEspecie(especie));
        response.put("totalPages", pageResult.getTotalPages());
        response.put("totalElements", pageResult.getTotalElements());
        response.put("currentPage", pageResult.getNumber());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(@RequestBody NuevaLiquidacionRequest request) {
        try {
            if (request.especieId() != null) {
                liquidacionService.registrarUnaLiquidacionConEspecieId(request.trabajadorId(), request.kilosProcesados(), request.especieId());
            } else {
                String especie = request.especie() != null ? request.especie() : "Pulpo";
                liquidacionService.registrarUnaLiquidacion(request.trabajadorId(), request.kilosProcesados(), especie);
            }
            auditoriaService.registrar("Liquidación Manual", 
                "Se registró una liquidación manual para el operario con ID " + request.trabajadorId() + 
                " con " + request.kilosProcesados() + " kg.");
            return ResponseEntity.ok(Map.of("mensaje", "Liquidación registrada correctamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "No se pudo registrar la liquidación."));
        }
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<Map<String, String>> aprobar(@PathVariable Long id) {
        try {
            liquidacionService.aprobarLiquidacion(id);
            auditoriaService.registrar("Aprobación de Liquidación", 
                "Se aprobó el pago de liquidación con ID: " + id);
            return ResponseEntity.ok(Map.of("mensaje", "Liquidación aprobada correctamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "No se pudo aprobar la liquidación."));
        }
    }

    @PostMapping("/registrar-lote")
    public ResponseEntity<Map<String, String>> registrarLote(@RequestBody RegistroLoteRequest request) {
        try {
            String especie = request.especie() != null ? request.especie() : "Pulpo";
            List<Map<String, Object>> trabajadores = request.trabajadores().stream().map(t -> 
                Map.of("trabajadorId", (Object) t.trabajadorId(), "kilosProcesados", (Object) t.kilosProcesados())
            ).collect(Collectors.toList());

            liquidacionService.registrarLoteDeLiquidaciones(especie, trabajadores);
            auditoriaService.registrar("Liquidación de Lote", 
                "Se registraron las liquidaciones y liberaciones de personal del lote para la especie: " + especie);
            return ResponseEntity.ok(Map.of("mensaje", "Liquidaciones registradas y personal liberado correctamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "No se pudo registrar las liquidaciones."));
        }
    }

    public record NuevaLiquidacionRequest(Long trabajadorId, Double kilosProcesados, String especie, Long especieId) {
    }

    public record TrabajadorLoteItem(Long trabajadorId, Double kilosProcesados) {
    }

    public record RegistroLoteRequest(String especie, List<TrabajadorLoteItem> trabajadores) {
    }
}
