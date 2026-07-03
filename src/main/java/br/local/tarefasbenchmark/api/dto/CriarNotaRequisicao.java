package br.local.tarefasbenchmark.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarNotaRequisicao(
        @NotBlank(message = "O texto da nota e obrigatorio.") String texto) {
}
