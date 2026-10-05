package com.example.contactos.services;

import com.example.contactos.dto.UsuarioForm;
import com.example.contactos.entity.User;
import com.example.contactos.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public boolean emailEnUso(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User crear(UsuarioForm form) {
        User usuario = new User();
        usuario.setName(form.getName());
        usuario.setEmail(form.getEmail());
        usuario.setRoles("[]");
        usuario.setPassword(passwordEncoder.encode(form.getPassword()));
        return userRepository.save(usuario);
    }
}