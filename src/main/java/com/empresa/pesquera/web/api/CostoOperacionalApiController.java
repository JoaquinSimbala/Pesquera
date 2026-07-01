package com.empresa.pesquera.web.api;

import com.empresa.pesquera.application.dto.form.RegistroCostoForm;
import com.empresa.pesquera.application.service.CostoOperacionalService;
import com.empresa.pesquera.domain.entity.CostoOperacional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/costos")
public class CostoOperacionalApiController {

    private final CostoOperacionalService costoService;

    public CostoOperacionalApiController(CostoOperacionalService costoService) {
        this.costoService = costoService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerDatos() {
        List<CostoOperacional> lista = costoService.listarCostos();
        CostoOperacionalService.ResumenCostos resumen = costoService.construirResumen();

        List<Map<String, Object>> costos = lista.stream().map(c -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId());
            item.put("categoria", c.getCategoria());
            item.put("concepto", c.getConcepto());
            item.put("monto", c.getMonto());
            item.put("fechaCosto", c.getFechaCosto());
            item.put("descripcion", c.getDescripcion());
            item.put("fechaRegistro", c.getFechaRegistro());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> resumenMap = new LinkedHashMap<>();
        resumenMap.put("totalMes", resumen.getTotalMes());
        resumenMap.put("totalGeneral", resumen.getTotalGeneral());
        resumenMap.put("porCategoria", resumen.getPorCategoria());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("costos", costos);
        response.put("resumen", resumenMap);
        response.put("categorias", costoService.categorias());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(@RequestBody NuevoCostoRequest request) {
        try {
            RegistroCostoForm form = new RegistroCostoForm();
            form.setCategoria(request.categoria());
            form.setConcepto(request.concepto());
            form.setMonto(request.monto());
            form.setFechaCosto(request.fechaCosto() != null ? request.fechaCosto() : LocalDate.now());
            form.setDescripcion(request.descripcion());
            costoService.registrarCosto(form);
            return ResponseEntity.ok(Map.of("mensaje", "Costo registrado correctamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "No se pudo registrar el costo."));
        }
    }

    public record NuevoCostoRequest(
            String categoria,
            String concepto,
            Double monto,
            LocalDate fechaCosto,
            String descripcion
    ) {}
}
