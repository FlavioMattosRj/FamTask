package br.local.tarefasbenchmark.aplicacao;

import br.local.tarefasbenchmark.dominio.BloqueioTarefa;
import br.local.tarefasbenchmark.dominio.DependenciaExterna;
import br.local.tarefasbenchmark.dominio.DependenciaTarefa;
import br.local.tarefasbenchmark.dominio.EstadoTarefa;
import br.local.tarefasbenchmark.dominio.NotaTarefa;
import br.local.tarefasbenchmark.dominio.PrioridadeTarefa;
import br.local.tarefasbenchmark.dominio.Tarefa;
import br.local.tarefasbenchmark.dominio.TipoBloqueio;
import br.local.tarefasbenchmark.persistencia.RepositorioDependenciaExterna;
import br.local.tarefasbenchmark.persistencia.RepositorioDependenciaTarefa;
import br.local.tarefasbenchmark.persistencia.RepositorioNotaTarefa;
import br.local.tarefasbenchmark.persistencia.RepositorioTarefa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ServicoTarefa {

    private final RepositorioTarefa repositorioTarefa;
    private final RepositorioDependenciaTarefa repositorioDependencia;
    private final RepositorioDependenciaExterna repositorioDependenciaExterna;
    private final RepositorioNotaTarefa repositorioNota;

    public ServicoTarefa(RepositorioTarefa repositorioTarefa,
                         RepositorioDependenciaTarefa repositorioDependencia,
                         RepositorioDependenciaExterna repositorioDependenciaExterna,
                         RepositorioNotaTarefa repositorioNota) {
        this.repositorioTarefa = repositorioTarefa;
        this.repositorioDependencia = repositorioDependencia;
        this.repositorioDependenciaExterna = repositorioDependenciaExterna;
        this.repositorioNota = repositorioNota;
    }

    // ----- Tarefas -----

    public Tarefa criarTarefa(String titulo, String descricao,
                              PrioridadeTarefa prioridade, LocalDate previstoPara) {
        validarTitulo(titulo);
        Tarefa tarefa = new Tarefa(null, titulo.trim(), descricao, prioridade, previstoPara);
        return repositorioTarefa.save(tarefa);
    }

    public Tarefa criarSubtarefa(Long tarefaPaiId, String titulo, String descricao,
                                 PrioridadeTarefa prioridade, LocalDate previstoPara) {
        validarTitulo(titulo);
        Tarefa pai = buscarTarefaPorId(tarefaPaiId);
        Tarefa subtarefa = new Tarefa(pai.getId(), titulo.trim(), descricao, prioridade, previstoPara);
        return repositorioTarefa.save(subtarefa);
    }

    public Tarefa atualizarTarefa(Long id, String titulo, String descricao,
                                  PrioridadeTarefa prioridade, LocalDate previstoPara) {
        validarTitulo(titulo);
        Tarefa tarefa = buscarTarefaPorId(id);
        tarefa.setTitulo(titulo.trim());
        tarefa.setDescricao(descricao);
        if (prioridade != null) {
            tarefa.setPrioridade(prioridade);
        }
        tarefa.setPrevistoPara(previstoPara);
        tarefa.setAtualizadoEm(LocalDateTime.now());
        return repositorioTarefa.save(tarefa);
    }

    public void excluirTarefa(Long id) {
        Tarefa tarefa = buscarTarefaPorId(id);
        excluirRecursivamente(tarefa);
    }

    private void excluirRecursivamente(Tarefa tarefa) {
        for (Tarefa subtarefa : repositorioTarefa.findByTarefaPaiIdOrderByIdAsc(tarefa.getId())) {
            excluirRecursivamente(subtarefa);
        }
        repositorioDependencia.deleteByTarefaIdOrDependeDeTarefaId(tarefa.getId(), tarefa.getId());
        repositorioDependenciaExterna.deleteByTarefaId(tarefa.getId());
        repositorioNota.deleteByTarefaId(tarefa.getId());
        repositorioTarefa.delete(tarefa);
    }

    @Transactional(readOnly = true)
    public Tarefa buscarTarefaPorId(Long id) {
        return repositorioTarefa.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tarefa nao encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<Tarefa> listarTarefas() {
        return repositorioTarefa.findAll();
    }

    @Transactional(readOnly = true)
    public List<Tarefa> listarArvoreTarefas() {
        return repositorioTarefa.findByTarefaPaiIdIsNullOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public List<Tarefa> listarSubtarefas(Long tarefaId) {
        buscarTarefaPorId(tarefaId);
        return repositorioTarefa.findByTarefaPaiIdOrderByIdAsc(tarefaId);
    }

    @Transactional(readOnly = true)
    public List<Tarefa> pesquisarTarefas(String texto) {
        if (texto == null || texto.isBlank()) {
            return repositorioTarefa.findAll();
        }
        return repositorioTarefa.pesquisarPorTexto(texto.trim());
    }

    // ----- Estados -----

    public Tarefa iniciarTarefa(Long id) {
        Tarefa tarefa = buscarTarefaPorId(id);
        if (tarefa.getEstado() != EstadoTarefa.A_FAZER) {
            throw new RegraNegocioException(
                    "Somente tarefas no estado A_FAZER podem ser iniciadas. Estado atual: "
                            + tarefa.getEstado());
        }
        List<BloqueioTarefa> bloqueios = bloqueiosDependenciasInternas(tarefa);
        if (!bloqueios.isEmpty()) {
            throw new RegraNegocioException(
                    "A tarefa nao pode ser iniciada porque possui dependencias nao concluidas: "
                            + String.join("; ",
                            bloqueios.stream().map(BloqueioTarefa::mensagem).toList()));
        }
        LocalDateTime agora = LocalDateTime.now();
        tarefa.setEstado(EstadoTarefa.EM_EXECUCAO);
        tarefa.setIniciadoEm(agora);
        tarefa.setAtualizadoEm(agora);
        return repositorioTarefa.save(tarefa);
    }

    public Tarefa concluirTarefa(Long id) {
        Tarefa tarefa = buscarTarefaPorId(id);
        if (tarefa.getEstado() != EstadoTarefa.EM_EXECUCAO) {
            throw new RegraNegocioException(
                    "Somente tarefas no estado EM_EXECUCAO podem ser concluidas. Estado atual: "
                            + tarefa.getEstado());
        }
        LocalDateTime agora = LocalDateTime.now();
        tarefa.setEstado(EstadoTarefa.CONCLUIDA);
        tarefa.setConcluidoEm(agora);
        tarefa.setAtualizadoEm(agora);
        return repositorioTarefa.save(tarefa);
    }

    public Tarefa cancelarTarefa(Long id) {
        Tarefa tarefa = buscarTarefaPorId(id);
        if (tarefa.getEstado().finalizada()) {
            throw new RegraNegocioException(
                    "Tarefas finalizadas nao podem ser canceladas. Estado atual: "
                            + tarefa.getEstado());
        }
        LocalDateTime agora = LocalDateTime.now();
        tarefa.setEstado(EstadoTarefa.CANCELADA);
        tarefa.setCanceladoEm(agora);
        tarefa.setAtualizadoEm(agora);
        return repositorioTarefa.save(tarefa);
    }

    // ----- Dependencias internas -----

    @Transactional(readOnly = true)
    public List<DependenciaTarefa> listarDependencias(Long tarefaId) {
        buscarTarefaPorId(tarefaId);
        return repositorioDependencia.findByTarefaIdOrderByIdAsc(tarefaId);
    }

    public DependenciaTarefa adicionarDependenciaInterna(Long tarefaId, Long dependeDeTarefaId) {
        Tarefa tarefa = buscarTarefaPorId(tarefaId);
        if (dependeDeTarefaId == null) {
            throw new RegraNegocioException("Informe a tarefa da qual esta tarefa depende.");
        }
        Tarefa dependeDe = buscarTarefaPorId(dependeDeTarefaId);
        if (tarefa.getId().equals(dependeDe.getId())) {
            throw new RegraNegocioException("Uma tarefa nao pode depender dela mesma.");
        }
        if (repositorioDependencia.existsByTarefaIdAndDependeDeTarefaId(tarefaId, dependeDeTarefaId)) {
            throw new RegraNegocioException("Essa dependencia ja foi cadastrada.");
        }
        if (criaCicloDeDependencia(tarefaId, dependeDeTarefaId)) {
            throw new RegraNegocioException(
                    "A dependencia criaria um ciclo entre as tarefas e nao pode ser adicionada.");
        }
        return repositorioDependencia.save(new DependenciaTarefa(tarefaId, dependeDeTarefaId));
    }

    public void removerDependenciaInterna(Long tarefaId, Long dependenciaId) {
        DependenciaTarefa dependencia = repositorioDependencia.findById(dependenciaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Dependencia interna nao encontrada: " + dependenciaId));
        if (!dependencia.getTarefaId().equals(tarefaId)) {
            throw new RecursoNaoEncontradoException(
                    "Dependencia " + dependenciaId + " nao pertence a tarefa " + tarefaId);
        }
        repositorioDependencia.delete(dependencia);
    }

    private boolean criaCicloDeDependencia(Long tarefaId, Long dependeDeTarefaId) {
        // Existe ciclo se a tarefa alvo da dependencia ja depende (direta ou
        // transitivamente) da tarefa de origem.
        Deque<Long> pendentes = new ArrayDeque<>();
        Set<Long> visitadas = new HashSet<>();
        pendentes.push(dependeDeTarefaId);
        while (!pendentes.isEmpty()) {
            Long atual = pendentes.pop();
            if (atual.equals(tarefaId)) {
                return true;
            }
            if (visitadas.add(atual)) {
                for (DependenciaTarefa dependencia : repositorioDependencia.findByTarefaIdOrderByIdAsc(atual)) {
                    pendentes.push(dependencia.getDependeDeTarefaId());
                }
            }
        }
        return false;
    }

    // ----- Dependencias externas -----

    @Transactional(readOnly = true)
    public List<DependenciaExterna> listarDependenciasExternas(Long tarefaId) {
        buscarTarefaPorId(tarefaId);
        return repositorioDependenciaExterna.findByTarefaIdOrderByIdAsc(tarefaId);
    }

    public DependenciaExterna adicionarDependenciaExterna(Long tarefaId, String titulo,
                                                          String descricao, LocalDate previstoPara) {
        validarTitulo(titulo);
        buscarTarefaPorId(tarefaId);
        return repositorioDependenciaExterna.save(
                new DependenciaExterna(tarefaId, titulo.trim(), descricao, previstoPara));
    }

    public DependenciaExterna atualizarDependenciaExterna(Long tarefaId, Long dependenciaId,
                                                          String titulo, String descricao,
                                                          LocalDate previstoPara, Boolean resolvida) {
        validarTitulo(titulo);
        DependenciaExterna dependencia = dependenciaExternaObrigatoria(tarefaId, dependenciaId);
        dependencia.setTitulo(titulo.trim());
        dependencia.setDescricao(descricao);
        dependencia.setPrevistoPara(previstoPara);
        if (resolvida != null) {
            dependencia.setResolvida(resolvida);
        }
        dependencia.setAtualizadoEm(LocalDateTime.now());
        return repositorioDependenciaExterna.save(dependencia);
    }

    public void removerDependenciaExterna(Long tarefaId, Long dependenciaId) {
        DependenciaExterna dependencia = dependenciaExternaObrigatoria(tarefaId, dependenciaId);
        repositorioDependenciaExterna.delete(dependencia);
    }

    public DependenciaExterna marcarDependenciaExternaResolvida(Long tarefaId, Long dependenciaId) {
        DependenciaExterna dependencia = dependenciaExternaObrigatoria(tarefaId, dependenciaId);
        dependencia.setResolvida(true);
        dependencia.setAtualizadoEm(LocalDateTime.now());
        return repositorioDependenciaExterna.save(dependencia);
    }

    private DependenciaExterna dependenciaExternaObrigatoria(Long tarefaId, Long dependenciaId) {
        DependenciaExterna dependencia = repositorioDependenciaExterna.findById(dependenciaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Dependencia externa nao encontrada: " + dependenciaId));
        if (!dependencia.getTarefaId().equals(tarefaId)) {
            throw new RecursoNaoEncontradoException(
                    "Dependencia externa " + dependenciaId + " nao pertence a tarefa " + tarefaId);
        }
        return dependencia;
    }

    // ----- Bloqueios -----

    @Transactional(readOnly = true)
    public List<BloqueioTarefa> listarBloqueios(Long tarefaId) {
        Tarefa tarefa = buscarTarefaPorId(tarefaId);
        List<BloqueioTarefa> bloqueios = new ArrayList<>();
        if (tarefa.getEstado() != EstadoTarefa.A_FAZER) {
            bloqueios.add(new BloqueioTarefa(TipoBloqueio.REGRA_ESTADO,
                    "Tarefa no estado " + tarefa.getEstado() + " nao pode ser iniciada.",
                    null, null));
        }
        bloqueios.addAll(bloqueiosDependenciasInternas(tarefa));
        for (DependenciaExterna externa : repositorioDependenciaExterna
                .findByTarefaIdOrderByIdAsc(tarefaId)) {
            if (!externa.isResolvida()) {
                bloqueios.add(new BloqueioTarefa(TipoBloqueio.DEPENDENCIA_EXTERNA,
                        "Dependencia externa pendente: " + externa.getTitulo()
                                + " (nao impede o inicio da tarefa)",
                        null, null));
            }
        }
        return bloqueios;
    }

    private List<BloqueioTarefa> bloqueiosDependenciasInternas(Tarefa tarefa) {
        List<BloqueioTarefa> bloqueios = new ArrayList<>();
        for (DependenciaTarefa dependencia : repositorioDependencia
                .findByTarefaIdOrderByIdAsc(tarefa.getId())) {
            repositorioTarefa.findById(dependencia.getDependeDeTarefaId())
                    .filter(dependeDe -> dependeDe.getEstado() != EstadoTarefa.CONCLUIDA)
                    .ifPresent(dependeDe -> bloqueios.add(new BloqueioTarefa(
                            TipoBloqueio.DEPENDENCIA_INTERNA,
                            "Depende da tarefa '" + dependeDe.getTitulo()
                                    + "' (estado " + dependeDe.getEstado() + ")",
                            dependeDe.getId(), dependeDe.getTitulo())));
        }
        return bloqueios;
    }

    @Transactional(readOnly = true)
    public boolean estaBloqueada(Tarefa tarefa) {
        return tarefa.getEstado() == EstadoTarefa.A_FAZER
                && !bloqueiosDependenciasInternas(tarefa).isEmpty();
    }

    @Transactional(readOnly = true)
    public boolean possuiSubtarefas(Long tarefaId) {
        return repositorioTarefa.existsByTarefaPaiId(tarefaId);
    }

    @Transactional(readOnly = true)
    public boolean possuiDependencias(Long tarefaId) {
        return repositorioDependencia.existsByTarefaId(tarefaId);
    }

    // ----- Notas -----

    public NotaTarefa adicionarNota(Long tarefaId, String texto) {
        if (texto == null || texto.isBlank()) {
            throw new RegraNegocioException("O texto da nota e obrigatorio.");
        }
        buscarTarefaPorId(tarefaId);
        return repositorioNota.save(new NotaTarefa(tarefaId, texto.trim()));
    }

    @Transactional(readOnly = true)
    public List<NotaTarefa> listarNotas(Long tarefaId) {
        buscarTarefaPorId(tarefaId);
        return repositorioNota.findByTarefaIdOrderByIdAsc(tarefaId);
    }

    private void validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new RegraNegocioException("O titulo e obrigatorio.");
        }
    }
}
