-- =====================================================================
-- V1 - Schema inicial do SiGOH
--
-- Estrutura fisica do hospital: bloco -> enfermaria -> leito.
-- O historico de mudancas de status e uma tabela append-only (nunca
-- recebe UPDATE nem DELETE), servindo como trilha de auditoria.
-- =====================================================================

CREATE TABLE usuarios (
    id            BIGSERIAL PRIMARY KEY,
    nome          VARCHAR(120) NOT NULL,
    email         VARCHAR(180) NOT NULL UNIQUE,
    senha_hash    VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_usuarios_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE blocos (
    id      BIGSERIAL PRIMARY KEY,
    nome    VARCHAR(120) NOT NULL,
    ativo   BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE enfermarias (
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(120) NOT NULL,
    bloco_id   BIGINT       NOT NULL REFERENCES blocos (id),
    ativo      BOOLEAN      NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_enfermarias_bloco ON enfermarias (bloco_id);

CREATE TABLE leitos (
    id                BIGSERIAL PRIMARY KEY,
    codigo            VARCHAR(30)  NOT NULL,
    enfermaria_id     BIGINT       NOT NULL REFERENCES enfermarias (id),
    status            VARCHAR(20)  NOT NULL,
    possui_oxigenio   BOOLEAN      NOT NULL DEFAULT FALSE,
    isolamento        BOOLEAN      NOT NULL DEFAULT FALSE,
    ativo             BOOLEAN      NOT NULL DEFAULT TRUE,
    atualizado_em     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    atualizado_por    BIGINT       REFERENCES usuarios (id),
    CONSTRAINT ck_leitos_status CHECK (status IN ('DISPONIVEL', 'OCUPADO', 'LIMPEZA', 'BLOQUEADO')),
    CONSTRAINT uq_leitos_codigo_enfermaria UNIQUE (enfermaria_id, codigo)
);
CREATE INDEX idx_leitos_enfermaria ON leitos (enfermaria_id);
CREATE INDEX idx_leitos_status     ON leitos (status);

-- Trilha de auditoria: append-only por decisao de projeto.
-- Nenhuma rotina da aplicacao atualiza ou remove linhas desta tabela.
CREATE TABLE historico_leitos (
    id               BIGSERIAL PRIMARY KEY,
    leito_id         BIGINT      NOT NULL REFERENCES leitos (id),
    status_anterior  VARCHAR(20) NOT NULL,
    status_novo      VARCHAR(20) NOT NULL,
    usuario_id       BIGINT      NOT NULL REFERENCES usuarios (id),
    registrado_em    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_historico_leito ON historico_leitos (leito_id, registrado_em DESC);

-- Fotografia diaria da ocupacao por bloco, alimentada por rotina agendada.
-- Existe para que relatorios historicos nao dependam de varrer todo o
-- historico de eventos a cada consulta.
CREATE TABLE snapshots_ocupacao (
    id            BIGSERIAL PRIMARY KEY,
    data_registro DATE    NOT NULL,
    bloco_id      BIGINT  NOT NULL REFERENCES blocos (id),
    total_leitos  INTEGER NOT NULL,
    ocupados      INTEGER NOT NULL,
    disponiveis   INTEGER NOT NULL,
    em_limpeza    INTEGER NOT NULL,
    bloqueados    INTEGER NOT NULL,
    CONSTRAINT uq_snapshot_data_bloco UNIQUE (data_registro, bloco_id)
);
