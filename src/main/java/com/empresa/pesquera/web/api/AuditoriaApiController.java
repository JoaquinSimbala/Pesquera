package com.empresa.pesquera.web.api;

import com.empresa.pesquera.domain.entity.Auditoria;
import com.empresa.pesquera.domain.entity.Usuario;
import com.empresa.pesquera.infra.persistence.AuditoriaRepository;
import com.empresa.pesquera.infra.persistence.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/supervisor/auditorias")
public class AuditoriaApiController {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaApiController(AuditoriaRepository auditoriaRepository, UsuarioRepository usuarioRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/lista")
    public ResponseEntity<Map<String, Object>> obtenerAuditorias(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String rango,
            @RequestParam(required = false) String fechaEspecifica,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long filterUserId = (usuarioId != null && usuarioId > 0) ? usuarioId : null;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Auditoria> pageResult;

        if ("ultimo-mes".equalsIgnoreCase(rango)) {
            java.time.LocalDateTime fechaInicio = java.time.LocalDateTime.now().minusMonths(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            if (filterUserId != null) {
                pageResult = auditoriaRepository.findByUsuarioIdAndFechaGreaterThanEqualOrderByFechaDesc(filterUserId, fechaInicio, pageable);
            } else {
                pageResult = auditoriaRepository.findByFechaGreaterThanEqualOrderByFechaDesc(fechaInicio, pageable);
            }
        } else if ("especifica".equalsIgnoreCase(rango) && fechaEspecifica != null && !fechaEspecifica.trim().isEmpty()) {
            try {
                java.time.LocalDate localDate = java.time.LocalDate.parse(fechaEspecifica);
                java.time.LocalDateTime fechaInicio = localDate.atStartOfDay();
                java.time.LocalDateTime fechaFin = localDate.atTime(23, 59, 59, 999999999);
                if (filterUserId != null) {
                    pageResult = auditoriaRepository.findByUsuarioIdAndFechaBetweenOrderByFechaDesc(filterUserId, fechaInicio, fechaFin, pageable);
                } else {
                    pageResult = auditoriaRepository.findByFechaBetweenOrderByFechaDesc(fechaInicio, fechaFin, pageable);
                }
            } catch (Exception e) {
                if (filterUserId != null) {
                    pageResult = auditoriaRepository.findByUsuarioIdOrderByFechaDesc(filterUserId, pageable);
                } else {
                    pageResult = auditoriaRepository.findAllByOrderByFechaDesc(pageable);
                }
            }
        } else {
            
            if (filterUserId != null) {
                pageResult = auditoriaRepository.findByUsuarioIdOrderByFechaDesc(filterUserId, pageable);
            } else {
                pageResult = auditoriaRepository.findAllByOrderByFechaDesc(pageable);
            }
        }

        List<Map<String, Object>> content = pageResult.getContent().stream().map(a -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("accion", a.getAccion());
            item.put("detalle", a.getDetalle());
            item.put("fecha", a.getFecha() != null ? a.getFecha().toString() : null);

            Map<String, Object> userMap = new LinkedHashMap<>();
            userMap.put("id", a.getUsuario().getId());
            userMap.put("username", a.getUsuario().getUsername());
            userMap.put("rol", a.getUsuario().getRol());
            item.put("usuario", userMap);

            return item;
        }).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", content);
        response.put("totalPages", pageResult.getTotalPages());
        response.put("totalElements", pageResult.getTotalElements());
        response.put("currentPage", pageResult.getNumber());
        response.put("pageSize", pageResult.getSize());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDto>> obtenerUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<UsuarioDto> response = usuarios.stream()
                .map(u -> new UsuarioDto(u.getId(), u.getUsername(), u.getRol()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    public record UsuarioDto(Long id, String username, String rol) {}
}
