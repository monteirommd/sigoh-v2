package com.sigoh.api.bloco;

import com.sigoh.api.bloco.dto.BlocoRequestDTO;
import com.sigoh.api.common.exception.BusinessException;
import com.sigoh.api.common.exception.ResourceNotFoundException;
import com.sigoh.api.enfermaria.EnfermariaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BlocoService {

    private final BlocoRepository blocoRepository;
    private final EnfermariaRepository enfermariaRepository;

    public BlocoService(BlocoRepository blocoRepository, EnfermariaRepository enfermariaRepository) {
        this.blocoRepository = blocoRepository;
        this.enfermariaRepository = enfermariaRepository;
    }

    @Transactional(readOnly = true)
    public List<Bloco> listarAtivos() {
        return blocoRepository.findByAtivoTrueOrderByNome();
    }

    @Transactional(readOnly = true)
    public Bloco buscarPorId(Long id) {
        return blocoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bloco", id));
    }

    @Transactional
    public Bloco criar(BlocoRequestDTO request) {
        return blocoRepository.save(Bloco.builder().nome(request.nome()).build());
    }

    @Transactional
    public Bloco atualizar(Long id, BlocoRequestDTO request) {
        Bloco bloco = buscarPorId(id);
        bloco.setNome(request.nome());
        return blocoRepository.save(bloco);
    }

    /**
     * Regra de negocio: nao se apaga um bloco que ainda tem enfermarias -
     * isso deixaria leitos orfaos. O usuario precisa esvaziar antes.
     */
    @Transactional
    public void excluir(Long id) {
        Bloco bloco = buscarPorId(id);
        if (enfermariaRepository.existsByBlocoId(id)) {
            throw new BusinessException("Bloco possui enfermarias vinculadas e nao pode ser excluido");
        }
        blocoRepository.delete(bloco);
    }
}
