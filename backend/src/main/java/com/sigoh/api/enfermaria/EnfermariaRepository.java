package com.sigoh.api.enfermaria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnfermariaRepository extends JpaRepository<Enfermaria, Long> {
    List<Enfermaria> findByAtivoTrueOrderByNome();
    List<Enfermaria> findByBlocoIdAndAtivoTrueOrderByNome(Long blocoId);
    boolean existsByBlocoId(Long blocoId);
}
