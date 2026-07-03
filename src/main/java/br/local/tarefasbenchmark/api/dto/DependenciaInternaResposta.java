package br.local.tarefasbenchmark.api.dto;

import br.local.tarefasbenchmark.dominio.EstadoTarefa;

import java.time.LocalDateTime;

public record DependenciaInternaResposta(
        Long id,
        Long tarefaId,
        Long dependeDeTarefaId,
        String tituloTarefaDependencia,
        EstadoTarefa estadoTarefaDependencia,
        LocalDateTime criadoEm) {
}
