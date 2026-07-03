package br.local.tarefasbenchmark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class TarefasBenchmarkApplication {

    public static void main(String[] args) {
        criarDiretorioDados();
        SpringApplication aplicacao = new SpringApplication(TarefasBenchmarkApplication.class);
        // Necessario para abrir o navegador padrao via java.awt.Desktop
        aplicacao.setHeadless(false);
        aplicacao.run(args);
    }

    static void criarDiretorioDados() {
        Path diretorio = Path.of(System.getProperty("user.home"), ".tarefas-benchmark");
        try {
            Files.createDirectories(diretorio);
        } catch (IOException excecao) {
            throw new UncheckedIOException(
                    "Nao foi possivel criar o diretorio de dados: " + diretorio, excecao);
        }
    }
}
