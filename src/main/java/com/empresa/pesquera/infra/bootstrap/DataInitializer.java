package com.empresa.pesquera.infra.bootstrap;

import com.empresa.pesquera.domain.entity.RendimientoDiario;
import com.empresa.pesquera.domain.entity.Trabajador;
import com.empresa.pesquera.domain.entity.Usuario;
import com.empresa.pesquera.infra.persistence.RendimientoDiarioRepository;
import com.empresa.pesquera.infra.persistence.TrabajadorRepository;
import com.empresa.pesquera.infra.persistence.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TrabajadorRepository trabajadorRepository;
    private final RendimientoDiarioRepository rendimientoRepository;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           TrabajadorRepository trabajadorRepository,
                           RendimientoDiarioRepository rendimientoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.trabajadorRepository = trabajadorRepository;
        this.rendimientoRepository = rendimientoRepository;
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
    }

    private final String[] nombresMasc = {"Luis", "Carlos", "José", "Jorge", "Víctor", "Miguel", "Julio", "César", "Manuel", "Martín", "Roberto", "Fernando"};
    private final String[] nombresFem = {"María", "Rosa", "Ana", "Carmen", "Luz", "Silvia", "Elena", "Julia", "Mónica", "Milagros", "Roxana", "Patricia"};
    private final String[] apellidos = {"Quispe", "Flores", "Rodríguez", "Sánchez", "García", "Rojas", "Díaz", "Huamán", "Mamani", "Vargas", "Ramos", "Mendoza", "Condori", "Castro"};

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
}