package br.local.tarefasbenchmark.api;

import br.local.tarefasbenchmark.api.dto.BloqueioResposta;
import br.local.tarefasbenchmark.api.dto.DependenciaExternaResposta;
import br.local.tarefasbenchmark.api.dto.DependenciaInternaResposta;
import br.local.tarefasbenchmark.api.dto.NotaTarefaResposta;
import br.local.tarefasbenchmark.api.dto.TarefaArvoreResposta;
import br.local.tarefasbenchmark.api.dto.TarefaDetalheResposta;
import br.local.tarefasbenchmark.api.dto.TarefaResumoResposta;
import br.local.tarefasbenchmark.aplicacao.ServicoTarefa;
import br.local.tarefasbenchmark.dominio.BloqueioTarefa;
import br.local.tarefasbenchmark.dominio.DependenciaExterna;
import br.local.tarefasbenchmark.dominio.DependenciaTarefa;
import br.local.tarefasbenchmark.dominio.NotaTarefa;
import br.local.tarefasbenchmark.dominio.Tarefa;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MontadorRespostas {

    private final ServicoTarefa servico;

    public MontadorRespostas(ServicoTarefa servico) {
        this.servico = servico;
    }

    public TarefaResumoResposta resumo(Tarefa tarefa) {
        return new TarefaResumoResposta(
                tarefa.getId(),
                tarefa.getTarefaPaiId(),
                tarefa.getTitulo(),
                tarefa.getEstado(),
                tarefa.getPrioridade(),
                tarefa.getPrevistoPara(),
                servico.possuiSubtarefas(tarefa.getId()),
                servico.possuiDependencias(tarefa.getId()),
                servico.estaBloqueada(tarefa));
    }

    public List<TarefaResumoResposta> resumos(List<Tarefa> tarefas) {
        return tarefas.stream().map(this::resumo).toList();
    }

    public TarefaArvoreResposta arvore(Tarefa tarefa) {
        List<TarefaArvoreResposta> subtarefas = servico.listarSubtarefas(tarefa.getId())
                .stream().map(this::arvore).toList();
        return new TarefaArvoreResposta(
                tarefa.getId(),
                tarefa.getTarefaPaiId(),
                tarefa.getTitulo(),
                tarefa.getEstado(),
                tarefa.getPrioridade(),
                tarefa.getPrevistoPara(),
                !subtarefas.isEmpty(),
                servico.possuiDependencias(tarefa.getId()),
                servico.estaBloqueada(tarefa),
                subtarefas);
    }

    public TarefaDetalheResposta detalhe(Tarefa tarefa) {
        return new TarefaDetalheResposta(
                tarefa.getId(),
                tarefa.getTarefaPaiId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getEstado(),
                tarefa.getPrioridade(),
                tarefa.getCriadoEm(),
                tarefa.getAtualizadoEm(),
                tarefa.getPrevistoPara(),
                tarefa.getIniciadoEm(),
                tarefa.getConcluidoEm(),
                tarefa.getCanceladoEm(),
                resumos(servico.listarSubtarefas(tarefa.getId())),
                dependenciasInternas(servico.listarDependencias(tarefa.getId())),
                dependenciasExternas(servico.listarDependenciasExternas(tarefa.getId())),
                notas(servico.listarNotas(tarefa.getId())),
                bloqueios(servico.listarBloqueios(tarefa.getId())));
    }

    public DependenciaInternaResposta dependenciaInterna(DependenciaTarefa dependencia) {
        Tarefa dependeDe = servico.buscarTarefaPorId(dependencia.getDependeDeTarefaId());
        return new DependenciaInternaResposta(
                dependencia.getId(),
                dependencia.getTarefaId(),
                dependencia.getDependeDeTarefaId(),
                dependeDe.getTitulo(),
                dependeDe.getEstado(),
                dependencia.getCriadoEm());
    }

    public List<DependenciaInternaResposta> dependenciasInternas(List<DependenciaTarefa> dependencias) {
        return dependencias.stream().map(this::dependenciaInterna).toList();
    }

    public DependenciaExternaResposta dependenciaExterna(DependenciaExterna dependencia) {
        return new DependenciaExternaResposta(
                dependencia.getId(),
                dependencia.getTarefaId(),
                dependencia.getTitulo(),
                dependencia.getDescricao(),
                dependencia.getPrevistoPara(),
                dependencia.isResolvida(),
                dependencia.getCriadoEm(),
                dependencia.getAtualizadoEm());
    }

    public List<DependenciaExternaResposta> dependenciasExternas(List<DependenciaExterna> dependencias) {
        return dependencias.stream().map(this::dependenciaExterna).toList();
    }

    public NotaTarefaResposta nota(NotaTarefa nota) {
        return new NotaTarefaResposta(nota.getId(), nota.getTarefaId(),
                nota.getTexto(), nota.getCriadoEm());
    }

    public List<NotaTarefaResposta> notas(List<NotaTarefa> notas) {
        return notas.stream().map(this::nota).toList();
    }

    public List<BloqueioResposta> bloqueios(List<BloqueioTarefa> bloqueios) {
        return bloqueios.stream()
                .map(b -> new BloqueioResposta(b.tipo(), b.mensagem(),
                        b.tarefaBloqueadoraId(), b.tituloTarefaBloqueadora()))
                .toList();
    }
}
