package br.local.tarefasbenchmark.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DependenciaExternaResposta(
        Long id,
        Long tarefaId,
        String titulo,
        String descricao,
        LocalDate previstoPara,
        boolean resolvida,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm) {
}
