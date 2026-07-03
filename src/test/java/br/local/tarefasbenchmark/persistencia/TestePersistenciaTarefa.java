package br.local.tarefasbenchmark.persistencia;

import br.local.tarefasbenchmark.dominio.EstadoTarefa;
import br.local.tarefasbenchmark.dominio.PrioridadeTarefa;
import br.local.tarefasbenchmark.dominio.Tarefa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties =
        "spring.datasource.url=jdbc:sqlite:target/teste-persistencia-tarefa.db")
class TestePersistenciaTarefa {

    @Autowired
    private RepositorioTarefa repositorio;

    @Autowired
    private TestEntityManager gerenciadorEntidades;

    @Test
    void salvaECarregaTarefaDoSqlite() {
        Tarefa tarefa = new Tarefa(null, "Tarefa persistida",
                "descricao persistida", PrioridadeTarefa.URGENTE, LocalDate.of(2026, 12, 25));
        Long id = repositorio.saveAndFlush(tarefa).getId();
        gerenciadorEntidades.clear();

        Tarefa carregada = repositorio.findById(id).orElseThrow();

        assertThat(carregada.getTitulo()).isEqualTo("Tarefa persistida");
        assertThat(carregada.getDescricao()).isEqualTo("descricao persistida");
        assertThat(carregada.getEstado()).isEqualTo(EstadoTarefa.A_FAZER);
        assertThat(carregada.getPrioridade()).isEqualTo(PrioridadeTarefa.URGENTE);
        assertThat(carregada.getPrevistoPara()).isEqualTo(LocalDate.of(2026, 12, 25));
        assertThat(carregada.getCriadoEm()).isNotNull();
        assertThat(carregada.getAtualizadoEm()).isNotNull();
    }

    @Test
    void listaSubtarefasPorTarefaPai() {
        Tarefa pai = repositorio.saveAndFlush(
                new Tarefa(null, "Tarefa pai", null, null, null));
        repositorio.saveAndFlush(
                new Tarefa(pai.getId(), "Subtarefa 1", null, null, null));
        repositorio.saveAndFlush(
                new Tarefa(pai.getId(), "Subtarefa 2", null, null, null));

        List<Tarefa> subtarefas = repositorio.findByTarefaPaiIdOrderByIdAsc(pai.getId());

        assertThat(subtarefas).hasSize(2);
        assertThat(repositorio.existsByTarefaPaiId(pai.getId())).isTrue();
    }

    @Test
    void pesquisaTarefaPorTexto() {
        repositorio.saveAndFlush(new Tarefa(null, "Publicar relatorio anual",
                "enviar para a gerencia", null, null));

        List<Tarefa> porTitulo = repositorio.pesquisarPorTexto("relatorio ANUAL");
        List<Tarefa> porDescricao = repositorio.pesquisarPorTexto("gerencia");

        assertThat(porTitulo).isNotEmpty();
        assertThat(porDescricao).isNotEmpty();
    }
}
