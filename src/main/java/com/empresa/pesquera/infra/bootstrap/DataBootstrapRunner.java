package com.empresa.pesquera.infra.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataBootstrapRunner implements CommandLineRunner {

    private final DataInitializer dataInitializer;

    public DataBootstrapRunner(DataInitializer dataInitializer) {
        this.dataInitializer = dataInitializer;
    }

    @Override
    public void run(String... args) {
        dataInitializer.seedDevelopmentData();
    }
}