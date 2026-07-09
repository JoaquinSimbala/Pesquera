package com.empresa.pesquera;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.TimeZone;

@SpringBootApplication
public class PesqueraApplication {

	public static void main(String[] args) {
		cargarEnv();
		SpringApplication.run(PesqueraApplication.class, args);
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
		System.out.println("Zona horaria de la JVM establecida globalmente a America/Lima. Hora actual: " + java.time.LocalDateTime.now());
	}

	private static void cargarEnv() {
		try {
			File file = new File(".env");
			if (file.exists()) {
				List<String> lines = Files.readAllLines(Paths.get(".env"));
				for (String line : lines) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) {
						continue;
					}
					int eqIdx = line.indexOf('=');
					if (eqIdx > 0) {
						String key = line.substring(0, eqIdx).trim();
						String value = line.substring(eqIdx + 1).trim();
						
						if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
							value = value.substring(1, value.length() - 1);
						} else if (value.startsWith("'") && value.endsWith("'") && value.length() > 1) {
							value = value.substring(1, value.length() - 1);
						}
						System.setProperty(key, value);
					}
				}
				System.out.println("Archivo .env cargado exitosamente en las propiedades del sistema.");
			} else {
				System.out.println("No se encontró archivo .env. Se usarán las variables del sistema.");
			}
		} catch (IOException e) {
			System.err.println("Error al leer el archivo .env: " + e.getMessage());
		}
	}

}
