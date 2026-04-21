package com.empresa.pesquera.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/")
    public String inicio() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/redirigir")
    public String redirigir(Authentication auth) {

        for (GrantedAuthority rol : auth.getAuthorities()) {

            if (rol.getAuthority().equals("ROLE_GERENTE")) {
                return "redirect:/gerente";
            }

            if (rol.getAuthority().equals("ROLE_SUPERVISOR")) {
                return "redirect:/supervisor";
            }
        }

        return "redirect:/login";
    }

}
