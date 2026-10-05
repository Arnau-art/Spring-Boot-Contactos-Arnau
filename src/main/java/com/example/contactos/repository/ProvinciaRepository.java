package com.example.contactos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.contactos.entity.Provincia;

public interface ProvinciaRepository extends JpaRepository<Provincia, Integer> {
}