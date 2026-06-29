package com.empresa.pesquera.web.api;

import com.empresa.pesquera.application.dto.AsignacionResponseDto;
import com.empresa.pesquera.application.dto.form.CalculoCarga;
import com.empresa.pesquera.application.service.AsignacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asignacion")
public class AsignacionApiController {

    private final AsignacionService asignacionService;

    public AsignacionApiController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @PostMapping("/generar")
    public ResponseEntity<AsignacionResponseDto> generarAsignacion(@Valid @RequestBody CalculoCarga calculo) {
        AsignacionService.AsignacionResultado resultado = asignacionService.sugerirAsignacionGlobal(calculo);

        Map<String, List<AsignacionResponseDto.TrabajadorAsignadoDto>> mappedAsignaciones = new LinkedHashMap<>();
        resultado.getAsignaciones().forEach((rol, lista) -> {
            List<AsignacionResponseDto.TrabajadorAsignadoDto> dtos = lista.stream()
                    .map(tr -> new AsignacionResponseDto.TrabajadorAsignadoDto(
                            tr.getTrabajador().getId(),
                            tr.getTrabajador().getNombreCompleto(),
                            tr.getTrabajador().getDni(),
                            tr.getRendimiento()
                    )).toList();
            mappedAsignaciones.put(rol, dtos);
        });

        AsignacionResponseDto dto = new AsignacionResponseDto(
                resultado.isDeficitPersonal(),
                resultado.getHorasRecomendadas(),
                mappedAsignaciones
        );

        return ResponseEntity.ok(dto);
    }
}
