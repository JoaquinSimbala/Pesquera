package com.empresa.pesquera.web.api;

import com.empresa.pesquera.application.dto.form.CalculoCarga;
import com.empresa.pesquera.application.service.CalculoService;
import com.empresa.pesquera.infra.persistence.TrabajadorRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gerente")
public class CargaApiController {

    private final CalculoService calculoService;
    private final TrabajadorRepository trabajadorRepository;

    public CargaApiController(CalculoService calculoService, TrabajadorRepository trabajadorRepository) {
        this.calculoService = calculoService;
        this.trabajadorRepository = trabajadorRepository;
    }

    @PostMapping("/calcular")
    public ResponseEntity<?> calcularCarga(@Valid @RequestBody CalculoCarga calculo) {
        try {
            Map<String, Integer> necesarios = calculoService.calcularPersonalPorEspecie(calculo);

            Map<String, Integer> disponibles = new LinkedHashMap<>();
            String[] roles = {"Apoyos", "Limpieza", "Clasificado", "Envasado"};
            for (String rol : roles) {
                disponibles.put(rol, trabajadorRepository.findByRolOperativoAndDisponibleTrue(rol).size());
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("necesarios", necesarios);
            response.put("disponibles", disponibles);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error interno al procesar el cálculo."));
        }
    }
}