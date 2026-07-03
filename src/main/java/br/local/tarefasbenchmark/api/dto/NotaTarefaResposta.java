package br.local.tarefasbenchmark.api.dto;

import java.time.LocalDateTime;

public record NotaTarefaResposta(
        Long id,
        Long tarefaId,
        String texto,
        LocalDateTime criadoEm) {
}
