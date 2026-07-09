package com.empresa.pesquera.web.api;

import com.empresa.pesquera.application.dto.AsignacionResponseDto;
import com.empresa.pesquera.application.dto.form.CalculoCarga;
import com.empresa.pesquera.application.service.AsignacionService;
import com.empresa.pesquera.domain.entity.Asignacion;
import com.empresa.pesquera.domain.entity.ConfiguracionProceso;
import com.empresa.pesquera.infra.persistence.ConfiguracionProcesoRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.empresa.pesquera.infra.security.AuditoriaService;

@RestController
@RequestMapping("/api/asignacion")
public class AsignacionApiController {

    private final AsignacionService asignacionService;
    private final ConfiguracionProcesoRepository configuracionProcesoRepository;
    private final AuditoriaService auditoriaService;

    public AsignacionApiController(AsignacionService asignacionService,
                                   ConfiguracionProcesoRepository configuracionProcesoRepository,
                                   AuditoriaService auditoriaService) {
        this.asignacionService = asignacionService;
        this.configuracionProcesoRepository = configuracionProcesoRepository;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping("/generar")
    public ResponseEntity<AsignacionResponseDto> generarAsignacion(@Valid @RequestBody CalculoCarga calculo) {
        AsignacionService.AsignacionResultado resultado = asignacionService.sugerirAsignacionGlobal(calculo);

        Map<String, List<AsignacionResponseDto.TrabajadorAsignadoDto>> mappedAsignaciones = new LinkedHashMap<>();
        
        resultado.getAsignaciones().forEach((rol, lista) -> {
            double standard = configuracionProcesoRepository.findByEspecieAndRol(calculo.getEspecie(), rol)
                    .map(ConfiguracionProceso::getRendimientoBase)
                    .orElse(150.0);

            List<AsignacionResponseDto.TrabajadorAsignadoDto> dtos = lista.stream()
                    .map(tr -> {
                        boolean bajo = tr.getRendimiento() < standard;
                        return new AsignacionResponseDto.TrabajadorAsignadoDto(
                            tr.getTrabajador().getId(),
                            tr.getTrabajador().getNombreCompleto(),
                            tr.getTrabajador().getDni(),
                            tr.getRendimiento(),
                            bajo
                        );
                    }).toList();
            mappedAsignaciones.put(rol, dtos);
        });

        AsignacionResponseDto dto = new AsignacionResponseDto(
                resultado.isDeficitPersonal(),
                resultado.getHorasRecomendadas(),
                resultado.isAdvertenciaRendimiento(),
                mappedAsignaciones
        );

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/guardar")
    public ResponseEntity<?> guardarAsignacion(@Valid @RequestBody GuardarAsignacionRequest request) {
        try {
            Asignacion asignacion = asignacionService.guardarAsignacion(request.calculo(), request.trabajadorIds());
            auditoriaService.registrar("Registro de Asignación", 
                "Se registró una asignación de personal para procesar " + request.calculo().getKilos() + 
                " kg de " + request.calculo().getEspecie() + " (ID Asignación: " + asignacion.getId() + ")");
            return ResponseEntity.ok(Map.of("mensaje", "Asignación de personal guardada correctamente.", "id", asignacion.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/activas")
    public ResponseEntity<?> obtenerAsignacionesActivas() {
        List<Asignacion> activas = asignacionService.obtenerAsignacionesActivas();
        List<Map<String, Object>> response = activas.stream().map(a -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("especie", a.getEspecie());
            item.put("kilos", a.getKilos());
            item.put("tiempoObjetivo", a.getTiempoObjetivo());
            item.put("fechaRegistro", a.getFechaRegistro());
            item.put("usuarioCreador", a.getUsuario().getUsername());
            
            List<String> roleOrder = List.of("Apoyos", "Limpieza", "Clasificado", "Envasado");
            List<Map<String, Object>> trabajadores = a.getTrabajadores().stream()
                .sorted(java.util.Comparator.comparingInt(t -> roleOrder.indexOf(t.getRolOperativo().getNombre())))
                .map(t -> {
                    Map<String, Object> tw = new LinkedHashMap<>();
                    tw.put("id", t.getId());
                    tw.put("nombreCompleto", t.getNombreCompleto());
                    tw.put("dni", t.getDni());
                    tw.put("rolOperativo", t.getRolOperativo().getNombre());
                    return tw;
                }).collect(Collectors.toList());
            
            item.put("trabajadores", trabajadores);
            return item;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/liberar/{trabajadorId}")
    public ResponseEntity<?> liberarTrabajador(@PathVariable Long trabajadorId, @RequestBody Map<String, Double> payload) {
        try {
            Double kilos = payload.get("kilos");
            if (kilos == null || kilos <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Los kilos procesados son obligatorios."));
            }
            asignacionService.liberarTrabajadorConLiquidacion(trabajadorId, kilos);
            auditoriaService.registrar("Liberación de Operario", 
                "Se liberó al operario con ID " + trabajadorId + " registrando " + kilos + " kg procesados.");
            return ResponseEntity.ok(Map.of("mensaje", "Trabajador liberado y liquidación registrada exitosamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    public record GuardarAsignacionRequest(CalculoCarga calculo, List<Long> trabajadorIds) {
    }
}
