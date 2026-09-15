package com.sigoh.api.leito;

import com.sigoh.api.leito.dto.LeitoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class LeitoMapper {
    public LeitoResponseDTO toResponse(Leito leito) {
        return new LeitoResponseDTO(
                leito.getId(),
                leito.getCodigo(),
                leito.getEnfermaria().getId(),
                leito.getEnfermaria().getNome(),
                leito.getEnfermaria().getBloco().getId(),
                leito.getEnfermaria().getBloco().getNome(),
                leito.getStatus(),
                leito.isPossuiOxigenio(),
                leito.isIsolamento(),
                leito.getAtualizadoEm(),
                leito.getAtualizadoPor() != null ? leito.getAtualizadoPor().getNome() : null);
    }
}
