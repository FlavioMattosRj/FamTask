package br.local.tarefasbenchmark.api.dto;

import br.local.tarefasbenchmark.dominio.TipoBloqueio;

public record BloqueioResposta(
        TipoBloqueio tipo,
        String mensagem,
        Long tarefaBloqueadoraId,
        String tituloTarefaBloqueadora) {
}
