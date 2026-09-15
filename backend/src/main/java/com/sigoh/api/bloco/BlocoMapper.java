package com.sigoh.api.bloco;

import com.sigoh.api.bloco.dto.BlocoResponseDTO;
import org.springframework.stereotype.Component;

/**
 * Converte entidade -> DTO. Manter a entidade JPA fora da resposta HTTP
 * impede que mudancas internas do modelo quebrem o contrato da API.
 */
@Component
public class BlocoMapper {
    public BlocoResponseDTO toResponse(Bloco bloco) {
        return new BlocoResponseDTO(bloco.getId(), bloco.getNome(), bloco.isAtivo());
    }
}
