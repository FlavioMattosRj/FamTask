package br.local.tarefasbenchmark.persistencia;

import br.local.tarefasbenchmark.dominio.DependenciaTarefa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioDependenciaTarefa extends JpaRepository<DependenciaTarefa, Long> {

    List<DependenciaTarefa> findByTarefaIdOrderByIdAsc(Long tarefaId);

    boolean existsByTarefaId(Long tarefaId);

    boolean existsByTarefaIdAndDependeDeTarefaId(Long tarefaId, Long dependeDeTarefaId);

    void deleteByTarefaIdOrDependeDeTarefaId(Long tarefaId, Long dependeDeTarefaId);
}
