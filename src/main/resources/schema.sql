CREATE TABLE IF NOT EXISTS tarefas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tarefa_pai_id INTEGER REFERENCES tarefas (id),
    titulo TEXT NOT NULL,
    descricao TEXT,
    estado TEXT NOT NULL,
    prioridade TEXT NOT NULL,
    criado_em TEXT NOT NULL,
    atualizado_em TEXT NOT NULL,
    previsto_para TEXT,
    iniciado_em TEXT,
    concluido_em TEXT,
    cancelado_em TEXT
);

CREATE TABLE IF NOT EXISTS dependencias_tarefa (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tarefa_id INTEGER NOT NULL REFERENCES tarefas (id),
    depende_de_tarefa_id INTEGER NOT NULL REFERENCES tarefas (id),
    criado_em TEXT NOT NULL,
    CONSTRAINT uq_dependencia_tarefa UNIQUE (tarefa_id, depende_de_tarefa_id),
    CONSTRAINT ck_dependencia_distinta CHECK (tarefa_id <> depende_de_tarefa_id)
);

CREATE TABLE IF NOT EXISTS dependencias_externas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tarefa_id INTEGER NOT NULL REFERENCES tarefas (id),
    titulo TEXT NOT NULL,
    descricao TEXT,
    previsto_para TEXT,
    resolvida INTEGER NOT NULL DEFAULT 0,
    criado_em TEXT NOT NULL,
    atualizado_em TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS notas_tarefa (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tarefa_id INTEGER NOT NULL REFERENCES tarefas (id),
    texto TEXT NOT NULL,
    criado_em TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tarefas_tarefa_pai_id ON tarefas (tarefa_pai_id);
CREATE INDEX IF NOT EXISTS idx_tarefas_estado ON tarefas (estado);
CREATE INDEX IF NOT EXISTS idx_tarefas_titulo ON tarefas (titulo);
CREATE INDEX IF NOT EXISTS idx_dependencias_tarefa_tarefa_id ON dependencias_tarefa (tarefa_id);
CREATE INDEX IF NOT EXISTS idx_dependencias_tarefa_depende_de ON dependencias_tarefa (depende_de_tarefa_id);
CREATE INDEX IF NOT EXISTS idx_dependencias_externas_tarefa_id ON dependencias_externas (tarefa_id);
CREATE INDEX IF NOT EXISTS idx_notas_tarefa_tarefa_id ON notas_tarefa (tarefa_id);
