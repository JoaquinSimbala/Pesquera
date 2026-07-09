package com.empresa.pesquera.infra.bootstrap;

import com.empresa.pesquera.domain.entity.*;
import com.empresa.pesquera.infra.persistence.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DataInitializer {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataInitializer.class);

    @Value("${app.default.gerente.username:gerente_general}")
    private String defaultGerenteUsername;

    @Value("${app.default.gerente.password:pota2026}")
    private String defaultGerentePassword;

    @Value("${app.default.supervisor.username:supervisor_planta}")
    private String defaultSupervisorUsername;

    @Value("${app.default.supervisor.password:planta2026}")
    private String defaultSupervisorPassword;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TrabajadorRepository trabajadorRepository;
    private final RendimientoDiarioRepository rendimientoRepository;
    private final ControlCalidadRepository controlCalidadRepository;
    private final CostoOperacionalRepository costoOperacionalRepository;
    private final InventarioDistribucionRepository inventarioDistribucionRepository;
    private final LoteProduccionRepository loteRepository;
    private final LiquidacionPagoRepository liquidacionPagoRepository;
    private final ConfiguracionProcesoRepository configuracionProcesoRepository;
    private final EspecieRepository especieRepository;
    private final RolOperativoRepository rolOperativoRepository;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           TrabajadorRepository trabajadorRepository,
                           RendimientoDiarioRepository rendimientoRepository,
                           ControlCalidadRepository controlCalidadRepository,
                           CostoOperacionalRepository costoOperacionalRepository,
                           InventarioDistribucionRepository inventarioDistribucionRepository,
                           LoteProduccionRepository loteRepository,
                           LiquidacionPagoRepository liquidacionPagoRepository,
                           ConfiguracionProcesoRepository configuracionProcesoRepository,
                           EspecieRepository especieRepository,
                           RolOperativoRepository rolOperativoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.trabajadorRepository = trabajadorRepository;
        this.rendimientoRepository = rendimientoRepository;
        this.controlCalidadRepository = controlCalidadRepository;
        this.costoOperacionalRepository = costoOperacionalRepository;
        this.inventarioDistribucionRepository = inventarioDistribucionRepository;
        this.loteRepository = loteRepository;
        this.liquidacionPagoRepository = liquidacionPagoRepository;
        this.configuracionProcesoRepository = configuracionProcesoRepository;
        this.especieRepository = especieRepository;
        this.rolOperativoRepository = rolOperativoRepository;
    }

    @Transactional
    public void seedDevelopmentData() {
        Usuario gerente = null;
        Usuario supervisor = null;

        if (usuarioRepository.count() == 0) {
            gerente = new Usuario();
            gerente.setUsername(defaultGerenteUsername);
            gerente.setPassword(passwordEncoder.encode(defaultGerentePassword));
            gerente.setRol("GERENTE");
            gerente = usuarioRepository.save(gerente);
            logger.info("Usuario Gerente creado por defecto.");

            supervisor = new Usuario();
            supervisor.setUsername(defaultSupervisorUsername);
            supervisor.setPassword(passwordEncoder.encode(defaultSupervisorPassword));
            supervisor.setRol("SUPERVISOR");
            supervisor = usuarioRepository.save(supervisor);
            logger.info("Usuario Supervisor creado por defecto.");
        } else {
            gerente = usuarioRepository.findByUsername(defaultGerenteUsername).orElse(null);
            supervisor = usuarioRepository.findByUsername(defaultSupervisorUsername).orElse(null);
        }

        
        Especie pulpo = especieRepository.findByNombre("Pulpo").orElseGet(() -> especieRepository.save(new Especie("Pulpo")));
        Especie atun = especieRepository.findByNombre("Atún").orElseGet(() -> especieRepository.save(new Especie("Atún")));
        Especie caballa = especieRepository.findByNombre("Caballa").orElseGet(() -> especieRepository.save(new Especie("Caballa")));

        RolOperativo apoyos = rolOperativoRepository.findByNombre("Apoyos").orElseGet(() -> rolOperativoRepository.save(new RolOperativo("Apoyos")));
        RolOperativo limpieza = rolOperativoRepository.findByNombre("Limpieza").orElseGet(() -> rolOperativoRepository.save(new RolOperativo("Limpieza")));
        RolOperativo clasificado = rolOperativoRepository.findByNombre("Clasificado").orElseGet(() -> rolOperativoRepository.save(new RolOperativo("Clasificado")));
        RolOperativo envasado = rolOperativoRepository.findByNombre("Envasado").orElseGet(() -> rolOperativoRepository.save(new RolOperativo("Envasado")));

        
        seedConfiguraciones(gerente, pulpo, atun, caballa, apoyos, limpieza, clasificado, envasado);

        if (trabajadorRepository.count() < 100) {
            logger.info("Limpiando y creando trabajadores de prueba...");

            rendimientoRepository.deleteAll();
            trabajadorRepository.deleteAll();

            generarTrabajadores(apoyos, 12, 500.0, gerente);
            generarTrabajadores(limpieza, 50, 60.0, gerente);
            generarTrabajadores(clasificado, 30, 250.0, gerente);
            generarTrabajadores(envasado, 25, 150.0, gerente);

            logger.info("Trabajadores de prueba creados exitosamente.");
        }

        seedControlCalidad(supervisor);
        seedCostosOperacionales(gerente);
        seedInventarioDistribucion(supervisor);
        seedLiquidacionesPago(gerente);
    }

    private void seedConfiguraciones(Usuario admin, Especie pulpo, Especie atun, Especie caballa,
                                     RolOperativo apoyos, RolOperativo limpieza, RolOperativo clasificado, RolOperativo envasado) {
        if (configuracionProcesoRepository.count() > 0) {
            return;
        }

        
        crearConfiguracion(pulpo, apoyos, 500.0, 0.05, admin);
        crearConfiguracion(pulpo, limpieza, 60.0, 0.15, admin);
        crearConfiguracion(pulpo, clasificado, 250.0, 0.12, admin);
        crearConfiguracion(pulpo, envasado, 150.0, 0.10, admin);

        
        crearConfiguracion(atun, apoyos, 600.0, 0.07, admin);
        crearConfiguracion(atun, limpieza, 100.0, 0.18, admin);
        crearConfiguracion(atun, clasificado, 300.0, 0.15, admin);
        crearConfiguracion(atun, envasado, 200.0, 0.12, admin);

        
        crearConfiguracion(caballa, apoyos, 550.0, 0.06, admin);
        crearConfiguracion(caballa, limpieza, 80.0, 0.16, admin);
        crearConfiguracion(caballa, clasificado, 280.0, 0.14, admin);
        crearConfiguracion(caballa, envasado, 180.0, 0.11, admin);
    }

    private void crearConfiguracion(Especie especie, RolOperativo rol, double rendimiento, double tarifa, Usuario admin) {
        ConfiguracionProceso cfg = new ConfiguracionProceso();
        cfg.setEspecie(especie);
        cfg.setRol(rol);
        cfg.setRendimientoBase(rendimiento);
        cfg.setTarifaPorKilo(tarifa);
        cfg.setUsuario(admin);
        configuracionProcesoRepository.save(cfg);
    }

    private void generarTrabajadores(RolOperativo rol, int cantidad, double rendimientoBase, Usuario gerente) {
        for (int i = 1; i <= cantidad; i++) {
            Trabajador t = new Trabajador();
            t.setNombreCompleto(generarNombrePeruano());
            t.setDni(String.format("88%06d", (int) (Math.random() * 999999)));
            t.setRolOperativo(rol);
            t.setDisponible(true);
            t.setUsuario(gerente);

            
            
            double randVal = Math.random();
            double yld;
            if (randVal < 0.2) {
                yld = Math.round((rendimientoBase * 0.65) * 100.0) / 100.0; 
            } else if (randVal < 0.6) {
                yld = Math.round((rendimientoBase * 1.15) * 100.0) / 100.0; 
            } else {
                yld = Math.round((rendimientoBase * 1.66) * 100.0) / 100.0; 
            }

            t.setRendimientoPromedio(yld);
            trabajadorRepository.save(t);

            if (Math.random() > 0.2) {
                RendimientoDiario r = new RendimientoDiario();
                r.setTrabajador(t);
                r.setFecha(LocalDate.now().minusDays(1));
                r.setHorasTrabajadas(8.0);
                r.setKilosProcesados(Math.round(8.0 * yld * 100.0) / 100.0);
                rendimientoRepository.save(r);
            }
        }
    }

    private String generarNombrePeruano() {
        String[] nombres = { "Juan", "Luis", "Carlos", "José", "Pedro", "Jorge", "Manuel", "Víctor", "Miguel", "Francisco",
                "María", "Ana", "Rosa", "Carmen", "Juana", "Luz", "Elena", "Silvia", "Elizabeth", "Nancy" };
        String[] apellidos = { "Quispe", "Flores", "Sánchez", "García", "Rodríguez", "Rojas", "Huamán", "Mamani", "Vargas",
                "Castillo", "Chávez", "Gómez", "Díaz", "Ramos", "López", "Mendoza", "Ruiz", "Pinto", "Salazar", "Cárdenas" };
        return nombres[(int) (Math.random() * nombres.length)] + " " + apellidos[(int) (Math.random() * apellidos.length)]
                + " " + apellidos[(int) (Math.random() * apellidos.length)];
    }

    private void seedControlCalidad(Usuario supervisor) {
        if (controlCalidadRepository.count() >= 20) {
            return;
        }

        controlCalidadRepository.deleteAll();

        String[] estados = { "APROBADO", "APROBADO", "RECHAZADO" };

        for (int i = 1; i <= 20; i++) {
            ControlCalidad control = new ControlCalidad();
            control.setLoteReferencia(String.format("LOTE-%03d", i));
            control.setTemperatura(Math.round((-2.0 + (i * 0.35)) * 100.0) / 100.0);
            control.setPh(Math.round((5.5 + (i * 0.08)) * 100.0) / 100.0);
            control.setHigienePersonal(i % 5 != 0);
            control.setLimpiezaEquipos(i % 7 != 0);
            control.setEstadoHaccp(estados[i % estados.length]);
            control.setObservaciones("Inspección de calidad rutinaria para el lote " + control.getLoteReferencia());
            control.setFechaRegistro(LocalDate.now().minusDays(i));
            control.setUsuario(supervisor);
            controlCalidadRepository.save(control);
        }
    }

    private void seedCostosOperacionales(Usuario gerente) {
        String[] categoriasCosto = { "Logistica", "Suministros", "Mantenimiento", "Seguridad", "Servicios" };
        String[] conceptosCosto = { "Transporte de carga", "Cajas de empaque", "Repuestos de maquina",
                "Epps de personal", "Consumo de agua", "Consumo de energia", "Limpieza de planta" };

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
            costo.setUsuario(gerente);
            costoOperacionalRepository.save(costo);
        }
    }

    private void seedInventarioDistribucion(Usuario supervisor) {
        if (inventarioDistribucionRepository.count() >= 20) {
            return;
        }

        inventarioDistribucionRepository.deleteAll();
        loteRepository.deleteAll();

        String[] destinosInventario = { "Supermercados", "Mercado Mayorista", "Exportacion", "Mercado Local" };

        for (int i = 1; i <= 20; i++) {
            String loteCod = String.format("INV-%03d", i);

            
            LoteProduccion lote = new LoteProduccion();
            lote.setCodigoLote(loteCod);
            lote.setKilosIniciales(1000.0);
            lote.setFechaRegistro(LocalDate.now().minusDays(i + 5));
            loteRepository.save(lote);

            
            InventarioDistribucion inventario = new InventarioDistribucion();
            inventario.setLoteReferencia(loteCod);
            inventario.setKilosTotales(180.0 + (i * 22.5));
            inventario.setDestino(destinosInventario[i % destinosInventario.length]);
            inventario.setFechaRegistro(LocalDate.now().minusDays(i));
            inventario.setUsuario(supervisor);
            inventarioDistribucionRepository.save(inventario);
        }
    }

    private void seedLiquidacionesPago(Usuario gerente) {
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
            liquidacion.setMontoTotal(Math.round(montoTotal * 100.0) / 100.0);
            liquidacion.setFechaProduccion(LocalDate.now().minusDays(i));
            liquidacion.setAprobado(i % 3 != 0);
            liquidacion.setTipoProceso("PRODUCCION");
            liquidacion.setFechaRegistro(LocalDateTime.now().minusDays(i));
            liquidacion.setUsuario(gerente);
            liquidacionPagoRepository.save(liquidacion);
        }
    }
}