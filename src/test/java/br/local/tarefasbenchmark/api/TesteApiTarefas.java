package br.local.tarefasbenchmark.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TesteApiTarefas {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long criarTarefa(String titulo) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"" + titulo + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode corpo = objectMapper.readTree(resultado.getResponse().getContentAsString());
        return corpo.get("id").asLong();
    }

    @Test
    void criarTarefaRetorna201ComEstadoInicial() throws Exception {
        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Tarefa de teste API",
                                 "descricao":"descricao livre",
                                 "prioridade":"ALTA",
                                 "previstoPara":"2026-12-31"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo").value("Tarefa de teste API"))
                .andExpect(jsonPath("$.estado").value("A_FAZER"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"))
                .andExpect(jsonPath("$.criadoEm").isNotEmpty());
    }

    @Test
    void criarTarefaSemTituloRetorna400() throws Exception {
        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\":\"sem titulo\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void listarTarefasRetorna200ComTarefaCriada() throws Exception {
        long id = criarTarefa("Tarefa para listagem");

        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")].titulo")
                        .value("Tarefa para listagem"));
    }

    @Test
    void fluxoIniciarEConcluirTarefa() throws Exception {
        long id = criarTarefa("Tarefa de fluxo completo");

        mockMvc.perform(post("/api/tarefas/{id}/iniciar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$.iniciadoEm").isNotEmpty());

        mockMvc.perform(post("/api/tarefas/{id}/concluir", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONCLUIDA"))
                .andExpect(jsonPath("$.concluidoEm").isNotEmpty());

        // Tarefa finalizada nao pode ser cancelada
        mockMvc.perform(post("/api/tarefas/{id}/cancelar", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void cancelarTarefaAFazer() throws Exception {
        long id = criarTarefa("Tarefa a cancelar");

        mockMvc.perform(post("/api/tarefas/{id}/cancelar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"))
                .andExpect(jsonPath("$.canceladoEm").isNotEmpty());
    }

    @Test
    void concluirSemIniciarRetorna400() throws Exception {
        long id = criarTarefa("Tarefa nao iniciada");

        mockMvc.perform(post("/api/tarefas/{id}/concluir", id))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tarefaComDependenciaNaoConcluidaNaoInicia() throws Exception {
        long dependente = criarTarefa("Tarefa dependente");
        long requisito = criarTarefa("Tarefa requisito");

        mockMvc.perform(post("/api/tarefas/{id}/dependencias/internas", dependente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dependeDeTarefaId\":" + requisito + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tarefas/{id}/iniciar", dependente))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/tarefas/{id}/bloqueios", dependente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("DEPENDENCIA_INTERNA"));
    }

    @Test
    void buscarTarefaInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/tarefas/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void excluirTarefaRetorna200EDepois404() throws Exception {
        long id = criarTarefa("Tarefa a excluir");

        mockMvc.perform(delete("/api/tarefas/{id}", id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tarefas/{id}", id))
                .andExpect(status().isNotFound());
    }
}
