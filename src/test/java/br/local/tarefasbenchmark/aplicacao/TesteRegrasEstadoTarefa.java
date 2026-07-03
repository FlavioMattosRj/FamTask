package br.local.tarefasbenchmark.aplicacao;

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
class TesteRegrasEstadoTarefa {

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
        lenient().when(repositorioTarefa.save(any(Tarefa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
        lenient().when(repositorioDependencia.findByTarefaIdOrderByIdAsc(any()))
                .thenReturn(List.of());
    }

    private Tarefa tarefaComId(Long id, EstadoTarefa estado) {
        Tarefa tarefa = new Tarefa(null, "Tarefa " + id, "", PrioridadeTarefa.MEDIA, null);
        ReflectionTestUtils.setField(tarefa, "id", id);
        tarefa.setEstado(estado);
        return tarefa;
    }

    @Test
    void novaTarefaComecaEmAFazerComPrioridadePadrao() {
        Tarefa tarefa = servico.criarTarefa("Nova tarefa", "descricao", null, null);

        assertThat(tarefa.getEstado()).isEqualTo(EstadoTarefa.A_FAZER);
        assertThat(tarefa.getPrioridade()).isEqualTo(PrioridadeTarefa.MEDIA);
        assertThat(tarefa.getCriadoEm()).isNotNull();
        assertThat(tarefa.getAtualizadoEm()).isNotNull();
    }

    @Test
    void criarTarefaSemTituloFalha() {
        assertThatThrownBy(() -> servico.criarTarefa("  ", null, null, null))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void iniciarTarefaAFazerPreencheIniciadoEm() {
        Tarefa tarefa = tarefaComId(1L, EstadoTarefa.A_FAZER);
        when(repositorioTarefa.findById(1L)).thenReturn(Optional.of(tarefa));

        Tarefa iniciada = servico.iniciarTarefa(1L);

        assertThat(iniciada.getEstado()).isEqualTo(EstadoTarefa.EM_EXECUCAO);
        assertThat(iniciada.getIniciadoEm()).isNotNull();
    }

    @Test
    void naoIniciaTarefaJaEmExecucao() {
        Tarefa tarefa = tarefaComId(1L, EstadoTarefa.EM_EXECUCAO);
        when(repositorioTarefa.findById(1L)).thenReturn(Optional.of(tarefa));

        assertThatThrownBy(() -> servico.iniciarTarefa(1L))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void concluirTarefaEmExecucaoPreencheConcluidoEm() {
        Tarefa tarefa = tarefaComId(1L, EstadoTarefa.EM_EXECUCAO);
        when(repositorioTarefa.findById(1L)).thenReturn(Optional.of(tarefa));

        Tarefa concluida = servico.concluirTarefa(1L);

        assertThat(concluida.getEstado()).isEqualTo(EstadoTarefa.CONCLUIDA);
        assertThat(concluida.getConcluidoEm()).isNotNull();
    }

    @Test
    void naoConcluiTarefaAFazer() {
        Tarefa tarefa = tarefaComId(1L, EstadoTarefa.A_FAZER);
        when(repositorioTarefa.findById(1L)).thenReturn(Optional.of(tarefa));

        assertThatThrownBy(() -> servico.concluirTarefa(1L))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void cancelaTarefaAFazerOuEmExecucao() {
        Tarefa aFazer = tarefaComId(1L, EstadoTarefa.A_FAZER);
        Tarefa emExecucao = tarefaComId(2L, EstadoTarefa.EM_EXECUCAO);
        when(repositorioTarefa.findById(1L)).thenReturn(Optional.of(aFazer));
        when(repositorioTarefa.findById(2L)).thenReturn(Optional.of(emExecucao));

        assertThat(servico.cancelarTarefa(1L).getEstado()).isEqualTo(EstadoTarefa.CANCELADA);
        assertThat(servico.cancelarTarefa(2L).getCanceladoEm()).isNotNull();
    }

    @Test
    void naoCancelaTarefaFinalizada() {
        Tarefa concluida = tarefaComId(1L, EstadoTarefa.CONCLUIDA);
        Tarefa cancelada = tarefaComId(2L, EstadoTarefa.CANCELADA);
        when(repositorioTarefa.findById(1L)).thenReturn(Optional.of(concluida));
        when(repositorioTarefa.findById(2L)).thenReturn(Optional.of(cancelada));

        assertThatThrownBy(() -> servico.cancelarTarefa(1L))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> servico.cancelarTarefa(2L))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void tarefaInexistenteLancaNaoEncontrado() {
        when(repositorioTarefa.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servico.iniciarTarefa(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
