package br.local.tarefasbenchmark.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record AtualizarDependenciaExternaRequisicao(
        @NotBlank(message = "O titulo e obrigatorio.") String titulo,
        String descricao,
        LocalDate previstoPara,
        Boolean resolvida) {
}
