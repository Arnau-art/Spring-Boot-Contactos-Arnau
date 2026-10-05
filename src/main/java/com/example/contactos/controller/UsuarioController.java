package com.example.contactos.controller;

import com.example.contactos.dto.UsuarioForm;
import com.example.contactos.entity.User;
import com.example.contactos.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private static final String VISTA_FORMULARIO = "usuarios/formulario";

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuarioForm", new UsuarioForm());
        return VISTA_FORMULARIO;
    }

    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("usuarioForm") UsuarioForm form,
                        BindingResult result, RedirectAttributes redirect) {
        if (!result.hasFieldErrors("email") && usuarioService.emailEnUso(form.getEmail())) {
            result.rejectValue("email", "email.duplicado", "Ya existe un usuario con ese correo electrónico.");
        }
        if (!result.hasFieldErrors("password") && !result.hasFieldErrors("confirmarPassword")
                && !form.getPassword().equals(form.getConfirmarPassword())) {
            result.rejectValue("confirmarPassword", "password.noCoincide", "Las contraseñas no coinciden.");
        }
        if (result.hasErrors()) {
            return VISTA_FORMULARIO;
        }
        User creado = usuarioService.crear(form);
        redirect.addFlashAttribute("exito",
                "Usuario «" + creado.getName() + "» creado correctamente. Ya puede iniciar sesión.");
        return "redirect:/contactos";
    }
}