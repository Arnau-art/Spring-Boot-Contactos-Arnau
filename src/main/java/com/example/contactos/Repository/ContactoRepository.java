package com.example.contactos.Repository;

import com.example.contactos.Entity.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactoRepository extends JpaRepository<Contacto, Integer> {

    boolean existsByEmail(String email);
}