package com.sigoh.api.enfermaria;

import com.sigoh.api.bloco.BlocoService;
import com.sigoh.api.common.exception.BusinessException;
import com.sigoh.api.common.exception.ResourceNotFoundException;
import com.sigoh.api.enfermaria.dto.EnfermariaRequestDTO;
import com.sigoh.api.leito.LeitoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnfermariaService {

    private final EnfermariaRepository enfermariaRepository;
    private final LeitoRepository leitoRepository;
    private final BlocoService blocoService;

    public EnfermariaService(
            EnfermariaRepository enfermariaRepository,
            LeitoRepository leitoRepository,
            BlocoService blocoService
    ) {
        this.enfermariaRepository = enfermariaRepository;
        this.leitoRepository = leitoRepository;
        this.blocoService = blocoService;
    }

    @Transactional(readOnly = true)
    public List<Enfermaria> listar(Long blocoId) {
        return blocoId == null
                ? enfermariaRepository.findByAtivoTrueOrderByNome()
                : enfermariaRepository.findByBlocoIdAndAtivoTrueOrderByNome(blocoId);
    }

    @Transactional(readOnly = true)
    public Enfermaria buscarPorId(Long id) {
        return enfermariaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enfermaria", id));
    }

    @Transactional
    public Enfermaria criar(EnfermariaRequestDTO request) {
        return enfermariaRepository.save(Enfermaria.builder()
                .nome(request.nome())
                .bloco(blocoService.buscarPorId(request.blocoId()))
                .build());
    }

    @Transactional
    public Enfermaria atualizar(Long id, EnfermariaRequestDTO request) {
        Enfermaria enfermaria = buscarPorId(id);
        enfermaria.setNome(request.nome());
        enfermaria.setBloco(blocoService.buscarPorId(request.blocoId()));
        return enfermariaRepository.save(enfermaria);
    }

    @Transactional
    public void excluir(Long id) {
        Enfermaria enfermaria = buscarPorId(id);
        if (leitoRepository.existsByEnfermariaId(id)) {
            throw new BusinessException("Enfermaria possui leitos vinculados e nao pode ser excluida");
        }
        enfermariaRepository.delete(enfermaria);
    }
}
