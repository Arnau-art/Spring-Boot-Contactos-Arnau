package com.example.contactos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UsuarioForm {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres.")
    private String name;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El correo electrónico no es válido.")
    @Size(max = 180, message = "El correo no puede superar los 180 caracteres.")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.")
    private String password;

    @NotBlank(message = "Repite la contraseña.")
    private String confirmarPassword;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmarPassword() {
        return confirmarPassword;
    }

    public void setConfirmarPassword(String confirmarPassword) {
        this.confirmarPassword = confirmarPassword;
    }
}