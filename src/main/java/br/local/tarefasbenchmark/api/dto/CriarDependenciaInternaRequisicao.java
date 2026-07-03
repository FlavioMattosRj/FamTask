package br.local.tarefasbenchmark.api.dto;

import jakarta.validation.constraints.NotNull;

public record CriarDependenciaInternaRequisicao(
        @NotNull(message = "Informe a tarefa da qual esta tarefa depende.") Long dependeDeTarefaId) {
}
