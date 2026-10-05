package com.example.contactos.controller;

import com.example.contactos.entity.User;
import com.example.contactos.repository.UserRepository;
import java.security.Principal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserRepository userRepository;

    public GlobalModelAttributes(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute("usuarioActual")
    public String usuarioActual(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByEmail(principal.getName())
                .map(User::getName)
                .orElse(principal.getName());
    }
}