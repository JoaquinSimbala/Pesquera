package com.empresa.pesquera.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PanelController {
    @GetMapping("/gerente")
    public String gerente() {
        return "panel-gerente";
    }

    @GetMapping("/supervisor")
    public String supervisor() {
        return "panel-supervisor";
    }
}
