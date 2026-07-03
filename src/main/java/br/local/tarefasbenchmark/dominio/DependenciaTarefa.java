package br.local.tarefasbenchmark.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "dependencias_tarefa",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tarefa_id", "depende_de_tarefa_id"}))
public class DependenciaTarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tarefa_id", nullable = false)
    private Long tarefaId;

    @Column(name = "depende_de_tarefa_id", nullable = false)
    private Long dependeDeTarefaId;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    protected DependenciaTarefa() {
    }

    public DependenciaTarefa(Long tarefaId, Long dependeDeTarefaId) {
        this.tarefaId = tarefaId;
        this.dependeDeTarefaId = dependeDeTarefaId;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getTarefaId() {
        return tarefaId;
    }

    public Long getDependeDeTarefaId() {
        return dependeDeTarefaId;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
