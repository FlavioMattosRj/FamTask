import { DatePipe } from '@angular/common';
import { Component, forwardRef, input, output, signal } from '@angular/core';
import { ROTULO_ESTADO, ROTULO_PRIORIDADE, TarefaArvore } from './modelos';

@Component({
  selector: 'app-item-tarefa',
  // forwardRef permite ao componente usar a si mesmo recursivamente
  imports: [DatePipe, forwardRef(() => ItemTarefa)],
  template: `
    <div
      class="item-tarefa"
      [class.selecionada]="no().id === selecionadaId()"
      [class.concluida]="no().estado === 'CONCLUIDA'"
      [class.cancelada]="no().estado === 'CANCELADA'"
      (click)="selecionar.emit(no().id); $event.stopPropagation()"
    >
      <button
        class="alternar"
        type="button"
        [class.invisivel]="no().subtarefas.length === 0"
        (click)="aberta.set(!aberta()); $event.stopPropagation()"
        [attr.aria-label]="aberta() ? 'Recolher subtarefas' : 'Expandir subtarefas'"
      >
        {{ aberta() ? '▾' : '▸' }}
      </button>
      <span class="titulo">{{ no().titulo }}</span>
      <span class="etiqueta estado estado-{{ no().estado }}">{{ rotuloEstado[no().estado] }}</span>
      <span class="etiqueta prioridade prioridade-{{ no().prioridade }}">
        {{ rotuloPrioridade[no().prioridade] }}
      </span>
      @if (no().previstoPara) {
        <span class="etiqueta data" title="Previsto para">
          📅 {{ no().previstoPara | date: 'dd/MM/yyyy' }}
        </span>
      }
      @if (no().bloqueada) {
        <span class="etiqueta bloqueada" title="Tarefa bloqueada por dependencias">🔒 Bloqueada</span>
      }
      @if (no().temDependencias) {
        <span class="indicador" title="Possui dependencias internas">🔗</span>
      }
      @if (no().subtarefas.length > 0) {
        <span class="indicador" title="Possui subtarefas">🗂 {{ no().subtarefas.length }}</span>
      }
    </div>
    @if (aberta() && no().subtarefas.length > 0) {
      <ul class="lista-subtarefas">
        @for (subtarefa of no().subtarefas; track subtarefa.id) {
          <li>
            <app-item-tarefa
              [no]="subtarefa"
              [selecionadaId]="selecionadaId()"
              (selecionar)="selecionar.emit($event)"
            />
          </li>
        }
      </ul>
    }
  `,
})
export class ItemTarefa {
  no = input.required<TarefaArvore>();
  selecionadaId = input<number | null>(null);
  selecionar = output<number>();

  aberta = signal(true);

  rotuloEstado = ROTULO_ESTADO;
  rotuloPrioridade = ROTULO_PRIORIDADE;
}
