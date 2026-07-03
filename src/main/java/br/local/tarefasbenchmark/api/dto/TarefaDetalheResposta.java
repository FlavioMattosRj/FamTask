package br.local.tarefasbenchmark.api.dto;

import br.local.tarefasbenchmark.dominio.EstadoTarefa;
import br.local.tarefasbenchmark.dominio.PrioridadeTarefa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TarefaDetalheResposta(
        Long id,
        Long tarefaPaiId,
        String titulo,
        String descricao,
        EstadoTarefa estado,
        PrioridadeTarefa prioridade,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        LocalDate previstoPara,
        LocalDateTime iniciadoEm,
        LocalDateTime concluidoEm,
        LocalDateTime canceladoEm,
        List<TarefaResumoResposta> subtarefas,
        List<DependenciaInternaResposta> dependenciasInternas,
        List<DependenciaExternaResposta> dependenciasExternas,
        List<NotaTarefaResposta> notas,
        List<BloqueioResposta> bloqueios) {
}
