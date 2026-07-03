import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Bloqueio,
  DadosDependenciaExterna,
  DadosTarefa,
  DependenciaExterna,
  DependenciaInterna,
  Nota,
  TarefaArvore,
  TarefaDetalhe,
  TarefaResumo,
} from './modelos';

@Injectable({ providedIn: 'root' })
export class TarefaServico {
  private http = inject(HttpClient);
  private base = '/api/tarefas';

  listarTarefas(): Observable<TarefaResumo[]> {
    return this.http.get<TarefaResumo[]>(this.base);
  }

  listarArvore(): Observable<TarefaArvore[]> {
    return this.http.get<TarefaArvore[]>(`${this.base}/arvore`);
  }

  pesquisar(texto: string): Observable<TarefaResumo[]> {
    return this.http.get<TarefaResumo[]>(`${this.base}/pesquisa`, { params: { texto } });
  }

  buscar(id: number): Observable<TarefaDetalhe> {
    return this.http.get<TarefaDetalhe>(`${this.base}/${id}`);
  }

  criar(dados: DadosTarefa): Observable<TarefaDetalhe> {
    return this.http.post<TarefaDetalhe>(this.base, dados);
  }

  atualizar(id: number, dados: DadosTarefa): Observable<TarefaDetalhe> {
    return this.http.put<TarefaDetalhe>(`${this.base}/${id}`, dados);
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  criarSubtarefa(tarefaPaiId: number, dados: DadosTarefa): Observable<TarefaDetalhe> {
    return this.http.post<TarefaDetalhe>(`${this.base}/${tarefaPaiId}/subtarefas`, dados);
  }

  iniciar(id: number): Observable<TarefaDetalhe> {
    return this.http.post<TarefaDetalhe>(`${this.base}/${id}/iniciar`, {});
  }

  concluir(id: number): Observable<TarefaDetalhe> {
    return this.http.post<TarefaDetalhe>(`${this.base}/${id}/concluir`, {});
  }

  cancelar(id: number): Observable<TarefaDetalhe> {
    return this.http.post<TarefaDetalhe>(`${this.base}/${id}/cancelar`, {});
  }

  listarBloqueios(id: number): Observable<Bloqueio[]> {
    return this.http.get<Bloqueio[]>(`${this.base}/${id}/bloqueios`);
  }

  adicionarDependenciaInterna(id: number, dependeDeTarefaId: number): Observable<DependenciaInterna> {
    return this.http.post<DependenciaInterna>(`${this.base}/${id}/dependencias/internas`, {
      dependeDeTarefaId,
    });
  }

  removerDependenciaInterna(id: number, dependenciaId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}/dependencias/internas/${dependenciaId}`);
  }

  adicionarDependenciaExterna(
    id: number,
    dados: DadosDependenciaExterna,
  ): Observable<DependenciaExterna> {
    return this.http.post<DependenciaExterna>(`${this.base}/${id}/dependencias/externas`, dados);
  }

  atualizarDependenciaExterna(
    id: number,
    dependenciaId: number,
    dados: DadosDependenciaExterna,
  ): Observable<DependenciaExterna> {
    return this.http.put<DependenciaExterna>(
      `${this.base}/${id}/dependencias/externas/${dependenciaId}`,
      dados,
    );
  }

  removerDependenciaExterna(id: number, dependenciaId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}/dependencias/externas/${dependenciaId}`);
  }

  resolverDependenciaExterna(id: number, dependenciaId: number): Observable<DependenciaExterna> {
    return this.http.post<DependenciaExterna>(
      `${this.base}/${id}/dependencias/externas/${dependenciaId}/resolver`,
      {},
    );
  }

  adicionarNota(id: number, texto: string): Observable<Nota> {
    return this.http.post<Nota>(`${this.base}/${id}/notas`, { texto });
  }

  listarNotas(id: number): Observable<Nota[]> {
    return this.http.get<Nota[]>(`${this.base}/${id}/notas`);
  }
}
