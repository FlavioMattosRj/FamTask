package br.local.tarefasbenchmark.persistencia;

import br.local.tarefasbenchmark.dominio.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RepositorioTarefa extends JpaRepository<Tarefa, Long> {

    List<Tarefa> findByTarefaPaiIdOrderByIdAsc(Long tarefaPaiId);

    List<Tarefa> findByTarefaPaiIdIsNullOrderByIdAsc();

    boolean existsByTarefaPaiId(Long tarefaPaiId);

    @Query("""
            select t from Tarefa t
            where lower(t.titulo) like lower(concat('%', :texto, '%'))
               or lower(t.descricao) like lower(concat('%', :texto, '%'))
            order by t.id asc
            """)
    List<Tarefa> pesquisarPorTexto(@Param("texto") String texto);
}
