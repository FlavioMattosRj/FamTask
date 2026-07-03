# Especificacao de Requisitos — Aplicacao Local de Tarefas para Benchmark Maven

## 1. Objetivo

Desenvolver uma aplicacao local de tarefas, funcional e simples, usando Java Maven, Spring Boot REST, Angular e SQLite.

A aplicacao servira para:

1. testar desenvolvimento assistido por IA;
2. representar uma arquitetura concreta com backend, frontend e banco local;
3. gerar um projeto Maven realista para benchmark de build;
4. produzir um JAR executavel que rode localmente no Windows.

A aplicacao nao tem objetivo de producao. Ela deve funcionar minimamente, mesmo que existam limitacoes ou bugs nao criticos.

## 2. Convencoes de nomenclatura

REQ-NOME-001 — Todos os nomes de dominio, campos, estados, endpoints, DTOs, tabelas e metodos especificos da aplicacao devem usar portugues brasileiro sem acentos.

REQ-NOME-002 — Exemplos aceitaveis:

```
tarefa
subtarefa
dependencia
descricao
execucao
concluida
criadoEm
atualizadoEm
```

REQ-NOME-003 — Exemplos a evitar:

```
task
subtask
dependency
description
done
createdAt
updatedAt
```

REQ-NOME-004 — Termos tecnicos padrao podem permanecer em ingles quando forem nomes de tecnologias, protocolos, ferramentas ou convencoes externas.

Exemplos:

```
Spring Boot
Angular
Maven
SQLite
REST
JSON
JAR
HTTP
GET
POST
PUT
DELETE
```

## 3. Premissas

REQ-PREM-001 — A aplicacao sera executada em Windows.

REQ-PREM-002 — Os arquivos de dados da aplicacao devem ser criados dentro da pasta de usuario do Windows.

REQ-PREM-003 — A aplicacao deve ser local, sem servidor externo obrigatorio.

REQ-PREM-004 — A aplicacao nao tera autenticacao.

REQ-PREM-005 — A aplicacao nao tera autorizacao por usuario ou perfil.

REQ-PREM-006 — A aplicacao nao tera preocupacao de seguranca de producao.

REQ-PREM-007 — A aplicacao nao tera migracao formal de schema.

REQ-PREM-008 — O banco pode ser criado automaticamente na inicializacao.

REQ-PREM-009 — O banco pode ser recriado ou ajustado de forma simples durante o desenvolvimento.

REQ-PREM-010 — O produto final deve ser um JAR executavel.

REQ-PREM-011 — Ao executar o JAR, a aplicacao deve subir o Spring Boot, abrir ou criar o banco SQLite, servir a interface Angular e abrir o navegador padrao.

## 4. Arquitetura geral

A arquitetura alvo sera:

```
Navegador padrao do Windows
        ↓
Interface Angular servida localmente
        ↓ HTTP localhost
Spring Boot REST API
        ↓
Servicos de aplicacao
        ↓
Persistencia SQLite
        ↓
Arquivo .db na pasta do usuario
```

REQ-ARQ-001 — O Angular sera o cliente da API REST.

REQ-ARQ-002 — O Spring Boot devera servir a API REST.

REQ-ARQ-003 — O Spring Boot tambem devera servir os arquivos estaticos gerados pelo build Angular.

REQ-ARQ-004 — Nao deve haver servidor Angular separado no produto final.

REQ-ARQ-005 — O JAR final devera conter os arquivos compilados do Angular.

REQ-ARQ-006 — A comunicacao entre Angular e Spring Boot sera feita por HTTP local.

REQ-ARQ-007 — A URL base da API sera:

```text
/api
```

REQ-ARQ-008 — A URL da interface sera servida na raiz da aplicacao local.

Exemplo:

```text
http://localhost:8080/
```

REQ-ARQ-009 — A porta padrao sera `8080`.

REQ-ARQ-010 — A porta deve poder ser alterada por configuracao padrao do Spring Boot.

REQ-ARQ-011 — A aplicacao deve abrir automaticamente o navegador padrao na inicializacao.

REQ-ARQ-012 — A abertura automatica do navegador deve poder ser desligada por propriedade simples.

Exemplo:

```text
java -jar tarefas-benchmark.jar --app.abrirNavegador=false
```

## 5. Estrutura recomendada do projeto

A estrutura recomendada e multi-modulo Maven simples:

```text
tarefas-benchmark/
├── pom.xml
├── tarefas-dominio/
├── tarefas-aplicacao/
├── tarefas-persistencia/
├── tarefas-api/
├── tarefas-ui/
└── tarefas-app/
```

Alternativamente, para acelerar o MVP, e aceitavel uma estrutura de modulo unico Spring Boot, desde que os pacotes internos preservem a separacao logica.

Estrutura alternativa aceitavel:

```text
tarefas-benchmark/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── br/
    │   │       └── local/
    │   │           └── tarefasbenchmark/
    │   │               ├── dominio/
    │   │               ├── aplicacao/
    │   │               ├── persistencia/
    │   │               ├── api/
    │   │               └── app/
    │   └── resources/
    └── test/
```

REQ-EST-001 — A prioridade e ter uma aplicacao funcionando rapidamente.

REQ-EST-002 — A separacao em modulos Maven e desejavel para benchmark, mas nao obrigatoria se atrasar o MVP.

REQ-EST-003 — Mesmo em modulo unico, o codigo deve ser organizado por responsabilidade.

## 6. Modulo de dominio

O dominio representa as regras centrais da aplicacao.

### 6.1 Entidade `Tarefa`

REQ-DOM-001 — Deve existir a entidade `Tarefa`.

Campos obrigatorios da entidade `Tarefa`:

```text
id
tarefaPaiId
titulo
descricao
estado
prioridade
criadoEm
atualizadoEm
previstoPara
iniciadoEm
concluidoEm
canceladoEm
```

Descricao dos campos:

```text
id              Identificador unico da tarefa
tarefaPaiId     Identificador da tarefa pai, quando for subtarefa
titulo          Titulo curto da tarefa
descricao       Descricao livre da tarefa
estado          Estado atual da tarefa
prioridade      Prioridade da tarefa
criadoEm        Data e hora de criacao
atualizadoEm    Data e hora da ultima atualizacao
previstoPara    Data prevista opcional
iniciadoEm      Data e hora em que a tarefa foi iniciada
concluidoEm     Data e hora em que a tarefa foi concluida
canceladoEm     Data e hora em que a tarefa foi cancelada
```

REQ-DOM-002 — `titulo` deve ser obrigatorio.

REQ-DOM-003 — `descricao` pode ser vazia.

REQ-DOM-004 — `tarefaPaiId` pode ser nulo.

REQ-DOM-005 — Uma tarefa com `tarefaPaiId` preenchido e uma subtarefa.

REQ-DOM-006 — Uma tarefa pode ter zero, uma ou muitas subtarefas.

REQ-DOM-007 — Uma tarefa nao pode ser subtarefa dela mesma.

## 7. Estados da tarefa

REQ-ESTADO-001 — O enum de estado deve se chamar `EstadoTarefa`.

REQ-ESTADO-002 — Os estados permitidos sao:

```text
A_FAZER
EM_EXECUCAO
CONCLUIDA
CANCELADA
```

Significado dos estados:

```text
A_FAZER       Tarefa criada, mas ainda nao iniciada
EM_EXECUCAO  Tarefa iniciada
CONCLUIDA    Tarefa concluida
CANCELADA    Tarefa cancelada
```

REQ-ESTADO-003 — Uma nova tarefa deve ser criada com estado `A_FAZER`.

REQ-ESTADO-004 — Uma tarefa em estado `A_FAZER` pode passar para `EM_EXECUCAO`.

REQ-ESTADO-005 — Uma tarefa em estado `EM_EXECUCAO` pode passar para `CONCLUIDA`.

REQ-ESTADO-006 — Uma tarefa em estado `A_FAZER` pode passar para `CANCELADA`.

REQ-ESTADO-007 — Uma tarefa em estado `EM_EXECUCAO` pode passar para `CANCELADA`.

REQ-ESTADO-008 — Uma tarefa em estado `CONCLUIDA` deve ser considerada finalizada.

REQ-ESTADO-009 — Uma tarefa em estado `CANCELADA` deve ser considerada finalizada.

REQ-ESTADO-010 — No MVP, uma tarefa finalizada nao precisa voltar para estados anteriores.

## 8. Prioridade da tarefa

REQ-PRIO-001 — O enum de prioridade deve se chamar `PrioridadeTarefa`.

REQ-PRIO-002 — As prioridades permitidas sao:

```text
BAIXA
MEDIA
ALTA
URGENTE
```

REQ-PRIO-003 — A prioridade padrao deve ser `MEDIA`.

## 9. Dependencias internas

Uma dependencia interna representa uma relacao entre duas tarefas da propria aplicacao.

Exemplo:

```text
A tarefa "Publicar relatorio" depende da tarefa "Revisar relatorio".
```

REQ-DEPI-001 — Deve existir a entidade `DependenciaTarefa`.

Campos da entidade `DependenciaTarefa`:

```text
id
tarefaId
dependeDeTarefaId
criadoEm
```

Descricao dos campos:

```text
id                  Identificador unico da dependencia
tarefaId            Tarefa que possui a dependencia
dependeDeTarefaId   Tarefa da qual a primeira depende
criadoEm            Data e hora de criacao da dependencia
```

REQ-DEPI-002 — Uma tarefa pode depender de zero, uma ou muitas tarefas.

REQ-DEPI-003 — Uma tarefa nao pode depender dela mesma.

REQ-DEPI-004 — A aplicacao deve evitar duplicidade de dependencia.

REQ-DEPI-005 — Sempre que simples de implementar, a aplicacao deve impedir ciclos de dependencia.

REQ-DEPI-006 — Uma tarefa com dependencias internas nao concluidas nao deve poder ser iniciada.

REQ-DEPI-007 — Para ser considerada desbloqueada, todas as tarefas das quais ela depende devem estar em estado `CONCLUIDA`.

## 10. Dependencias externas

Uma dependencia externa representa algo fora do controle da aplicacao.

Exemplo:

```text
Aguardando aprovacao da gerencia.
Aguardando envio de documento por outra equipe.
```

REQ-DEPE-001 — Deve existir a entidade `DependenciaExterna`.

Campos da entidade `DependenciaExterna`:

```text
id
tarefaId
titulo
descricao
previstoPara
resolvida
criadoEm
atualizadoEm
```

Descricao dos campos:

```text
id             Identificador unico da dependencia externa
tarefaId       Tarefa relacionada
titulo         Titulo curto da dependencia externa
descricao      Descricao livre
previstoPara   Data esperada opcional
resolvida      Indica se a dependencia externa foi resolvida
criadoEm       Data e hora de criacao
atualizadoEm   Data e hora da ultima atualizacao
```

REQ-DEPE-002 — Uma tarefa pode ter zero, uma ou muitas dependencias externas.

REQ-DEPE-003 — Dependencias externas nao precisam bloquear automaticamente o inicio da tarefa no MVP.

REQ-DEPE-004 — A interface deve exibir dependencias externas de forma clara.

REQ-DEPE-005 — Deve ser possivel marcar uma dependencia externa como resolvida.

## 11. Notas da tarefa

REQ-NOTA-001 — Deve existir a entidade opcional `NotaTarefa`.

Campos da entidade `NotaTarefa`:

```text
id
tarefaId
texto
criadoEm
```

Descricao dos campos:

```text
id        Identificador unico da nota
tarefaId  Tarefa relacionada
texto     Texto livre da nota
criadoEm  Data e hora de criacao da nota
```

REQ-NOTA-002 — A nota e opcional no MVP, mas recomendada para tornar a aplicacao mais concreta.

REQ-NOTA-003 — Uma tarefa pode ter zero, uma ou muitas notas.

## 12. Modulo de aplicacao

O modulo de aplicacao contem os casos de uso e servicos.

### 12.1 Servico principal

REQ-APP-001 — Deve existir um servico chamado `ServicoTarefa`.

Metodos recomendados do `ServicoTarefa`:

```text
criarTarefa
atualizarTarefa
excluirTarefa
buscarTarefaPorId
listarTarefas
listarArvoreTarefas
criarSubtarefa
listarSubtarefas
iniciarTarefa
concluirTarefa
cancelarTarefa
pesquisarTarefas
listarDependencias
adicionarDependenciaInterna
removerDependenciaInterna
adicionarDependenciaExterna
atualizarDependenciaExterna
removerDependenciaExterna
marcarDependenciaExternaResolvida
listarBloqueios
adicionarNota
listarNotas
```

### 12.2 Casos de uso obrigatorios

REQ-APP-002 — Criar tarefa.

REQ-APP-003 — Atualizar tarefa.

REQ-APP-004 — Excluir tarefa.

REQ-APP-005 — Buscar tarefa por ID.

REQ-APP-006 — Listar tarefas.

REQ-APP-007 — Listar tarefas em formato de arvore.

REQ-APP-008 — Criar subtarefa.

REQ-APP-009 — Listar subtarefas de uma tarefa.

REQ-APP-010 — Iniciar tarefa.

REQ-APP-011 — Concluir tarefa.

REQ-APP-012 — Cancelar tarefa.

REQ-APP-013 — Pesquisar tarefas por texto.

REQ-APP-014 — Adicionar dependencia interna.

REQ-APP-015 — Remover dependencia interna.

REQ-APP-016 — Adicionar dependencia externa.

REQ-APP-017 — Atualizar dependencia externa.

REQ-APP-018 — Remover dependencia externa.

REQ-APP-019 — Marcar dependencia externa como resolvida.

REQ-APP-020 — Listar bloqueios de uma tarefa.

REQ-APP-021 — Adicionar nota em tarefa.

REQ-APP-022 — Listar notas de uma tarefa.

### 12.3 Regras de aplicacao

REQ-APP-023 — Ao criar tarefa, preencher automaticamente `criadoEm` e `atualizadoEm`.

REQ-APP-024 — Ao atualizar tarefa, atualizar automaticamente `atualizadoEm`.

REQ-APP-025 — Ao iniciar tarefa, preencher `iniciadoEm`.

REQ-APP-026 — Ao concluir tarefa, preencher `concluidoEm`.

REQ-APP-027 — Ao cancelar tarefa, preencher `canceladoEm`.

REQ-APP-028 — Ao iniciar tarefa, validar se existem dependencias internas nao concluidas.

REQ-APP-029 — Quando houver erro de regra de negocio, retornar mensagem compreensivel para a interface.

## 13. Modulo de persistencia

O modulo de persistencia usa SQLite.

### 13.1 Local do banco

REQ-DB-001 — O banco deve ser um arquivo SQLite.

REQ-DB-002 — O arquivo do banco deve ficar dentro da pasta do usuario Windows.

Caminho recomendado:

```text
%USERPROFILE%\.tarefas-benchmark\tarefas-benchmark.db
```

REQ-DB-003 — A aplicacao deve criar o diretorio `.tarefas-benchmark` caso ele nao exista.

REQ-DB-004 — A aplicacao deve criar o banco caso ele nao exista.

REQ-DB-005 — A aplicacao pode criar o schema automaticamente via JPA/Hibernate ou script SQL simples.

REQ-DB-006 — Nao usar Flyway ou Liquibase no MVP, salvo se a ferramenta de IA considerar mais facil sem prejudicar prazo.

### 13.2 Tabela `tarefas`

Tabela:

```text
tarefas
```

Campos:

```text
id
tarefa_pai_id
titulo
descricao
estado
prioridade
criado_em
atualizado_em
previsto_para
iniciado_em
concluido_em
cancelado_em
```

REQ-DB-007 — `id` deve ser chave primaria.

REQ-DB-008 — `tarefa_pai_id` deve referenciar `tarefas.id`.

REQ-DB-009 — `titulo` deve ser obrigatorio.

REQ-DB-010 — `estado` deve armazenar os valores de `EstadoTarefa`.

REQ-DB-011 — `prioridade` deve armazenar os valores de `PrioridadeTarefa`.

### 13.3 Tabela `dependencias_tarefa`

Tabela:

```text
dependencias_tarefa
```

Campos:

```text
id
tarefa_id
depende_de_tarefa_id
criado_em
```

REQ-DB-012 — `id` deve ser chave primaria.

REQ-DB-013 — `tarefa_id` deve referenciar `tarefas.id`.

REQ-DB-014 — `depende_de_tarefa_id` deve referenciar `tarefas.id`.

REQ-DB-015 — Deve haver restricao ou validacao para evitar duplicidade de `tarefa_id` com `depende_de_tarefa_id`.

REQ-DB-016 — Deve haver restricao ou validacao para impedir que `tarefa_id` seja igual a `depende_de_tarefa_id`.

### 13.4 Tabela `dependencias_externas`

Tabela:

```text
dependencias_externas
```

Campos:

```text
id
tarefa_id
titulo
descricao
previsto_para
resolvida
criado_em
atualizado_em
```

REQ-DB-017 — `id` deve ser chave primaria.

REQ-DB-018 — `tarefa_id` deve referenciar `tarefas.id`.

REQ-DB-019 — `titulo` deve ser obrigatorio.

REQ-DB-020 — `resolvida` deve ser booleano ou equivalente.

### 13.5 Tabela `notas_tarefa`

Tabela:

```text
notas_tarefa
```

Campos:

```text
id
tarefa_id
texto
criado_em
```

REQ-DB-021 — `id` deve ser chave primaria.

REQ-DB-022 — `tarefa_id` deve referenciar `tarefas.id`.

REQ-DB-023 — `texto` deve ser obrigatorio.

### 13.6 Indices recomendados

REQ-DB-024 — Criar indice para `tarefas.tarefa_pai_id`.

REQ-DB-025 — Criar indice para `tarefas.estado`.

REQ-DB-026 — Criar indice para `tarefas.titulo`.

REQ-DB-027 — Criar indice para `dependencias_tarefa.tarefa_id`.

REQ-DB-028 — Criar indice para `dependencias_tarefa.depende_de_tarefa_id`.

REQ-DB-029 — Criar indice para `dependencias_externas.tarefa_id`.

REQ-DB-030 — Criar indice para `notas_tarefa.tarefa_id`.

## 14. Modulo REST API

A API REST deve usar JSON e prefixo `/api`.

### 14.1 Convencoes da API

REQ-API-001 — Todos os endpoints da aplicacao devem usar portugues sem acentos.

REQ-API-002 — O recurso principal deve ser chamado `tarefas`.

REQ-API-003 — A URL base deve ser:

```text
/api
```

REQ-API-004 — A API deve retornar JSON.

REQ-API-005 — A API nao tera autenticacao.

REQ-API-006 — A API deve retornar HTTP 404 para recurso inexistente.

REQ-API-007 — A API deve retornar HTTP 400 para erro de validacao ou regra de negocio.

REQ-API-008 — A API deve retornar HTTP 200 ou 201 para sucesso, conforme o caso.

### 14.2 DTOs da API

#### `CriarTarefaRequisicao`

Campos:

```text
titulo
descricao
prioridade
previstoPara
```

#### `AtualizarTarefaRequisicao`

Campos:

```text
titulo
descricao
prioridade
previstoPara
```

#### `TarefaResumoResposta`

Campos:

```text
id
tarefaPaiId
titulo
estado
prioridade
previstoPara
temSubtarefas
temDependencias
bloqueada
```

#### `TarefaDetalheResposta`

Campos:

```text
id
tarefaPaiId
titulo
descricao
estado
prioridade
criadoEm
atualizadoEm
previstoPara
iniciadoEm
concluidoEm
canceladoEm
subtarefas
dependenciasInternas
dependenciasExternas
notas
bloqueios
```

#### `CriarDependenciaInternaRequisicao`

Campos:

```text
dependeDeTarefaId
```

#### `CriarDependenciaExternaRequisicao`

Campos:

```text
titulo
descricao
previstoPara
```

#### `AtualizarDependenciaExternaRequisicao`

Campos:

```text
titulo
descricao
previstoPara
resolvida
```

#### `DependenciaInternaResposta`

Campos:

```text
id
tarefaId
dependeDeTarefaId
tituloTarefaDependencia
estadoTarefaDependencia
criadoEm
```

#### `DependenciaExternaResposta`

Campos:

```text
id
tarefaId
titulo
descricao
previstoPara
resolvida
criadoEm
atualizadoEm
```

#### `BloqueioResposta`

Campos:

```text
tipo
mensagem
tarefaBloqueadoraId
tituloTarefaBloqueadora
```

Valores recomendados para `tipo`:

```text
DEPENDENCIA_INTERNA
DEPENDENCIA_EXTERNA
REGRA_ESTADO
```

#### `CriarNotaRequisicao`

Campos:

```text
texto
```

#### `NotaTarefaResposta`

Campos:

```text
id
tarefaId
texto
criadoEm
```

### 14.3 Endpoints de tarefas

Criar tarefa:

```text
POST /api/tarefas
```

Funcao correspondente:

```text
criarTarefa
```

Listar tarefas:

```text
GET /api/tarefas
```

Funcao correspondente:

```text
listarTarefas
```

Listar tarefas em arvore:

```text
GET /api/tarefas/arvore
```

Funcao correspondente:

```text
listarArvoreTarefas
```

Buscar tarefa por ID:

```text
GET /api/tarefas/{id}
```

Funcao correspondente:

```text
buscarTarefaPorId
```

Atualizar tarefa:

```text
PUT /api/tarefas/{id}
```

Funcao correspondente:

```text
atualizarTarefa
```

Excluir tarefa:

```text
DELETE /api/tarefas/{id}
```

Funcao correspondente:

```text
excluirTarefa
```

Pesquisar tarefas:

```text
GET /api/tarefas/pesquisa?texto=valor
```

Funcao correspondente:

```text
pesquisarTarefas
```

### 14.4 Endpoints de subtarefas

Criar subtarefa:

```text
POST /api/tarefas/{id}/subtarefas
```

Funcao correspondente:

```text
criarSubtarefa
```

Listar subtarefas:

```text
GET /api/tarefas/{id}/subtarefas
```

Funcao correspondente:

```text
listarSubtarefas
```

### 14.5 Endpoints de estado

Iniciar tarefa:

```text
POST /api/tarefas/{id}/iniciar
```

Funcao correspondente:

```text
iniciarTarefa
```

Concluir tarefa:

```text
POST /api/tarefas/{id}/concluir
```

Funcao correspondente:

```text
concluirTarefa
```

Cancelar tarefa:

```text
POST /api/tarefas/{id}/cancelar
```

Funcao correspondente:

```text
cancelarTarefa
```

### 14.6 Endpoints de dependencias internas

Listar dependencias da tarefa:

```text
GET /api/tarefas/{id}/dependencias
```

Funcao correspondente:

```text
listarDependencias
```

Adicionar dependencia interna:

```text
POST /api/tarefas/{id}/dependencias/internas
```

Funcao correspondente:

```text
adicionarDependenciaInterna
```

Remover dependencia interna:

```text
DELETE /api/tarefas/{id}/dependencias/internas/{dependenciaId}
```

Funcao correspondente:

```text
removerDependenciaInterna
```

### 14.7 Endpoints de dependencias externas

Adicionar dependencia externa:

```text
POST /api/tarefas/{id}/dependencias/externas
```

Funcao correspondente:

```text
adicionarDependenciaExterna
```

Atualizar dependencia externa:

```text
PUT /api/tarefas/{id}/dependencias/externas/{dependenciaId}
```

Funcao correspondente:

```text
atualizarDependenciaExterna
```

Remover dependencia externa:

```text
DELETE /api/tarefas/{id}/dependencias/externas/{dependenciaId}
```

Funcao correspondente:

```text
removerDependenciaExterna
```

Marcar dependencia externa como resolvida:

```text
POST /api/tarefas/{id}/dependencias/externas/{dependenciaId}/resolver
```

Funcao correspondente:

```text
marcarDependenciaExternaResolvida
```

### 14.8 Endpoint de bloqueios

Listar bloqueios que impedem inicio da tarefa:

```text
GET /api/tarefas/{id}/bloqueios
```

Funcao correspondente:

```text
listarBloqueios
```

### 14.9 Endpoints de notas

Adicionar nota:

```text
POST /api/tarefas/{id}/notas
```

Funcao correspondente:

```text
adicionarNota
```

Listar notas:

```text
GET /api/tarefas/{id}/notas
```

Funcao correspondente:

```text
listarNotas
```

## 15. Modulo de interface Angular

A interface Angular deve ser funcional, simples e completa o bastante para demonstrar a aplicacao.

A ferramenta de IA codificadora tem liberdade para escolher o layout, componentes e organizacao visual, desde que os requisitos funcionais sejam atendidos.

### 15.1 Requisitos gerais da interface

REQ-UI-001 — A interface deve ser carregada pelo navegador padrao.

REQ-UI-002 — A interface deve ser servida pelo Spring Boot no produto final.

REQ-UI-003 — A interface deve consumir a API REST local.

REQ-UI-004 — A tela inicial deve apresentar a lista de tarefas.

REQ-UI-005 — A lista deve permitir visualizar tarefas e subtarefas.

REQ-UI-006 — A visualizacao de subtarefas deve ser expansivel.

REQ-UI-007 — A interface deve permitir criar tarefa.

REQ-UI-008 — A interface deve permitir editar tarefa.

REQ-UI-009 — A interface deve permitir excluir tarefa.

REQ-UI-010 — A interface deve permitir criar subtarefa.

REQ-UI-011 — A interface deve permitir iniciar tarefa.

REQ-UI-012 — A interface deve permitir concluir tarefa.

REQ-UI-013 — A interface deve permitir cancelar tarefa.

REQ-UI-014 — A interface deve exibir detalhes da tarefa selecionada.

REQ-UI-015 — A interface deve exibir dependencias internas.

REQ-UI-016 — A interface deve exibir dependencias externas.

REQ-UI-017 — A interface deve exibir notas, se implementadas.

### 15.2 Campos exibidos na lista

Cada tarefa na lista deve exibir, no minimo:

```text
titulo
estado
prioridade
previstoPara
indicador de subtarefas
indicador de dependencias
indicador de bloqueio
```

REQ-UI-018 — A tarefa bloqueada deve ter indicacao visual clara.

REQ-UI-019 — A tarefa concluida deve ter indicacao visual clara.

REQ-UI-020 — A tarefa cancelada deve ter indicacao visual clara.

### 15.3 Interface de dependencias

REQ-UI-021 — A interface deve permitir pesquisar tarefas existentes.

REQ-UI-022 — A pesquisa deve usar o endpoint:

```text
GET /api/tarefas/pesquisa?texto=valor
```

REQ-UI-023 — O usuario deve conseguir selecionar uma tarefa pesquisada como dependencia interna.

REQ-UI-024 — A tarefa atual nao deve aparecer como dependencia valida dela mesma.

REQ-UI-025 — Dependencias internas ja cadastradas devem ser exibidas.

REQ-UI-026 — Deve ser possivel remover dependencias internas.

REQ-UI-027 — Deve ser possivel cadastrar dependencia externa.

REQ-UI-028 — Deve ser possivel editar dependencia externa.

REQ-UI-029 — Deve ser possivel marcar dependencia externa como resolvida.

REQ-UI-030 — Deve ser possivel remover dependencia externa.

### 15.4 Liberdade visual

REQ-UI-031 — A IA codificadora pode usar componentes simples.

REQ-UI-032 — A IA codificadora pode usar biblioteca visual Angular, se isso acelerar o desenvolvimento.

REQ-UI-033 — A interface nao precisa ser sofisticada.

REQ-UI-034 — A interface deve priorizar clareza e funcionamento.

REQ-UI-035 — A interface deve funcionar bem em navegador desktop.

## 16. Modulo de empacotamento e inicializacao

REQ-JAR-001 — O produto final deve ser um JAR executavel.

REQ-JAR-002 — O JAR deve conter o backend Spring Boot.

REQ-JAR-003 — O JAR deve conter as dependencias Java necessarias.

REQ-JAR-004 — O JAR deve conter os arquivos estaticos compilados do Angular.

REQ-JAR-005 — O comando de execucao deve ser:

```text
java -jar tarefas-benchmark.jar
```

REQ-JAR-006 — Ao iniciar, a aplicacao deve criar o diretorio local de dados, se necessario.

REQ-JAR-007 — Ao iniciar, a aplicacao deve abrir ou criar o banco SQLite.

REQ-JAR-008 — Ao iniciar, a aplicacao deve subir a API REST.

REQ-JAR-009 — Ao iniciar, a aplicacao deve servir a interface Angular.

REQ-JAR-010 — Ao iniciar, a aplicacao deve abrir o navegador padrao.

REQ-JAR-011 — A abertura do navegador deve poder ser desligada.

Propriedade sugerida:

```text
app.abrirNavegador
```

Valor padrao:

```text
true
```

Exemplo de execucao sem abrir navegador:

```text
java -jar tarefas-benchmark.jar --app.abrirNavegador=false
```

## 17. Build Maven e Angular

REQ-BUILD-001 — O projeto deve compilar com Maven.

REQ-BUILD-002 — O comando principal de build deve ser:

```text
mvn clean package
```

REQ-BUILD-003 — O build Maven deve gerar o JAR executavel.

REQ-BUILD-004 — O build Maven deve incluir ou acionar o build Angular.

REQ-BUILD-005 — Se a integracao Maven-Angular atrasar o MVP, a ferramenta de IA pode documentar um comando auxiliar temporario.

Comando auxiliar aceitavel durante desenvolvimento:

```text
npm install
npm run build
mvn clean package
```

REQ-BUILD-006 — O objetivo final continua sendo build integrado por Maven.

REQ-BUILD-007 — O projeto deve permitir medir tempo de build de forma repetivel.

REQ-BUILD-008 — O projeto nao deve depender de servicos externos em tempo de execucao.

REQ-BUILD-009 — O projeto pode depender de Maven, JDK, Node e npm em tempo de build.

## 18. Testes

REQ-TEST-001 — Deve haver testes unitarios para regras de estado da tarefa.

REQ-TEST-002 — Deve haver testes unitarios para regras de dependencia interna.

REQ-TEST-003 — Deve haver testes da API para criacao de tarefa.

REQ-TEST-004 — Deve haver testes da API para listagem de tarefas.

REQ-TEST-005 — Deve haver testes da API para iniciar, concluir e cancelar tarefa.

REQ-TEST-006 — Deve haver teste simples de persistencia.

REQ-TEST-007 — Nao e obrigatorio teste automatizado completo da interface Angular no MVP.

REQ-TEST-008 — O build Angular deve ser validado pelo processo de build.

Comando esperado para testes Java:

```text
mvn test
```

## 19. Funcionalidades fora do escopo

Fora do escopo do MVP:

```text
autenticacao
autorizacao
multiusuario
criptografia
sincronizacao em nuvem
instalador nativo
Electron
JavaFX
Tauri
migracao formal de banco
recorrencia avancada
relatorios gerenciais
auditoria
backup
internacionalizacao
notificacoes do sistema operacional
integracao com calendario
controle sofisticado de permissoes
tema visual sofisticado
```

## 20. Criterios de aceite

ACE-001 — O projeto deve ser compreensivel para uma ferramenta de IA codificadora.

ACE-002 — O projeto deve usar Java, Maven, Spring Boot, Angular e SQLite.

ACE-003 — O comando `mvn clean package` deve produzir um JAR executavel, ainda que seja aceitavel um comando auxiliar temporario para o build Angular durante o desenvolvimento.

ACE-004 — O comando `java -jar tarefas-benchmark.jar` deve iniciar a aplicacao.

ACE-005 — A aplicacao deve abrir o navegador padrao automaticamente.

ACE-006 — A interface Angular deve carregar a partir do Spring Boot.

ACE-007 — O banco SQLite deve ser criado dentro da pasta de usuario do Windows.

ACE-008 — O usuario deve conseguir criar uma tarefa pela interface.

ACE-009 — O usuario deve conseguir listar tarefas pela interface.

ACE-010 — O usuario deve conseguir visualizar subtarefas de forma expansivel.

ACE-011 — O usuario deve conseguir criar subtarefas.

ACE-012 — O usuario deve conseguir iniciar tarefa.

ACE-013 — O usuario deve conseguir concluir tarefa.

ACE-014 — O usuario deve conseguir cancelar tarefa.

ACE-015 — O usuario deve conseguir pesquisar tarefas para definir dependencias internas.

ACE-016 — O usuario deve conseguir adicionar dependencia interna.

ACE-017 — O usuario deve conseguir remover dependencia interna.

ACE-018 — O usuario deve conseguir adicionar dependencia externa.

ACE-019 — O usuario deve conseguir marcar dependencia externa como resolvida.

ACE-020 — O banco SQLite deve persistir dados entre execucoes.

ACE-021 — O projeto deve conter testes automatizados minimos.

ACE-022 — A aplicacao deve demonstrar claramente a arquitetura Angular + Spring Boot REST + SQLite + JAR executavel.

## 21. Orientacao para a IA codificadora

A implementacao deve priorizar funcionamento, clareza e rapidez.

A IA codificadora deve seguir esta ordem sugerida:

```text
1. Criar estrutura Maven
2. Criar dominio em portugues sem acentos
3. Criar entidades Tarefa, DependenciaTarefa, DependenciaExterna e NotaTarefa
4. Criar enums EstadoTarefa e PrioridadeTarefa
5. Criar persistencia SQLite
6. Criar ServicoTarefa
7. Criar REST API com endpoints em portugues sem acentos
8. Criar interface Angular funcional
9. Integrar build Angular ao Spring Boot
10. Gerar JAR executavel
11. Implementar abertura automatica do navegador
12. Adicionar testes minimos
13. Validar mvn clean package
14. Validar java -jar tarefas-benchmark.jar
```

O resultado aceitavel e uma aplicacao simples, local, com eventuais limitacoes, mas que compile, rode, abra no navegador, persista dados e demonstre uma arquitetura realista para benchmark de desenvolvimento e build.
