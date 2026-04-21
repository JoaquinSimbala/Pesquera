package com.empresa.pesquera;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerarContraseñas {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "121212";
        String hash = encoder.encode(password);

        System.out.println(hash);
    }
}
