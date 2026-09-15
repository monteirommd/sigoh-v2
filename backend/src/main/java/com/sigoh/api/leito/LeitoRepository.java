package com.sigoh.api.leito;

import com.sigoh.api.common.enums.StatusLeito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeitoRepository extends JpaRepository<Leito, Long> {

    List<Leito> findByAtivoTrueOrderByCodigo();

    List<Leito> findByEnfermariaIdAndAtivoTrueOrderByCodigo(Long enfermariaId);

    boolean existsByEnfermariaId(Long enfermariaId);

    /**
     * Busca por bloco atravessando enfermaria. Em JPQL o relacionamento e
     * navegado por objeto (leito.enfermaria.bloco), nao por JOIN manual.
     */
    @Query("""
            SELECT l FROM Leito l
            WHERE l.enfermaria.bloco.id = :blocoId AND l.ativo = true
            ORDER BY l.codigo
            """)
    List<Leito> findByBlocoId(@Param("blocoId") Long blocoId);

    /**
     * Contagem agregada por status dentro de um bloco. Agregar no banco evita
     * trazer todos os leitos para a memoria so para conta-los.
     */
    @Query("""
            SELECT l.status, COUNT(l) FROM Leito l
            WHERE l.enfermaria.bloco.id = :blocoId AND l.ativo = true
            GROUP BY l.status
            """)
    List<Object[]> contarPorStatusNoBloco(@Param("blocoId") Long blocoId);

    long countByStatusAndAtivoTrue(StatusLeito status);

    long countByAtivoTrue();
}
