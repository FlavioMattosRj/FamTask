package br.local.tarefasbenchmark.persistencia;

import br.local.tarefasbenchmark.dominio.NotaTarefa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioNotaTarefa extends JpaRepository<NotaTarefa, Long> {

    List<NotaTarefa> findByTarefaIdOrderByIdAsc(Long tarefaId);

    void deleteByTarefaId(Long tarefaId);
}
