package com.empresa.pesquera.web.api;

import com.empresa.pesquera.application.service.LiquidacionService;
import com.empresa.pesquera.domain.entity.LiquidacionPago;
import com.empresa.pesquera.domain.entity.Trabajador;
import com.empresa.pesquera.infra.persistence.TrabajadorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/liquidaciones")
public class LiquidacionApiController {

    private final LiquidacionService liquidacionService;
    private final TrabajadorRepository trabajadorRepository;

    public LiquidacionApiController(LiquidacionService liquidacionService,
            TrabajadorRepository trabajadorRepository) {
        this.liquidacionService = liquidacionService;
        this.trabajadorRepository = trabajadorRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerDatos() {
        List<LiquidacionPago> lista = liquidacionService.listarLiquidaciones();
        LiquidacionService.ResumenLiquidacion resumen = liquidacionService.construirResumen(lista);

        List<Map<String, Object>> liquidaciones = lista.stream().map(l -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", l.getId());
            item.put("trabajadorNombre", l.getTrabajador().getNombreCompleto());
            item.put("trabajadorRol", l.getTrabajador().getRolOperativo());
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
        response.put("tarifas", liquidacionService.tarifasOficiales());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(@RequestBody NuevaLiquidacionRequest request) {
        try {
            liquidacionService.registrarUnaLiquidacion(request.trabajadorId(), request.kilosProcesados());
            return ResponseEntity.ok(Map.of("mensaje", "Liquidación registrada correctamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "No se pudo registrar la liquidación."));
        }
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<Map<String, String>> aprobar(@PathVariable Long id) {
        try {
            liquidacionService.aprobarLiquidacion(id);
            return ResponseEntity.ok(Map.of("mensaje", "Liquidación aprobada correctamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "No se pudo aprobar la liquidación."));
        }
    }

    @PostMapping("/registrar-lote")
    public ResponseEntity<Map<String, String>> registrarLote(@RequestBody RegistroLoteRequest request) {
        try {
            for (TrabajadorLoteItem item : request.trabajadores()) {
                liquidacionService.registrarUnaLiquidacion(item.trabajadorId(), item.kilosProcesados());
            }
            return ResponseEntity.ok(Map.of("mensaje", "Liquidaciones registradas correctamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "No se pudo registrar las liquidaciones."));
        }
    }

    public record NuevaLiquidacionRequest(Long trabajadorId, Double kilosProcesados) {
    }

    public record TrabajadorLoteItem(Long trabajadorId, Double kilosProcesados) {
    }

    public record RegistroLoteRequest(List<TrabajadorLoteItem> trabajadores) {
    }
}
