package com.sigoh.api.leito;

import com.sigoh.api.common.enums.StatusLeito;
import com.sigoh.api.common.exception.ResourceNotFoundException;
import com.sigoh.api.enfermaria.EnfermariaService;
import com.sigoh.api.historico.HistoricoLeito;
import com.sigoh.api.historico.HistoricoLeitoRepository;
import com.sigoh.api.leito.dto.LeitoRequestDTO;
import com.sigoh.api.realtime.OcupacaoBroadcaster;
import com.sigoh.api.realtime.dto.LeitoAtualizadoEvent;
import com.sigoh.api.usuario.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Regras de negocio do leito.
 *
 * O metodo atualizarStatus() e o unico caminho para mudar o status de um leito.
 * Concentrar isso num ponto so garante que TODA mudanca gere um registro de
 * historico e um evento de tempo real - se cada controller alterasse o status
 * por conta propria, cedo ou tarde alguma alteracao escaparia da auditoria.
 */
@Service
public class LeitoService {

    private final LeitoRepository leitoRepository;
    private final HistoricoLeitoRepository historicoRepository;
    private final EnfermariaService enfermariaService;
    private final OcupacaoBroadcaster broadcaster;

    public LeitoService(
            LeitoRepository leitoRepository,
            HistoricoLeitoRepository historicoRepository,
            EnfermariaService enfermariaService,
            OcupacaoBroadcaster broadcaster
    ) {
        this.leitoRepository = leitoRepository;
        this.historicoRepository = historicoRepository;
        this.enfermariaService = enfermariaService;
        this.broadcaster = broadcaster;
    }

    @Transactional(readOnly = true)
    public List<Leito> listar(Long blocoId, Long enfermariaId) {
        if (enfermariaId != null) {
            return leitoRepository.findByEnfermariaIdAndAtivoTrueOrderByCodigo(enfermariaId);
        }
        if (blocoId != null) {
            return leitoRepository.findByBlocoId(blocoId);
        }
        return leitoRepository.findByAtivoTrueOrderByCodigo();
    }

    @Transactional(readOnly = true)
    public Leito buscarPorId(Long id) {
        return leitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leito", id));
    }

    @Transactional
    public Leito criar(LeitoRequestDTO request) {
        Leito leito = Leito.builder()
                .codigo(request.codigo())
                .enfermaria(enfermariaService.buscarPorId(request.enfermariaId()))
                .status(request.status())
                .possuiOxigenio(request.possuiOxigenio())
                .isolamento(request.isolamento())
                .atualizadoEm(Instant.now())
                .build();
        return leitoRepository.save(leito);
    }

    @Transactional
    public Leito atualizar(Long id, LeitoRequestDTO request, Usuario usuarioLogado) {
        Leito leito = buscarPorId(id);
        StatusLeito statusAnterior = leito.getStatus();

        leito.setCodigo(request.codigo());
        leito.setEnfermaria(enfermariaService.buscarPorId(request.enfermariaId()));
        leito.setPossuiOxigenio(request.possuiOxigenio());
        leito.setIsolamento(request.isolamento());

        // Se o cadastro tambem mudou o status, passa pelo mesmo fluxo auditado.
        if (statusAnterior != request.status()) {
            return aplicarMudancaStatus(leito, request.status(), usuarioLogado);
        }

        leito.setAtualizadoEm(Instant.now());
        leito.setAtualizadoPor(usuarioLogado);
        return leitoRepository.save(leito);
    }

    @Transactional
    public void excluir(Long id) {
        // Exclusao logica: o leito sai das telas, mas o historico ja registrado
        // continua fazendo sentido (as linhas de auditoria seguem apontando
        // para um registro existente).
        Leito leito = buscarPorId(id);
        leito.setAtivo(false);
        leitoRepository.save(leito);
    }

    @Transactional
    public Leito atualizarStatus(Long id, StatusLeito novoStatus, Usuario usuarioLogado) {
        return aplicarMudancaStatus(buscarPorId(id), novoStatus, usuarioLogado);
    }

    /** Avanca o leito para o proximo status do ciclo operacional. */
    @Transactional
    public Leito avancarStatus(Long id, Usuario usuarioLogado) {
        Leito leito = buscarPorId(id);
        return aplicarMudancaStatus(leito, leito.getStatus().proximo(), usuarioLogado);
    }

    /**
     * Ponto unico de mudanca de status. Tudo abaixo roda na mesma transacao:
     * ou leito e historico sao gravados juntos, ou nada e gravado.
     */
    private Leito aplicarMudancaStatus(Leito leito, StatusLeito novoStatus, Usuario usuarioLogado) {
        StatusLeito statusAnterior = leito.getStatus();
        Instant agora = Instant.now();

        leito.setStatus(novoStatus);
        leito.setAtualizadoEm(agora);
        leito.setAtualizadoPor(usuarioLogado);
        Leito salvo = leitoRepository.save(leito);

        historicoRepository.save(HistoricoLeito.builder()
                .leito(salvo)
                .statusAnterior(statusAnterior)
                .statusNovo(novoStatus)
                .usuario(usuarioLogado)
                .registradoEm(agora)
                .build());

        broadcaster.leitoAtualizado(new LeitoAtualizadoEvent(
                salvo.getId(),
                salvo.getCodigo(),
                salvo.getEnfermaria().getId(),
                salvo.getEnfermaria().getBloco().getId(),
                statusAnterior,
                novoStatus,
                usuarioLogado.getNome(),
                agora));

        return salvo;
    }
}
