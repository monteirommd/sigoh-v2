package com.sigoh.api.historico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoLeitoRepository extends JpaRepository<HistoricoLeito, Long> {
    List<HistoricoLeito> findByLeitoIdOrderByRegistradoEmDesc(Long leitoId);
}
