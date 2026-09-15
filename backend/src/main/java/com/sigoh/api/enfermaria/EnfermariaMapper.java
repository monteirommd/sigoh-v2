package com.sigoh.api.enfermaria;

import com.sigoh.api.enfermaria.dto.EnfermariaResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class EnfermariaMapper {
    public EnfermariaResponseDTO toResponse(Enfermaria enfermaria) {
        return new EnfermariaResponseDTO(
                enfermaria.getId(),
                enfermaria.getNome(),
                enfermaria.getBloco().getId(),
                enfermaria.getBloco().getNome(),
                enfermaria.isAtivo());
    }
}
