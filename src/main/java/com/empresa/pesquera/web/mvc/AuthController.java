package com.empresa.pesquera.web.mvc;

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
        return "pages/login";
    }

    @GetMapping("/redirigir")
    public String redirigir(Authentication auth) {
        if (auth != null) {
            for (GrantedAuthority rol : auth.getAuthorities()) {
                if (rol.getAuthority().equals("ROLE_GERENTE")) {
                    return "redirect:/gerente";
                } else if (rol.getAuthority().equals("ROLE_SUPERVISOR")) {
                    return "redirect:/supervisor";
                }
            }
        }
        return "redirect:/login";
    }
}