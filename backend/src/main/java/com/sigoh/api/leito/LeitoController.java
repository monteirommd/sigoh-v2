package com.sigoh.api.leito;

import com.sigoh.api.historico.HistoricoLeitoRepository;
import com.sigoh.api.historico.dto.HistoricoLeitoResponseDTO;
import com.sigoh.api.leito.dto.AtualizarStatusDTO;
import com.sigoh.api.leito.dto.LeitoRequestDTO;
import com.sigoh.api.leito.dto.LeitoResponseDTO;
import com.sigoh.api.usuario.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Leitura liberada a qualquer usuario autenticado; escrita restrita a ADMIN
 * pela regra central definida em SecurityConfig.
 */
@RestController
@RequestMapping("/api/leitos")
public class LeitoController {

    private final LeitoService leitoService;
    private final LeitoMapper leitoMapper;
    private final HistoricoLeitoRepository historicoRepository;

    public LeitoController(
            LeitoService leitoService,
            LeitoMapper leitoMapper,
            HistoricoLeitoRepository historicoRepository
    ) {
        this.leitoService = leitoService;
        this.leitoMapper = leitoMapper;
        this.historicoRepository = historicoRepository;
    }

    @GetMapping
    public List<LeitoResponseDTO> listar(
            @RequestParam(required = false) Long blocoId,
            @RequestParam(required = false) Long enfermariaId
    ) {
        return leitoService.listar(blocoId, enfermariaId).stream().map(leitoMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public LeitoResponseDTO buscar(@PathVariable Long id) {
        return leitoMapper.toResponse(leitoService.buscarPorId(id));
    }

    @GetMapping("/{id}/historico")
    public List<HistoricoLeitoResponseDTO> historico(@PathVariable Long id) {
        return historicoRepository.findByLeitoIdOrderByRegistradoEmDesc(id).stream()
                .map(h -> new HistoricoLeitoResponseDTO(
                        h.getId(),
                        h.getStatusAnterior(),
                        h.getStatusNovo(),
                        h.getUsuario().getNome(),
                        h.getRegistradoEm()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeitoResponseDTO criar(@Valid @RequestBody LeitoRequestDTO request) {
        return leitoMapper.toResponse(leitoService.criar(request));
    }

    @PutMapping("/{id}")
    public LeitoResponseDTO atualizar(
            @PathVariable Long id,
            @Valid @RequestBody LeitoRequestDTO request,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        return leitoMapper.toResponse(leitoService.atualizar(id, request, usuarioLogado));
    }

    /** Acao operacional de ronda: muda apenas o status. */
    @PatchMapping("/{id}/status")
    public LeitoResponseDTO atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarStatusDTO request,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        return leitoMapper.toResponse(leitoService.atualizarStatus(id, request.novoStatus(), usuarioLogado));
    }

    @PatchMapping("/{id}/avancar-status")
    public LeitoResponseDTO avancarStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        return leitoMapper.toResponse(leitoService.avancarStatus(id, usuarioLogado));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        leitoService.excluir(id);
    }
}
