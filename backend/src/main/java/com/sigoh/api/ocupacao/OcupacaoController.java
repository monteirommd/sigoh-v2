package com.sigoh.api.ocupacao;

import com.sigoh.api.ocupacao.dto.OcupacaoBlocoDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ocupacao")
public class OcupacaoController {

    private final OcupacaoService ocupacaoService;

    public OcupacaoController(OcupacaoService ocupacaoService) {
        this.ocupacaoService = ocupacaoService;
    }

    @GetMapping("/blocos")
    public List<OcupacaoBlocoDTO> porBloco() {
        return ocupacaoService.ocupacaoPorBloco();
    }

    @GetMapping("/blocos/{blocoId}")
    public OcupacaoBlocoDTO doBloco(@PathVariable Long blocoId) {
        return ocupacaoService.ocupacaoDoBloco(blocoId);
    }
}
