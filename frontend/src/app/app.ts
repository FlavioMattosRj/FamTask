import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ItemTarefa } from './item-tarefa';
import { TarefaServico } from './tarefa-servico';
import {
  DadosTarefa,
  DependenciaExterna,
  PrioridadeTarefa,
  ROTULO_ESTADO,
  ROTULO_PRIORIDADE,
  TarefaArvore,
  TarefaDetalhe,
  TarefaResumo,
} from './modelos';

type ModoFormularioTarefa = 'nova' | 'editar' | 'subtarefa';

@Component({
  selector: 'app-root',
  imports: [FormsModule, DatePipe, ItemTarefa],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  private servico = inject(TarefaServico);

  rotuloEstado = ROTULO_ESTADO;
  rotuloPrioridade = ROTULO_PRIORIDADE;
  prioridades: PrioridadeTarefa[] = ['BAIXA', 'MEDIA', 'ALTA', 'URGENTE'];

  arvore = signal<TarefaArvore[]>([]);
  resultadosPesquisa = signal<TarefaResumo[] | null>(null);
  detalhe = signal<TarefaDetalhe | null>(null);
  erro = signal<string | null>(null);
  private temporizadorErro: ReturnType<typeof setTimeout> | null = null;

  textoPesquisa = '';

  // Formulario de tarefa (nova, edicao ou subtarefa)
  modoFormulario = signal<ModoFormularioTarefa | null>(null);
  formularioTitulo = '';
  formularioDescricao = '';
  formularioPrioridade: PrioridadeTarefa = 'MEDIA';
  formularioPrevistoPara = '';

  // Dependencia interna
  textoPesquisaDependencia = '';
  candidatasDependencia = signal<TarefaResumo[] | null>(null);

  // Dependencia externa
  formularioExternaAberto = signal(false);
  externaEmEdicao = signal<DependenciaExterna | null>(null);
  externaTitulo = '';
  externaDescricao = '';
  externaPrevistoPara = '';

  textoNota = '';

  ngOnInit(): void {
    this.carregarArvore();
  }

  carregarArvore(): void {
    this.servico.listarArvore().subscribe({
      next: (arvore) => this.arvore.set(arvore),
      error: (erro) => this.mostrarErro(erro),
    });
  }

  selecionarTarefa(id: number): void {
    this.servico.buscar(id).subscribe({
      next: (detalhe) => {
        this.detalhe.set(detalhe);
        this.modoFormulario.set(null);
        this.formularioExternaAberto.set(false);
        this.externaEmEdicao.set(null);
        this.candidatasDependencia.set(null);
        this.textoPesquisaDependencia = '';
      },
      error: (erro) => this.mostrarErro(erro),
    });
  }

  private recarregar(): void {
    this.carregarArvore();
    const selecionada = this.detalhe();
    if (selecionada) {
      this.servico.buscar(selecionada.id).subscribe({
        next: (detalhe) => this.detalhe.set(detalhe),
        error: () => this.detalhe.set(null),
      });
    }
    if (this.resultadosPesquisa() !== null) {
      this.pesquisar();
    }
  }

  // ----- Pesquisa da lista principal -----

  pesquisar(): void {
    const texto = this.textoPesquisa.trim();
    if (!texto) {
      this.limparPesquisa();
      return;
    }
    this.servico.pesquisar(texto).subscribe({
      next: (resultados) => this.resultadosPesquisa.set(resultados),
      error: (erro) => this.mostrarErro(erro),
    });
  }

  limparPesquisa(): void {
    this.textoPesquisa = '';
    this.resultadosPesquisa.set(null);
  }

  // ----- Formulario de tarefa -----

  abrirFormularioNova(): void {
    this.modoFormulario.set('nova');
    this.preencherFormulario('', '', 'MEDIA', '');
  }

  abrirFormularioSubtarefa(): void {
    this.modoFormulario.set('subtarefa');
    this.preencherFormulario('', '', 'MEDIA', '');
  }

  abrirFormularioEdicao(): void {
    const tarefa = this.detalhe();
    if (!tarefa) {
      return;
    }
    this.modoFormulario.set('editar');
    this.preencherFormulario(
      tarefa.titulo,
      tarefa.descricao ?? '',
      tarefa.prioridade,
      tarefa.previstoPara ?? '',
    );
  }

  private preencherFormulario(
    titulo: string,
    descricao: string,
    prioridade: PrioridadeTarefa,
    previstoPara: string,
  ): void {
    this.formularioTitulo = titulo;
    this.formularioDescricao = descricao;
    this.formularioPrioridade = prioridade;
    this.formularioPrevistoPara = previstoPara;
  }

  cancelarFormulario(): void {
    this.modoFormulario.set(null);
  }

  salvarFormulario(): void {
    const dados: DadosTarefa = {
      titulo: this.formularioTitulo,
      descricao: this.formularioDescricao || null,
      prioridade: this.formularioPrioridade,
      previstoPara: this.formularioPrevistoPara || null,
    };
    const modo = this.modoFormulario();
    const selecionada = this.detalhe();

    if (modo === 'nova') {
      this.servico.criar(dados).subscribe({
        next: (criada) => {
          this.modoFormulario.set(null);
          this.detalhe.set(criada);
          this.recarregar();
        },
        error: (erro) => this.mostrarErro(erro),
      });
    } else if (modo === 'subtarefa' && selecionada) {
      this.servico.criarSubtarefa(selecionada.id, dados).subscribe({
        next: () => {
          this.modoFormulario.set(null);
          this.recarregar();
        },
        error: (erro) => this.mostrarErro(erro),
      });
    } else if (modo === 'editar' && selecionada) {
      this.servico.atualizar(selecionada.id, dados).subscribe({
        next: (atualizada) => {
          this.modoFormulario.set(null);
          this.detalhe.set(atualizada);
          this.recarregar();
        },
        error: (erro) => this.mostrarErro(erro),
      });
    }
  }

  excluirTarefa(): void {
    const tarefa = this.detalhe();
    if (!tarefa) {
      return;
    }
    if (!confirm(`Excluir a tarefa "${tarefa.titulo}" e todas as suas subtarefas?`)) {
      return;
    }
    this.servico.excluir(tarefa.id).subscribe({
      next: () => {
        this.detalhe.set(null);
        this.recarregar();
      },
      error: (erro) => this.mostrarErro(erro),
    });
  }

  // ----- Transicoes de estado -----

  iniciarTarefa(): void {
    this.executarTransicao((id) => this.servico.iniciar(id));
  }

  concluirTarefa(): void {
    this.executarTransicao((id) => this.servico.concluir(id));
  }

  cancelarTarefa(): void {
    this.executarTransicao((id) => this.servico.cancelar(id));
  }

  private executarTransicao(acao: (id: number) => ReturnType<TarefaServico['iniciar']>): void {
    const tarefa = this.detalhe();
    if (!tarefa) {
      return;
    }
    acao(tarefa.id).subscribe({
      next: (atualizada) => {
        this.detalhe.set(atualizada);
        this.carregarArvore();
      },
      error: (erro) => this.mostrarErro(erro),
    });
  }

  // ----- Dependencias internas -----

  pesquisarDependencia(): void {
    const texto = this.textoPesquisaDependencia.trim();
    if (!texto) {
      this.candidatasDependencia.set(null);
      return;
    }
    this.servico.pesquisar(texto).subscribe({
      next: (resultados) => {
        const tarefa = this.detalhe();
        // A propria tarefa e dependencias ja cadastradas nao sao candidatas validas
        const idsExistentes = new Set(
          tarefa?.dependenciasInternas.map((dependencia) => dependencia.dependeDeTarefaId) ?? [],
        );
        this.candidatasDependencia.set(
          resultados.filter(
            (candidata) => candidata.id !== tarefa?.id && !idsExistentes.has(candidata.id),
          ),
        );
      },
      error: (erro) => this.mostrarErro(erro),
    });
  }

  adicionarDependencia(candidata: TarefaResumo): void {
    const tarefa = this.detalhe();
    if (!tarefa) {
      return;
    }
    this.servico.adicionarDependenciaInterna(tarefa.id, candidata.id).subscribe({
      next: () => {
        this.textoPesquisaDependencia = '';
        this.candidatasDependencia.set(null);
        this.recarregar();
      },
      error: (erro) => this.mostrarErro(erro),
    });
  }

  removerDependencia(dependenciaId: number): void {
    const tarefa = this.detalhe();
    if (!tarefa) {
      return;
    }
    this.servico.removerDependenciaInterna(tarefa.id, dependenciaId).subscribe({
      next: () => this.recarregar(),
      error: (erro) => this.mostrarErro(erro),
    });
  }

  // ----- Dependencias externas -----

  abrirFormularioExternaNova(): void {
    this.externaEmEdicao.set(null);
    this.externaTitulo = '';
    this.externaDescricao = '';
    this.externaPrevistoPara = '';
    this.formularioExternaAberto.set(true);
  }

  abrirFormularioExternaEdicao(dependencia: DependenciaExterna): void {
    this.externaEmEdicao.set(dependencia);
    this.externaTitulo = dependencia.titulo;
    this.externaDescricao = dependencia.descricao ?? '';
    this.externaPrevistoPara = dependencia.previstoPara ?? '';
    this.formularioExternaAberto.set(true);
  }

  cancelarFormularioExterna(): void {
    this.formularioExternaAberto.set(false);
    this.externaEmEdicao.set(null);
  }

  salvarDependenciaExterna(): void {
    const tarefa = this.detalhe();
    if (!tarefa) {
      return;
    }
    const emEdicao = this.externaEmEdicao();
    const dados = {
      titulo: this.externaTitulo,
      descricao: this.externaDescricao || null,
      previstoPara: this.externaPrevistoPara || null,
    };
    const requisicao = emEdicao
      ? this.servico.atualizarDependenciaExterna(tarefa.id, emEdicao.id, {
          ...dados,
          resolvida: emEdicao.resolvida,
        })
      : this.servico.adicionarDependenciaExterna(tarefa.id, dados);
    requisicao.subscribe({
      next: () => {
        this.cancelarFormularioExterna();
        this.recarregar();
      },
      error: (erro) => this.mostrarErro(erro),
    });
  }

  resolverDependenciaExterna(dependenciaId: number): void {
    const tarefa = this.detalhe();
    if (!tarefa) {
      return;
    }
    this.servico.resolverDependenciaExterna(tarefa.id, dependenciaId).subscribe({
      next: () => this.recarregar(),
      error: (erro) => this.mostrarErro(erro),
    });
  }

  removerDependenciaExterna(dependenciaId: number): void {
    const tarefa = this.detalhe();
    if (!tarefa) {
      return;
    }
    this.servico.removerDependenciaExterna(tarefa.id, dependenciaId).subscribe({
      next: () => this.recarregar(),
      error: (erro) => this.mostrarErro(erro),
    });
  }

  // ----- Notas -----

  adicionarNota(): void {
    const tarefa = this.detalhe();
    const texto = this.textoNota.trim();
    if (!tarefa || !texto) {
      return;
    }
    this.servico.adicionarNota(tarefa.id, texto).subscribe({
      next: () => {
        this.textoNota = '';
        this.recarregar();
      },
      error: (erro) => this.mostrarErro(erro),
    });
  }

  // ----- Erros -----

  private mostrarErro(erro: unknown): void {
    let mensagem = 'Erro inesperado ao comunicar com a API.';
    if (erro instanceof HttpErrorResponse && erro.error?.mensagem) {
      mensagem = erro.error.mensagem;
    }
    this.erro.set(mensagem);
    if (this.temporizadorErro) {
      clearTimeout(this.temporizadorErro);
    }
    this.temporizadorErro = setTimeout(() => this.erro.set(null), 6000);
  }
}
