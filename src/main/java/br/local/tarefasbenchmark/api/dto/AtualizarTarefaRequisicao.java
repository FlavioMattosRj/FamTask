package br.local.tarefasbenchmark.api.dto;

import br.local.tarefasbenchmark.dominio.PrioridadeTarefa;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record AtualizarTarefaRequisicao(
        @NotBlank(message = "O titulo e obrigatorio.") String titulo,
        String descricao,
        PrioridadeTarefa prioridade,
        LocalDate previstoPara) {
}
