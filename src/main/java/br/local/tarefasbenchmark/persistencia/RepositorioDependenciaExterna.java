package br.local.tarefasbenchmark.persistencia;

import br.local.tarefasbenchmark.dominio.DependenciaExterna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioDependenciaExterna extends JpaRepository<DependenciaExterna, Long> {

    List<DependenciaExterna> findByTarefaIdOrderByIdAsc(Long tarefaId);

    void deleteByTarefaId(Long tarefaId);
}
