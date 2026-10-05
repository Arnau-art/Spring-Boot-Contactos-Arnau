package com.example.contactos.repository;

import com.example.contactos.entity.Contacto;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactoRepository extends JpaRepository<Contacto, Integer> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Integer id);

    @EntityGraph(attributePaths = "provincia")
    List<Contacto> findAllByOrderByNombreAsc();
}