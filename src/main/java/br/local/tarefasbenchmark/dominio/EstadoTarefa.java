package br.local.tarefasbenchmark.dominio;

public enum EstadoTarefa {
    A_FAZER,
    EM_EXECUCAO,
    CONCLUIDA,
    CANCELADA;

    public boolean finalizada() {
        return this == CONCLUIDA || this == CANCELADA;
    }
}
