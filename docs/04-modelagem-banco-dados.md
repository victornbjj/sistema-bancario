# Modelagem do Banco de Dados

> Continuação da Modelagem de Domínio (documento 03). Estratégia de herança JPA adotada: **JOINED** (`@Inheritance(strategy = InheritanceType.JOINED)`), com uma tabela base `conta` e tabelas filhas `conta_corrente` e `conta_poupanca` ligadas por FK/PK compartilhada. Migrations gerenciadas via **Flyway** em `src/main/resources/db/migration`.

## 1. Diagrama Entidade-Relacionamento (visão textual)

```mermaid
erDiagram
    CORRENTISTA ||--o{ CONTA : possui
    CONTA ||--o{ TRANSACAO : origina
    CONTA ||--o| CONTA_CORRENTE : especializa
    CONTA ||--o| CONTA_POUPANCA : especializa

    CORRENTISTA {
        bigint id PK
        varchar nome
        varchar documento UK
        varchar email
        varchar telefone
        datetime data_cadastro
    }

    CONTA {
        bigint id PK
        varchar numero UK
        decimal saldo
        varchar tipo
        datetime data_abertura
        bigint correntista_id FK
    }

    CONTA_CORRENTE {
        bigint id PK_FK
        decimal limite
    }

    CONTA_POUPANCA {
        bigint id PK_FK
    }

    TRANSACAO {
        bigint id PK
        varchar tipo
        decimal valor
        datetime data
        bigint conta_id FK
    }

```

## 2. Definição das Tabelas

### `correntista`
| Coluna | Tipo | Restrições |
|--------|------|------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| nome | VARCHAR(150) | NOT NULL |
| documento | VARCHAR(20) | NOT NULL, UNIQUE |
| email | VARCHAR(150) | NULL |
| telefone | VARCHAR(20) | NULL |
| data_cadastro | DATETIME | NOT NULL, default CURRENT_TIMESTAMP |

### `conta` (tabela base — atributos comuns)
| Coluna | Tipo | Restrições |
|--------|------|------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| numero | VARCHAR(20) | NOT NULL, UNIQUE |
| saldo | DECIMAL(15,2) | NOT NULL, default 0 |
| tipo | VARCHAR(20) | NOT NULL (discriminador auxiliar/consulta) |
| data_abertura | DATETIME | NOT NULL, default CURRENT_TIMESTAMP |
| correntista_id | BIGINT | NOT NULL, FK → correntista(id) |

### `conta_corrente` (filha — JOINED)
| Coluna | Tipo | Restrições |
|--------|------|------------|
| id | BIGINT | PK, FK → conta(id) |
| limite | DECIMAL(15,2) | NOT NULL, default 0 |

### `conta_poupanca` (filha — JOINED)
| Coluna | Tipo | Restrições |
|--------|------|------------|
| id | BIGINT | PK, FK → conta(id) |

### `transacao`
| Coluna | Tipo | Restrições |
|--------|------|------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| tipo | VARCHAR(20) | NOT NULL (`DEPOSITO`, `SAQUE`, `RENDIMENTO`, `JUROS`) |
| valor | DECIMAL(15,2) | NOT NULL |
| data | DATETIME | NOT NULL, default CURRENT_TIMESTAMP |
| conta_id | BIGINT | NOT NULL, FK → conta(id) |

## 3. Script de Migration (Flyway) — `V1__create_schema.sql`

```sql
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

```

## 4. Observações

- `numero` da conta pode ser gerado pela aplicação (ex.: sequencial + dígito verificador) no Service, não no banco.
- Índices únicos em `correntista.documento` e `conta.numero` já garantem RN09/RN10 a nível de banco.
- Caso opte por **SINGLE_TABLE** em vez de JOINED (uma única tabela `conta` com coluna `limite` nula para poupança), simplifica-se a query mas perde-se a garantia de "limite não existe para poupança" a nível de schema — trade-off a citar no README.
- Para H2 (testes), o mesmo script Flyway roda sem alteração, bastando trocar o dialect no `application-test.yml`.
