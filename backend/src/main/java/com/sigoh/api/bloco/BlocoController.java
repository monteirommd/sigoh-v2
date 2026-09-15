package com.sigoh.api.bloco;

import com.sigoh.api.bloco.dto.BlocoRequestDTO;
import com.sigoh.api.bloco.dto.BlocoResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blocos")
public class BlocoController {

    private final BlocoService blocoService;
    private final BlocoMapper blocoMapper;

    public BlocoController(BlocoService blocoService, BlocoMapper blocoMapper) {
        this.blocoService = blocoService;
        this.blocoMapper = blocoMapper;
    }

    @GetMapping
    public List<BlocoResponseDTO> listar() {
        return blocoService.listarAtivos().stream().map(blocoMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public BlocoResponseDTO buscar(@PathVariable Long id) {
        return blocoMapper.toResponse(blocoService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BlocoResponseDTO criar(@Valid @RequestBody BlocoRequestDTO request) {
        return blocoMapper.toResponse(blocoService.criar(request));
    }

    @PutMapping("/{id}")
    public BlocoResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody BlocoRequestDTO request) {
        return blocoMapper.toResponse(blocoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        blocoService.excluir(id);
    }
}
