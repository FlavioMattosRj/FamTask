package br.local.tarefasbenchmark.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CriarDependenciaExternaRequisicao(
        @NotBlank(message = "O titulo e obrigatorio.") String titulo,
        String descricao,
        LocalDate previstoPara) {
}
