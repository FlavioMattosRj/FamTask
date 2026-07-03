# Tarefas Benchmark

Aplicacao local de tarefas para benchmark Maven, construida conforme [requisitos.md](requisitos.md).

Arquitetura: **Angular 20** (interface) + **Spring Boot 3.5 / Java 21** (API REST) + **SQLite** (persistencia), empacotados em um unico JAR executavel.

## Pre-requisitos de build

- JDK 21+
- Maven 3.9+
- Node.js 20+ e npm (usados pelo build integrado do frontend)

Em tempo de execucao basta o Java.

## Build

```text
mvn clean package
```

O build Maven compila o backend, roda os testes, executa `npm install` e `npm run build` na pasta `frontend/` e copia os arquivos compilados do Angular para dentro do JAR.

Resultado: `target/tarefas-benchmark.jar`

Apenas testes:

```text
mvn test
```

(`mvn test` nao dispara o build do Angular, o que mantem o ciclo de testes rapido.)

## Execucao

```text
java -jar target\tarefas-benchmark.jar
```

Ao iniciar, a aplicacao:

1. cria o diretorio de dados `%USERPROFILE%\.tarefas-benchmark`, se necessario;
2. abre ou cria o banco SQLite `tarefas-benchmark.db` nesse diretorio;
3. sobe a API REST em `http://localhost:8080/api`;
4. serve a interface Angular em `http://localhost:8080/`;
5. abre o navegador padrao automaticamente.

Para nao abrir o navegador:

```text
java -jar target\tarefas-benchmark.jar --app.abrirNavegador=false
```

Para trocar a porta (configuracao padrao do Spring Boot):

```text
java -jar target\tarefas-benchmark.jar --server.port=9090
```

## Desenvolvimento do frontend

Para desenvolver a interface com recarga automatica, rode o backend (`mvn spring-boot:run`) e, em outra janela:

```text
cd frontend
npm start
```

O `ng serve` atende em `http://localhost:4200`; configure um proxy para `/api` se desejar, ou use o build integrado.

## Estrutura

```text
tarefas-benchmark/
├── pom.xml                  Build Maven integrado (backend + frontend)
├── frontend/                Aplicacao Angular
└── src/
    ├── main/java/br/local/tarefasbenchmark/
    │   ├── dominio/         Entidades (Tarefa, DependenciaTarefa, DependenciaExterna, NotaTarefa) e enums
    │   ├── aplicacao/       ServicoTarefa (casos de uso e regras de negocio)
    │   ├── persistencia/    Repositorios Spring Data JPA (SQLite)
    │   ├── api/             Controlador REST, DTOs e tratamento de erros
    │   └── app/             Abertura automatica do navegador
    ├── main/resources/      application.properties e schema.sql
    └── test/java/           Testes unitarios, de API e de persistencia
```
