package com.empresa.pesquera.infra.bootstrap;

import com.empresa.pesquera.domain.entity.ControlCalidad;
import com.empresa.pesquera.domain.entity.CostoOperacional;
import com.empresa.pesquera.domain.entity.InventarioDistribucion;
import com.empresa.pesquera.domain.entity.LiquidacionPago;
import com.empresa.pesquera.domain.entity.RendimientoDiario;
import com.empresa.pesquera.domain.entity.Trabajador;
import com.empresa.pesquera.domain.entity.Usuario;
import com.empresa.pesquera.infra.persistence.ControlCalidadRepository;
import com.empresa.pesquera.infra.persistence.CostoOperacionalRepository;
import com.empresa.pesquera.infra.persistence.InventarioDistribucionRepository;
import com.empresa.pesquera.infra.persistence.LiquidacionPagoRepository;
import com.empresa.pesquera.infra.persistence.RendimientoDiarioRepository;
import com.empresa.pesquera.infra.persistence.TrabajadorRepository;
import com.empresa.pesquera.infra.persistence.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TrabajadorRepository trabajadorRepository;
    private final RendimientoDiarioRepository rendimientoRepository;
    private final ControlCalidadRepository controlCalidadRepository;
    private final CostoOperacionalRepository costoOperacionalRepository;
    private final InventarioDistribucionRepository inventarioDistribucionRepository;
    private final LiquidacionPagoRepository liquidacionPagoRepository;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           TrabajadorRepository trabajadorRepository,
                           RendimientoDiarioRepository rendimientoRepository,
                           ControlCalidadRepository controlCalidadRepository,
                           CostoOperacionalRepository costoOperacionalRepository,
                           InventarioDistribucionRepository inventarioDistribucionRepository,
                           LiquidacionPagoRepository liquidacionPagoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.trabajadorRepository = trabajadorRepository;
        this.rendimientoRepository = rendimientoRepository;
        this.controlCalidadRepository = controlCalidadRepository;
        this.costoOperacionalRepository = costoOperacionalRepository;
        this.inventarioDistribucionRepository = inventarioDistribucionRepository;
        this.liquidacionPagoRepository = liquidacionPagoRepository;
    }

    @Transactional
    public void seedDevelopmentData() {
        if (usuarioRepository.count() == 0) {
            Usuario gerente = new Usuario();
            gerente.setUsername("gerente_general");
            gerente.setPassword(passwordEncoder.encode("pota2026"));
            gerente.setRol("GERENTE");
            usuarioRepository.save(gerente);
            System.out.println("Usuario Gerente creado por defecto.");

            Usuario supervisor = new Usuario();
            supervisor.setUsername("supervisor_planta");
            supervisor.setPassword(passwordEncoder.encode("planta2026"));
            supervisor.setRol("SUPERVISOR");
            usuarioRepository.save(supervisor);
            System.out.println("Usuario Supervisor creado por defecto.");
        }

        if (trabajadorRepository.count() < 100) {
            System.out.println("Limpiando y creando trabajadores de prueba...");

            rendimientoRepository.deleteAll();
            trabajadorRepository.deleteAll();

            generarTrabajadores("Apoyos", 12, 500.0);
            generarTrabajadores("Limpieza", 50, 60.0);
            generarTrabajadores("Clasificado", 30, 250.0);
            generarTrabajadores("Envasado", 25, 150.0);

            System.out.println("Trabajadores de prueba creados exitosamente.");
        }

        seedControlCalidad();
        seedCostosOperacionales();
        seedInventarioDistribucion();
        seedLiquidacionesPago();
    }

    private final String[] nombresMasc = {"Luis", "Carlos", "José", "Jorge", "Víctor", "Miguel", "Julio", "César", "Manuel", "Martín", "Roberto", "Fernando"};
    private final String[] nombresFem = {"María", "Rosa", "Ana", "Carmen", "Luz", "Silvia", "Elena", "Julia", "Mónica", "Milagros", "Roxana", "Patricia"};
    private final String[] apellidos = {"Quispe", "Flores", "Rodríguez", "Sánchez", "García", "Rojas", "Díaz", "Huamán", "Mamani", "Vargas", "Ramos", "Mendoza", "Condori", "Castro"};
    private final String[] lotesCalidad = {"LOTE-FRIO", "LOTE-ATUN", "LOTE-CONGELADO", "LOTE-ENLATADO", "LOTE-FILETE"};
    private final String[] estadosHaccp = {"APROBADO", "OBSERVADO", "RECHAZADO"};
    private final String[] categoriasCosto = {"Insumos", "Energía", "Mantenimiento", "Transporte", "Personal"};
    private final String[] conceptosCosto = {"Compra de hielo", "Consumo eléctrico", "Reparación de equipos", "Combustible", "Horas extra", "Material de limpieza"};
    private final String[] destinosInventario = {"Mercado Local", "Planta Congelado", "Exportación", "Mayorista Nacional", "Puerto de Despacho"};

    private String generarNombrePeruano() {
        String nombre = Math.random() > 0.5
                ? nombresMasc[(int) (Math.random() * nombresMasc.length)]
                : nombresFem[(int) (Math.random() * nombresFem.length)];
        String apellidoPaterno = apellidos[(int) (Math.random() * apellidos.length)];
        String apellidoMaterno = apellidos[(int) (Math.random() * apellidos.length)];
        return nombre + " " + apellidoPaterno + " " + apellidoMaterno;
    }

    private void generarTrabajadores(String rol, int cantidad, double rendimientoBase) {
        for (int i = 1; i <= cantidad; i++) {
            Trabajador t = new Trabajador();
            t.setNombreCompleto(generarNombrePeruano());
            t.setDni(String.format("88%06d", (int) (Math.random() * 999999)));
            t.setRolOperativo(rol);
            t.setDisponible(true);
            trabajadorRepository.save(t);

            if (Math.random() > 0.2) {
                RendimientoDiario r = new RendimientoDiario();
                r.setTrabajador(t);
                r.setFecha(LocalDate.now().minusDays(1));
                r.setHorasTrabajadas(8.0);

                double variacion = 0.8 + (Math.random() * 0.4);
                r.setKilosProcesados(8.0 * (rendimientoBase * variacion));
                rendimientoRepository.save(r);
            }
        }
    }

    private void seedControlCalidad() {
        if (controlCalidadRepository.count() >= 20) {
            return;
        }

        controlCalidadRepository.deleteAll();

        for (int i = 1; i <= 20; i++) {
            ControlCalidad controlCalidad = new ControlCalidad();
            controlCalidad.setLoteReferencia(String.format("%s-%03d", lotesCalidad[i % lotesCalidad.length], i));
            controlCalidad.setTemperatura(2.5 + (i % 6) * 0.6);
            controlCalidad.setPh(6.2 + (i % 5) * 0.12);
            controlCalidad.setHigienePersonal(i % 5 != 0);
            controlCalidad.setLimpiezaEquipos(i % 4 != 0);
            String estado = estadosHaccp[(i - 1) % estadosHaccp.length];
            if (i % 7 == 0) {
                estado = "RECHAZADO";
            }
            controlCalidad.setEstadoHaccp(estado);
            controlCalidad.setObservaciones(obtenerObservacionCalidad(estado));
            controlCalidad.setFechaRegistro(LocalDate.now().minusDays(i));
            controlCalidadRepository.save(controlCalidad);
        }
    }

    private void seedCostosOperacionales() {
        if (costoOperacionalRepository.count() >= 20) {
            return;
        }

        costoOperacionalRepository.deleteAll();

        for (int i = 1; i <= 20; i++) {
            CostoOperacional costo = new CostoOperacional();
            costo.setCategoria(categoriasCosto[i % categoriasCosto.length]);
            costo.setConcepto(conceptosCosto[i % conceptosCosto.length]);
            costo.setMonto(450.0 + (i * 135.0));
            costo.setFechaCosto(LocalDate.now().minusDays(i * 2L));
            costo.setDescripcion("Registro operativo generado para control de costos del proceso pesquero.");
            costo.setFechaRegistro(LocalDateTime.now().minusDays(i));
            costoOperacionalRepository.save(costo);
        }
    }

    private void seedInventarioDistribucion() {
        if (inventarioDistribucionRepository.count() >= 20) {
            return;
        }

        inventarioDistribucionRepository.deleteAll();

        for (int i = 1; i <= 20; i++) {
            InventarioDistribucion inventario = new InventarioDistribucion();
            inventario.setLoteReferencia(String.format("INV-%03d", i));
            inventario.setKilosTotales(180.0 + (i * 22.5));
            inventario.setDestino(destinosInventario[i % destinosInventario.length]);
            inventario.setFechaRegistro(LocalDate.now().minusDays(i));
            inventarioDistribucionRepository.save(inventario);
        }
    }

    private void seedLiquidacionesPago() {
        if (liquidacionPagoRepository.count() >= 20) {
            return;
        }

        liquidacionPagoRepository.deleteAll();

        List<Trabajador> trabajadores = trabajadorRepository.findAll();
        if (trabajadores.isEmpty()) {
            return;
        }

        for (int i = 0; i < 20; i++) {
            Trabajador trabajador = trabajadores.get(i % trabajadores.size());
            double kilosProcesados = 160.0 + (i * 14.5);
            double tarifaPorKilo = 1.65 + ((i % 5) * 0.1);
            double montoTotal = kilosProcesados * tarifaPorKilo;

            LiquidacionPago liquidacion = new LiquidacionPago();
            liquidacion.setTrabajador(trabajador);
            liquidacion.setKilosProcesados(kilosProcesados);
            liquidacion.setTarifaPorKilo(tarifaPorKilo);
            liquidacion.setMontoTotal(montoTotal);
            liquidacion.setFechaProduccion(LocalDate.now().minusDays(i + 1L));
            liquidacion.setAprobado(i % 4 != 0);
            liquidacion.setFechaAprobacion(liquidacion.getAprobado() ? LocalDateTime.now().minusDays(i) : null);
            liquidacion.setFechaRegistro(LocalDateTime.now().minusDays(i));
            liquidacionPagoRepository.save(liquidacion);
        }
    }

    private String obtenerObservacionCalidad(String estadoHaccp) {
        return switch (estadoHaccp) {
            case "RECHAZADO" -> "Se detectaron desviaciones críticas en higiene y control térmico.";
            case "OBSERVADO" -> "Requiere verificación adicional antes de liberar el lote.";
            default -> "Cumple con los parámetros operativos del control de calidad.";
        };
    }
}