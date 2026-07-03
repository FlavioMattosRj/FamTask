package br.local.tarefasbenchmark.dominio;

public record BloqueioTarefa(
        TipoBloqueio tipo,
        String mensagem,
        Long tarefaBloqueadoraId,
        String tituloTarefaBloqueadora) {
}
