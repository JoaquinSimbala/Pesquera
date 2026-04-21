package com.empresa.pesquera.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

//Esta clase contiene configuración del sistema
@Configuration
public class SecurityConfig {

    //Este objeto lo va a gestionar Spring
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{
        //csrf protege los formularios de ataques, lo bloqueamos temporalmente
        httpSecurity.csrf(csrf -> csrf.disable());

        httpSecurity.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login","/css/**","/js/**").permitAll()
                .requestMatchers("/supervisor").hasRole("SUPERVISOR")
                .requestMatchers("/gerente").hasRole("GERENTE")
                .anyRequest().authenticated()

        );

        httpSecurity.formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/redirigir", true)
                .permitAll()
        );

        httpSecurity.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
        );

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
