CREATE TABLE correntista (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    documento VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(150),
    telefone VARCHAR(20),
    data_cadastro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE conta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(20) NOT NULL UNIQUE,
    saldo DECIMAL(15,2) NOT NULL DEFAULT 0,
    tipo VARCHAR(20) NOT NULL,
    data_abertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correntista_id BIGINT NOT NULL,
    CONSTRAINT fk_conta_correntista FOREIGN KEY (correntista_id) REFERENCES correntista(id)
);

CREATE TABLE conta_corrente (
    id BIGINT PRIMARY KEY,
    limite DECIMAL(15,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_conta_corrente_conta FOREIGN KEY (id) REFERENCES conta(id)
);

CREATE TABLE conta_poupanca (
    id BIGINT PRIMARY KEY,
    CONSTRAINT fk_conta_poupanca_conta FOREIGN KEY (id) REFERENCES conta(id)
);

CREATE TABLE transacao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL,
    valor DECIMAL(15,2) NOT NULL,
    data DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    conta_id BIGINT NOT NULL,
    CONSTRAINT fk_transacao_conta FOREIGN KEY (conta_id) REFERENCES conta(id)
);

CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);
