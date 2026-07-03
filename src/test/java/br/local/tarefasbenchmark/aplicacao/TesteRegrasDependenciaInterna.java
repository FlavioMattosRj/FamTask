package br.local.tarefasbenchmark.aplicacao;

import br.local.tarefasbenchmark.dominio.DependenciaTarefa;
import br.local.tarefasbenchmark.dominio.EstadoTarefa;
import br.local.tarefasbenchmark.dominio.PrioridadeTarefa;
import br.local.tarefasbenchmark.dominio.Tarefa;
import br.local.tarefasbenchmark.persistencia.RepositorioDependenciaExterna;
import br.local.tarefasbenchmark.persistencia.RepositorioDependenciaTarefa;
import br.local.tarefasbenchmark.persistencia.RepositorioNotaTarefa;
import br.local.tarefasbenchmark.persistencia.RepositorioTarefa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TesteRegrasDependenciaInterna {

    @Mock
    private RepositorioTarefa repositorioTarefa;
    @Mock
    private RepositorioDependenciaTarefa repositorioDependencia;
    @Mock
    private RepositorioDependenciaExterna repositorioDependenciaExterna;
    @Mock
    private RepositorioNotaTarefa repositorioNota;

    private ServicoTarefa servico;

    @BeforeEach
    void preparar() {
        servico = new ServicoTarefa(repositorioTarefa, repositorioDependencia,
                repositorioDependenciaExterna, repositorioNota);
        lenient().when(repositorioDependencia.save(any(DependenciaTarefa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
        lenient().when(repositorioTarefa.save(any(Tarefa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private Tarefa tarefaComId(Long id, EstadoTarefa estado) {
        Tarefa tarefa = new Tarefa(null, "Tarefa " + id, "", PrioridadeTarefa.MEDIA, null);
        ReflectionTestUtils.setField(tarefa, "id", id);
        tarefa.setEstado(estado);
        lenient().when(repositorioTarefa.findById(id)).thenReturn(Optional.of(tarefa));
        return tarefa;
    }

    private DependenciaTarefa dependencia(Long id, Long tarefaId, Long dependeDeTarefaId) {
        DependenciaTarefa dependencia = new DependenciaTarefa(tarefaId, dependeDeTarefaId);
        ReflectionTestUtils.setField(dependencia, "id", id);
        return dependencia;
    }

    @Test
    void adicionaDependenciaValida() {
        tarefaComId(1L, EstadoTarefa.A_FAZER);
        tarefaComId(2L, EstadoTarefa.A_FAZER);
        when(repositorioDependencia.existsByTarefaIdAndDependeDeTarefaId(1L, 2L)).thenReturn(false);
        when(repositorioDependencia.findByTarefaIdOrderByIdAsc(2L)).thenReturn(List.of());

        DependenciaTarefa criada = servico.adicionarDependenciaInterna(1L, 2L);

        assertThat(criada.getTarefaId()).isEqualTo(1L);
        assertThat(criada.getDependeDeTarefaId()).isEqualTo(2L);
        assertThat(criada.getCriadoEm()).isNotNull();
    }

    @Test
    void naoPermiteDependerDeSiMesma() {
        tarefaComId(1L, EstadoTarefa.A_FAZER);

        assertThatThrownBy(() -> servico.adicionarDependenciaInterna(1L, 1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("dela mesma");
    }

    @Test
    void naoPermiteDependenciaDuplicada() {
        tarefaComId(1L, EstadoTarefa.A_FAZER);
        tarefaComId(2L, EstadoTarefa.A_FAZER);
        when(repositorioDependencia.existsByTarefaIdAndDependeDeTarefaId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> servico.adicionarDependenciaInterna(1L, 2L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("ja foi cadastrada");
    }

    @Test
    void impedeCicloDeDependencia() {
        // Tarefa 1 ja depende da tarefa 2; tentar fazer 2 depender de 1 criaria ciclo
        tarefaComId(1L, EstadoTarefa.A_FAZER);
        tarefaComId(2L, EstadoTarefa.A_FAZER);
        when(repositorioDependencia.existsByTarefaIdAndDependeDeTarefaId(2L, 1L)).thenReturn(false);
        when(repositorioDependencia.findByTarefaIdOrderByIdAsc(1L))
                .thenReturn(List.of(dependencia(10L, 1L, 2L)));

        assertThatThrownBy(() -> servico.adicionarDependenciaInterna(2L, 1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("ciclo");
    }

    @Test
    void impedeCicloTransitivo() {
        // 1 depende de 2, 2 depende de 3; fazer 3 depender de 1 fecha o ciclo
        tarefaComId(1L, EstadoTarefa.A_FAZER);
        tarefaComId(3L, EstadoTarefa.A_FAZER);
        when(repositorioDependencia.existsByTarefaIdAndDependeDeTarefaId(3L, 1L)).thenReturn(false);
        when(repositorioDependencia.findByTarefaIdOrderByIdAsc(1L))
                .thenReturn(List.of(dependencia(10L, 1L, 2L)));
        when(repositorioDependencia.findByTarefaIdOrderByIdAsc(2L))
                .thenReturn(List.of(dependencia(11L, 2L, 3L)));

        assertThatThrownBy(() -> servico.adicionarDependenciaInterna(3L, 1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("ciclo");
    }

    @Test
    void naoIniciaTarefaComDependenciaNaoConcluida() {
        Tarefa tarefa = tarefaComId(1L, EstadoTarefa.A_FAZER);
        tarefaComId(2L, EstadoTarefa.A_FAZER);
        when(repositorioDependencia.findByTarefaIdOrderByIdAsc(tarefa.getId()))
                .thenReturn(List.of(dependencia(10L, 1L, 2L)));

        assertThatThrownBy(() -> servico.iniciarTarefa(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("dependencias nao concluidas");
    }

    @Test
    void iniciaTarefaComTodasDependenciasConcluidas() {
        Tarefa tarefa = tarefaComId(1L, EstadoTarefa.A_FAZER);
        tarefaComId(2L, EstadoTarefa.CONCLUIDA);
        when(repositorioDependencia.findByTarefaIdOrderByIdAsc(tarefa.getId()))
                .thenReturn(List.of(dependencia(10L, 1L, 2L)));

        Tarefa iniciada = servico.iniciarTarefa(1L);

        assertThat(iniciada.getEstado()).isEqualTo(EstadoTarefa.EM_EXECUCAO);
    }

    @Test
    void tarefaComDependenciaNaoConcluidaFicaBloqueada() {
        Tarefa tarefa = tarefaComId(1L, EstadoTarefa.A_FAZER);
        tarefaComId(2L, EstadoTarefa.EM_EXECUCAO);
        when(repositorioDependencia.findByTarefaIdOrderByIdAsc(tarefa.getId()))
                .thenReturn(List.of(dependencia(10L, 1L, 2L)));

        assertThat(servico.estaBloqueada(tarefa)).isTrue();
    }
}
