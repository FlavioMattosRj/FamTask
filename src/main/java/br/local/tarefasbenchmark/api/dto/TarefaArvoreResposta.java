package br.local.tarefasbenchmark.api.dto;

import br.local.tarefasbenchmark.dominio.EstadoTarefa;
import br.local.tarefasbenchmark.dominio.PrioridadeTarefa;

import java.time.LocalDate;
import java.util.List;

public record TarefaArvoreResposta(
        Long id,
        Long tarefaPaiId,
        String titulo,
        EstadoTarefa estado,
        PrioridadeTarefa prioridade,
        LocalDate previstoPara,
        boolean temSubtarefas,
        boolean temDependencias,
        boolean bloqueada,
        List<TarefaArvoreResposta> subtarefas) {
}
