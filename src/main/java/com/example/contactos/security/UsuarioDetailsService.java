package com.example.contactos.security;

import com.example.contactos.entity.User;
import com.example.contactos.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public UsuarioDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        List<GrantedAuthority> permisos = new ArrayList<>();
        permisos.add(new SimpleGrantedAuthority("ROLE_USER"));
        // En la BD los roles son un JSON tipo ["ROLE_ADMIN"]
        String roles = usuario.getRoles() == null ? "" : usuario.getRoles().replaceAll("[\\[\\]\"\\s]", "");
        for (String rol : roles.split(",")) {
            if (!rol.isEmpty() && !rol.equals("ROLE_USER")) {
                permisos.add(new SimpleGrantedAuthority(rol));
            }
        }

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(), usuario.getPassword(), permisos);
    }
}   