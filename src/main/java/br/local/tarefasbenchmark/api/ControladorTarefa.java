package br.local.tarefasbenchmark.api;

import br.local.tarefasbenchmark.api.dto.AtualizarDependenciaExternaRequisicao;
import br.local.tarefasbenchmark.api.dto.AtualizarTarefaRequisicao;
import br.local.tarefasbenchmark.api.dto.BloqueioResposta;
import br.local.tarefasbenchmark.api.dto.CriarDependenciaExternaRequisicao;
import br.local.tarefasbenchmark.api.dto.CriarDependenciaInternaRequisicao;
import br.local.tarefasbenchmark.api.dto.CriarNotaRequisicao;
import br.local.tarefasbenchmark.api.dto.CriarTarefaRequisicao;
import br.local.tarefasbenchmark.api.dto.DependenciaExternaResposta;
import br.local.tarefasbenchmark.api.dto.DependenciaInternaResposta;
import br.local.tarefasbenchmark.api.dto.NotaTarefaResposta;
import br.local.tarefasbenchmark.api.dto.TarefaArvoreResposta;
import br.local.tarefasbenchmark.api.dto.TarefaDetalheResposta;
import br.local.tarefasbenchmark.api.dto.TarefaResumoResposta;
import br.local.tarefasbenchmark.aplicacao.ServicoTarefa;
import br.local.tarefasbenchmark.dominio.DependenciaExterna;
import br.local.tarefasbenchmark.dominio.DependenciaTarefa;
import br.local.tarefasbenchmark.dominio.NotaTarefa;
import br.local.tarefasbenchmark.dominio.Tarefa;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tarefas")
public class ControladorTarefa {

    private final ServicoTarefa servico;
    private final MontadorRespostas montador;

    public ControladorTarefa(ServicoTarefa servico, MontadorRespostas montador) {
        this.servico = servico;
        this.montador = montador;
    }

    // ----- Tarefas -----

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaDetalheResposta criarTarefa(@Valid @RequestBody CriarTarefaRequisicao requisicao) {
        Tarefa tarefa = servico.criarTarefa(requisicao.titulo(), requisicao.descricao(),
                requisicao.prioridade(), requisicao.previstoPara());
        return montador.detalhe(tarefa);
    }

    @GetMapping
    public List<TarefaResumoResposta> listarTarefas() {
        return montador.resumos(servico.listarTarefas());
    }

    @GetMapping("/arvore")
    public List<TarefaArvoreResposta> listarArvoreTarefas() {
        return servico.listarArvoreTarefas().stream().map(montador::arvore).toList();
    }

    @GetMapping("/pesquisa")
    public List<TarefaResumoResposta> pesquisarTarefas(@RequestParam(required = false) String texto) {
        return montador.resumos(servico.pesquisarTarefas(texto));
    }

    @GetMapping("/{id}")
    public TarefaDetalheResposta buscarTarefaPorId(@PathVariable Long id) {
        return montador.detalhe(servico.buscarTarefaPorId(id));
    }

    @PutMapping("/{id}")
    public TarefaDetalheResposta atualizarTarefa(@PathVariable Long id,
                                                 @Valid @RequestBody AtualizarTarefaRequisicao requisicao) {
        Tarefa tarefa = servico.atualizarTarefa(id, requisicao.titulo(), requisicao.descricao(),
                requisicao.prioridade(), requisicao.previstoPara());
        return montador.detalhe(tarefa);
    }

    @DeleteMapping("/{id}")
    public void excluirTarefa(@PathVariable Long id) {
        servico.excluirTarefa(id);
    }

    // ----- Subtarefas -----

    @PostMapping("/{id}/subtarefas")
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaDetalheResposta criarSubtarefa(@PathVariable Long id,
                                                @Valid @RequestBody CriarTarefaRequisicao requisicao) {
        Tarefa subtarefa = servico.criarSubtarefa(id, requisicao.titulo(), requisicao.descricao(),
                requisicao.prioridade(), requisicao.previstoPara());
        return montador.detalhe(subtarefa);
    }

    @GetMapping("/{id}/subtarefas")
    public List<TarefaResumoResposta> listarSubtarefas(@PathVariable Long id) {
        return montador.resumos(servico.listarSubtarefas(id));
    }

    // ----- Estados -----

    @PostMapping("/{id}/iniciar")
    public TarefaDetalheResposta iniciarTarefa(@PathVariable Long id) {
        return montador.detalhe(servico.iniciarTarefa(id));
    }

    @PostMapping("/{id}/concluir")
    public TarefaDetalheResposta concluirTarefa(@PathVariable Long id) {
        return montador.detalhe(servico.concluirTarefa(id));
    }

    @PostMapping("/{id}/cancelar")
    public TarefaDetalheResposta cancelarTarefa(@PathVariable Long id) {
        return montador.detalhe(servico.cancelarTarefa(id));
    }

    // ----- Dependencias internas -----

    @GetMapping("/{id}/dependencias")
    public List<DependenciaInternaResposta> listarDependencias(@PathVariable Long id) {
        return montador.dependenciasInternas(servico.listarDependencias(id));
    }

    @PostMapping("/{id}/dependencias/internas")
    @ResponseStatus(HttpStatus.CREATED)
    public DependenciaInternaResposta adicionarDependenciaInterna(
            @PathVariable Long id,
            @Valid @RequestBody CriarDependenciaInternaRequisicao requisicao) {
        DependenciaTarefa dependencia =
                servico.adicionarDependenciaInterna(id, requisicao.dependeDeTarefaId());
        return montador.dependenciaInterna(dependencia);
    }

    @DeleteMapping("/{id}/dependencias/internas/{dependenciaId}")
    public void removerDependenciaInterna(@PathVariable Long id,
                                          @PathVariable Long dependenciaId) {
        servico.removerDependenciaInterna(id, dependenciaId);
    }

    // ----- Dependencias externas -----

    @PostMapping("/{id}/dependencias/externas")
    @ResponseStatus(HttpStatus.CREATED)
    public DependenciaExternaResposta adicionarDependenciaExterna(
            @PathVariable Long id,
            @Valid @RequestBody CriarDependenciaExternaRequisicao requisicao) {
        DependenciaExterna dependencia = servico.adicionarDependenciaExterna(
                id, requisicao.titulo(), requisicao.descricao(), requisicao.previstoPara());
        return montador.dependenciaExterna(dependencia);
    }

    @PutMapping("/{id}/dependencias/externas/{dependenciaId}")
    public DependenciaExternaResposta atualizarDependenciaExterna(
            @PathVariable Long id,
            @PathVariable Long dependenciaId,
            @Valid @RequestBody AtualizarDependenciaExternaRequisicao requisicao) {
        DependenciaExterna dependencia = servico.atualizarDependenciaExterna(
                id, dependenciaId, requisicao.titulo(), requisicao.descricao(),
                requisicao.previstoPara(), requisicao.resolvida());
        return montador.dependenciaExterna(dependencia);
    }

    @DeleteMapping("/{id}/dependencias/externas/{dependenciaId}")
    public void removerDependenciaExterna(@PathVariable Long id,
                                          @PathVariable Long dependenciaId) {
        servico.removerDependenciaExterna(id, dependenciaId);
    }

    @PostMapping("/{id}/dependencias/externas/{dependenciaId}/resolver")
    public DependenciaExternaResposta marcarDependenciaExternaResolvida(
            @PathVariable Long id,
            @PathVariable Long dependenciaId) {
        return montador.dependenciaExterna(
                servico.marcarDependenciaExternaResolvida(id, dependenciaId));
    }

    // ----- Bloqueios -----

    @GetMapping("/{id}/bloqueios")
    public List<BloqueioResposta> listarBloqueios(@PathVariable Long id) {
        return montador.bloqueios(servico.listarBloqueios(id));
    }

    // ----- Notas -----

    @PostMapping("/{id}/notas")
    @ResponseStatus(HttpStatus.CREATED)
    public NotaTarefaResposta adicionarNota(@PathVariable Long id,
                                            @Valid @RequestBody CriarNotaRequisicao requisicao) {
        NotaTarefa nota = servico.adicionarNota(id, requisicao.texto());
        return montador.nota(nota);
    }

    @GetMapping("/{id}/notas")
    public List<NotaTarefaResposta> listarNotas(@PathVariable Long id) {
        return montador.notas(servico.listarNotas(id));
    }
}
