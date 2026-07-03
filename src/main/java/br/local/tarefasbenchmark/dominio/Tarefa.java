package br.local.tarefasbenchmark.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarefas")
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tarefa_pai_id")
    private Long tarefaPaiId;

    @Column(nullable = false)
    private String titulo;

    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTarefa estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadeTarefa prioridade;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "previsto_para")
    private LocalDate previstoPara;

    @Column(name = "iniciado_em")
    private LocalDateTime iniciadoEm;

    @Column(name = "concluido_em")
    private LocalDateTime concluidoEm;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    protected Tarefa() {
    }

    public Tarefa(Long tarefaPaiId, String titulo, String descricao,
                  PrioridadeTarefa prioridade, LocalDate previstoPara) {
        LocalDateTime agora = LocalDateTime.now();
        this.tarefaPaiId = tarefaPaiId;
        this.titulo = titulo;
        this.descricao = descricao;
        this.estado = EstadoTarefa.A_FAZER;
        this.prioridade = prioridade != null ? prioridade : PrioridadeTarefa.MEDIA;
        this.previstoPara = previstoPara;
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    public Long getId() {
        return id;
    }

    public Long getTarefaPaiId() {
        return tarefaPaiId;
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

    public EstadoTarefa getEstado() {
        return estado;
    }

    public void setEstado(EstadoTarefa estado) {
        this.estado = estado;
    }

    public PrioridadeTarefa getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(PrioridadeTarefa prioridade) {
        this.prioridade = prioridade;
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

    public LocalDate getPrevistoPara() {
        return previstoPara;
    }

    public void setPrevistoPara(LocalDate previstoPara) {
        this.previstoPara = previstoPara;
    }

    public LocalDateTime getIniciadoEm() {
        return iniciadoEm;
    }

    public void setIniciadoEm(LocalDateTime iniciadoEm) {
        this.iniciadoEm = iniciadoEm;
    }

    public LocalDateTime getConcluidoEm() {
        return concluidoEm;
    }

    public void setConcluidoEm(LocalDateTime concluidoEm) {
        this.concluidoEm = concluidoEm;
    }

    public LocalDateTime getCanceladoEm() {
        return canceladoEm;
    }

    public void setCanceladoEm(LocalDateTime canceladoEm) {
        this.canceladoEm = canceladoEm;
    }
}
