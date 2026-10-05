package com.example.contactos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ContactoForm {

    private Integer id;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres.")
    private String nombre;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Pattern(regexp = "^[0-9+()\\s.-]{6,15}$", message = "Introduce un teléfono válido (6 a 15 caracteres: números, +, espacios o guiones).")
    private String telefono;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El correo electrónico no es válido.")
    @Size(max = 255, message = "El correo no puede superar los 255 caracteres.")
    private String email;

    @NotNull(message = "Selecciona una provincia.")
    private Integer provinciaId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre == null ? null : nombre.trim();
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono == null ? null : telefono.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public Integer getProvinciaId() {
        return provinciaId;
    }

    public void setProvinciaId(Integer provinciaId) {
        this.provinciaId = provinciaId;
    }
}