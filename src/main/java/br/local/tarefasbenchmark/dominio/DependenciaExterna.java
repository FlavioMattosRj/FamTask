package br.local.tarefasbenchmark.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dependencias_externas")
public class DependenciaExterna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tarefa_id", nullable = false)
    private Long tarefaId;

    @Column(nullable = false)
    private String titulo;

    private String descricao;

    @Column(name = "previsto_para")
    private LocalDate previstoPara;

    @Column(nullable = false)
    private boolean resolvida;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    protected DependenciaExterna() {
    }

    public DependenciaExterna(Long tarefaId, String titulo, String descricao, LocalDate previstoPara) {
        LocalDateTime agora = LocalDateTime.now();
        this.tarefaId = tarefaId;
        this.titulo = titulo;
        this.descricao = descricao;
        this.previstoPara = previstoPara;
        this.resolvida = false;
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    public Long getId() {
        return id;
    }

    public Long getTarefaId() {
        return tarefaId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getPrevistoPara() {
        return previstoPara;
    }

    public void setPrevistoPara(LocalDate previstoPara) {
        this.previstoPara = previstoPara;
    }

    public boolean isResolvida() {
        return resolvida;
    }

    public void setResolvida(boolean resolvida) {
        this.resolvida = resolvida;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
