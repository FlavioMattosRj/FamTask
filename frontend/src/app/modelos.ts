export type EstadoTarefa = 'A_FAZER' | 'EM_EXECUCAO' | 'CONCLUIDA' | 'CANCELADA';
export type PrioridadeTarefa = 'BAIXA' | 'MEDIA' | 'ALTA' | 'URGENTE';
export type TipoBloqueio = 'DEPENDENCIA_INTERNA' | 'DEPENDENCIA_EXTERNA' | 'REGRA_ESTADO';

export interface TarefaResumo {
  id: number;
  tarefaPaiId: number | null;
  titulo: string;
  estado: EstadoTarefa;
  prioridade: PrioridadeTarefa;
  previstoPara: string | null;
  temSubtarefas: boolean;
  temDependencias: boolean;
  bloqueada: boolean;
}

export interface TarefaArvore extends TarefaResumo {
  subtarefas: TarefaArvore[];
}

export interface Bloqueio {
  tipo: TipoBloqueio;
  mensagem: string;
  tarefaBloqueadoraId: number | null;
  tituloTarefaBloqueadora: string | null;
}

export interface DependenciaInterna {
  id: number;
  tarefaId: number;
  dependeDeTarefaId: number;
  tituloTarefaDependencia: string;
  estadoTarefaDependencia: EstadoTarefa;
  criadoEm: string;
}

export interface DependenciaExterna {
  id: number;
  tarefaId: number;
  titulo: string;
  descricao: string | null;
  previstoPara: string | null;
  resolvida: boolean;
  criadoEm: string;
  atualizadoEm: string;
}

export interface Nota {
  id: number;
  tarefaId: number;
  texto: string;
  criadoEm: string;
}

export interface TarefaDetalhe {
  id: number;
  tarefaPaiId: number | null;
  titulo: string;
  descricao: string | null;
  estado: EstadoTarefa;
  prioridade: PrioridadeTarefa;
  criadoEm: string;
  atualizadoEm: string;
  previstoPara: string | null;
  iniciadoEm: string | null;
  concluidoEm: string | null;
  canceladoEm: string | null;
  subtarefas: TarefaResumo[];
  dependenciasInternas: DependenciaInterna[];
  dependenciasExternas: DependenciaExterna[];
  notas: Nota[];
  bloqueios: Bloqueio[];
}

export interface DadosTarefa {
  titulo: string;
  descricao: string | null;
  prioridade: PrioridadeTarefa;
  previstoPara: string | null;
}

export interface DadosDependenciaExterna {
  titulo: string;
  descricao: string | null;
  previstoPara: string | null;
  resolvida?: boolean;
}

export const ROTULO_ESTADO: Record<EstadoTarefa, string> = {
  A_FAZER: 'A fazer',
  EM_EXECUCAO: 'Em execucao',
  CONCLUIDA: 'Concluida',
  CANCELADA: 'Cancelada',
};

export const ROTULO_PRIORIDADE: Record<PrioridadeTarefa, string> = {
  BAIXA: 'Baixa',
  MEDIA: 'Media',
  ALTA: 'Alta',
  URGENTE: 'Urgente',
};
