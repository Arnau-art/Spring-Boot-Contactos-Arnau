package com.example.contactos.Controller;

import com.example.contactos.Repository.ContactoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PruebaController {

    private final ContactoRepository contactoRepository;

    public PruebaController(ContactoRepository contactoRepository) {
        this.contactoRepository = contactoRepository;
    }

    @GetMapping("/prueba")
    public String prueba() {
        return "Contactos en la base de datos: " + contactoRepository.count();
    }
}