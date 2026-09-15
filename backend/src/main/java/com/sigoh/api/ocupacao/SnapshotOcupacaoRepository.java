package com.sigoh.api.ocupacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SnapshotOcupacaoRepository extends JpaRepository<SnapshotOcupacao, Long> {
    boolean existsByDataRegistroAndBlocoId(LocalDate dataRegistro, Long blocoId);
    List<SnapshotOcupacao> findByBlocoIdAndDataRegistroBetweenOrderByDataRegistro(
            Long blocoId, LocalDate inicio, LocalDate fim);
}
