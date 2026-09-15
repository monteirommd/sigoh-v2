package com.sigoh.api.ocupacao;

import com.sigoh.api.bloco.Bloco;
import com.sigoh.api.bloco.BlocoRepository;
import com.sigoh.api.ocupacao.dto.OcupacaoBlocoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Rotina agendada que grava a ocupacao do dia.
 *
 * E idempotente: se rodar duas vezes no mesmo dia, o segundo disparo nao
 * duplica registros. Roda dentro da propria aplicacao (@Scheduled) - com
 * varias instancias em paralelo seria necessario um lock distribuido para
 * garantir execucao unica.
 */
@Component
public class SnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(SnapshotScheduler.class);

    private final BlocoRepository blocoRepository;
    private final SnapshotOcupacaoRepository snapshotRepository;
    private final OcupacaoService ocupacaoService;

    public SnapshotScheduler(
            BlocoRepository blocoRepository,
            SnapshotOcupacaoRepository snapshotRepository,
            OcupacaoService ocupacaoService
    ) {
        this.blocoRepository = blocoRepository;
        this.snapshotRepository = snapshotRepository;
        this.ocupacaoService = ocupacaoService;
    }

    @Scheduled(cron = "0 55 23 * * *", zone = "America/Sao_Paulo")
    @Transactional
    public void gerarSnapshotDiario() {
        LocalDate hoje = LocalDate.now();
        int gerados = 0;

        for (Bloco bloco : blocoRepository.findByAtivoTrueOrderByNome()) {
            if (snapshotRepository.existsByDataRegistroAndBlocoId(hoje, bloco.getId())) {
                continue;
            }

            OcupacaoBlocoDTO ocupacao = ocupacaoService.ocupacaoDoBloco(bloco.getId());
            snapshotRepository.save(SnapshotOcupacao.builder()
                    .dataRegistro(hoje)
                    .bloco(bloco)
                    .totalLeitos(ocupacao.totalLeitos())
                    .ocupados(ocupacao.ocupados())
                    .disponiveis(ocupacao.disponiveis())
                    .emLimpeza(ocupacao.emLimpeza())
                    .bloqueados(ocupacao.bloqueados())
                    .build());
            gerados++;
        }

        log.info("Snapshot diario de ocupacao gerado para {} bloco(s)", gerados);
    }
}
