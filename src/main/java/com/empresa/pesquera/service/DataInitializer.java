package com.empresa.pesquera.service;

import com.empresa.pesquera.model.Usuario;
import com.empresa.pesquera.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Esta clase se ejecuta automáticamente al iniciar la aplicación.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // SEGURIDAD: Verificamos si la tabla de usuarios está vacía.
        // Si lo está, creamos una cuenta de gerente por defecto para poder ingresar.
        if (usuarioRepository.count() == 0) {
            Usuario gerente = new Usuario();
            gerente.setUsername("gerente_general");
            // Ciframos la contraseña usando BCrypt antes de guardarla.
            gerente.setPassword(passwordEncoder.encode("pota2026"));
            gerente.setRol("GERENTE");

            usuarioRepository.save(gerente);
            System.out.println(">>> SEEDER: Usuario Gerente creado por defecto.");
        }
    }
}