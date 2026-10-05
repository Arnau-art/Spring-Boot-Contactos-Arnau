package com.example.contactos.controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/")
    public String raiz() {
        return "redirect:/contactos";
    }

    @GetMapping("/login")
    public String login(Principal principal) {
        return principal != null ? "redirect:/contactos" : "login";
    }
}