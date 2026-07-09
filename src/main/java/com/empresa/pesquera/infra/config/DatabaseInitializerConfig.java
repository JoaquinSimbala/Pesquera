package com.empresa.pesquera.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.jdbc.DataSourceBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Configuration
public class DatabaseInitializerConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializerConfig.class);

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        String dbName = "unknown";
        try {
            
            String cleanUrl = dbUrl.substring(dbUrl.indexOf("//") + 2);
            String hostPort = cleanUrl.substring(0, cleanUrl.indexOf("/"));
            dbName = cleanUrl.substring(cleanUrl.indexOf("/") + 1);
            if (dbName.contains("?")) {
                dbName = dbName.substring(0, dbName.indexOf("?"));
            }

            String adminUrl = "jdbc:postgresql://" + hostPort + "/postgres";
            
            try (Connection conn = DriverManager.getConnection(adminUrl, username, password);
                 Statement stmt = conn.createStatement()) {
                
                
                ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'");
                if (!rs.next()) {
                    stmt.executeUpdate("CREATE DATABASE \"" + dbName + "\"");
                    logger.info("Base de datos '{}' creada automáticamente con éxito.", dbName);
                } else {
                    logger.info("Base de datos '{}' ya existe.", dbName);
                }
            }
        } catch (Exception e) {
            logger.error("Error al intentar verificar/crear la base de datos '{}': {}", dbName, e.getMessage());
        }

        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(username)
                .password(password)
                .build();
    }
}
