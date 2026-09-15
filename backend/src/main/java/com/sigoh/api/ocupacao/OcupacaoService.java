package com.sigoh.api.ocupacao;

import com.sigoh.api.bloco.Bloco;
import com.sigoh.api.bloco.BlocoService;
import com.sigoh.api.common.enums.StatusLeito;
import com.sigoh.api.leito.LeitoRepository;
import com.sigoh.api.ocupacao.dto.OcupacaoBlocoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Calculo de ocupacao.
 *
 * A taxa e derivada (nunca armazenada no leito), o que evita o risco classico
 * de um valor agregado ficar dessincronizado dos dados que o originaram.
 */
@Service
public class OcupacaoService {

    private final LeitoRepository leitoRepository;
    private final BlocoService blocoService;

    public OcupacaoService(LeitoRepository leitoRepository, BlocoService blocoService) {
        this.leitoRepository = leitoRepository;
        this.blocoService = blocoService;
    }

    @Transactional(readOnly = true)
    public List<OcupacaoBlocoDTO> ocupacaoPorBloco() {
        return blocoService.listarAtivos().stream().map(this::calcular).toList();
    }

    @Transactional(readOnly = true)
    public OcupacaoBlocoDTO ocupacaoDoBloco(Long blocoId) {
        return calcular(blocoService.buscarPorId(blocoId));
    }

    private OcupacaoBlocoDTO calcular(Bloco bloco) {
        Map<StatusLeito, Long> contagem = new EnumMap<>(StatusLeito.class);
        for (StatusLeito status : StatusLeito.values()) {
            contagem.put(status, 0L);
        }

        // A consulta devolve linhas [status, quantidade] agregadas pelo banco.
        for (Object[] linha : leitoRepository.contarPorStatusNoBloco(bloco.getId())) {
            contagem.put((StatusLeito) linha[0], (Long) linha[1]);
        }

        int ocupados = contagem.get(StatusLeito.OCUPADO).intValue();
        int disponiveis = contagem.get(StatusLeito.DISPONIVEL).intValue();
        int emLimpeza = contagem.get(StatusLeito.LIMPEZA).intValue();
        int bloqueados = contagem.get(StatusLeito.BLOQUEADO).intValue();
        int total = ocupados + disponiveis + emLimpeza + bloqueados;

        // Leitos bloqueados saem do denominador: nao estao disponiveis para
        // uso, entao conta-los distorceria a taxa para baixo.
        int base = total - bloqueados;
        double taxa = base == 0 ? 0.0 : (double) ocupados / base * 100.0;

        return new OcupacaoBlocoDTO(
                bloco.getId(), bloco.getNome(), total, ocupados, disponiveis, emLimpeza, bloqueados,
                Math.round(taxa * 10.0) / 10.0);
    }
}
