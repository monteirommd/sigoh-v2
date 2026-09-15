package com.sigoh.api.enfermaria;

import com.sigoh.api.enfermaria.dto.EnfermariaRequestDTO;
import com.sigoh.api.enfermaria.dto.EnfermariaResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enfermarias")
public class EnfermariaController {

    private final EnfermariaService enfermariaService;
    private final EnfermariaMapper enfermariaMapper;

    public EnfermariaController(EnfermariaService enfermariaService, EnfermariaMapper enfermariaMapper) {
        this.enfermariaService = enfermariaService;
        this.enfermariaMapper = enfermariaMapper;
    }

    @GetMapping
    public List<EnfermariaResponseDTO> listar(@RequestParam(required = false) Long blocoId) {
        return enfermariaService.listar(blocoId).stream().map(enfermariaMapper::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnfermariaResponseDTO criar(@Valid @RequestBody EnfermariaRequestDTO request) {
        return enfermariaMapper.toResponse(enfermariaService.criar(request));
    }

    @PutMapping("/{id}")
    public EnfermariaResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody EnfermariaRequestDTO request) {
        return enfermariaMapper.toResponse(enfermariaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        enfermariaService.excluir(id);
    }
}
