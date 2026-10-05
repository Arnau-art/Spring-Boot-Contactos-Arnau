package com.example.contactos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.contactos.entity.Pais;
import java.util.List;

public interface PaisRepository extends JpaRepository<Pais, Integer> {
    List<Pais> findAllByOrderByNombreAsc();
}
