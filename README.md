# Sistema Bancário — API de Conta Bancária (Cooperativa de Crédito)

API REST para gerenciamento de correntistas, contas (corrente e poupança) e
movimentações financeiras (depósito, saque, rendimento e juros) de uma
cooperativa de crédito, desenvolvida em Java 8 / Spring Boot 2.7.

Documentação de requisitos, casos de uso, modelagem e roadmap completos estão
em [`docs/`](./docs). Este README cobre como rodar o projeto, os endpoints
disponíveis e como testar.

## Sumário

- [Stack e principais dependências](#stack-e-principais-dependências)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Como rodar localmente](#como-rodar-localmente)
- [Como rodar com Docker](#como-rodar-com-docker)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Migrations (Flyway)](#migrations-flyway)
- [Endpoints da API](#endpoints-da-api)
- [Regras de negócio](#regras-de-negócio)
- [Formato padrão de erro](#formato-padrão-de-erro)
- [Testes](#testes)
- [Limitações conhecidas](#limitações-conhecidas)

## Stack e principais dependências

- Java 8
- Spring Boot 2.7.18 (Web, Data JPA, Validation, Actuator)
- MySQL 8 (perfil `dev`) / H2 em memória (perfil `test`)
- Flyway (versionamento de schema)
- springdoc-openapi-ui 1.7.0 (Swagger)
- Lombok
- JUnit 5 + Mockito + AssertJ (via `spring-boot-starter-test`)
- Docker / Docker Compose

## Estrutura do projeto

```
src/main/java/br/com/sistemabancario/api
├── controller       → CorrentistaController, ContaController
├── database
│   ├── entity        → CorrentistaEntity, ContaEntity, ContaCorrente, ContaPoupanca, TransacaoEntity
│   └── repository    → ICorrentistaRepository, IContaRepository, ITransacaoRepository
├── dto               → Requests/Responses de entrada e saída da API
├── enums             → TipoConta, TipoTransacao
├── exception         → NegocioException e subclasses + GlobalExceptionHandler
└── service           → CorrentistaService, ContaService, TransacaoService

src/main/resources
├── application.properties        → ativa o perfil (dev/test)
├── application-dev.properties    → datasource MySQL
├── application-test.properties   → datasource H2
└── db/migration/V1__create_schema.sql
```

## Como rodar localmente

Pré-requisitos: JDK 8+, Maven (ou use o wrapper `./mvnw`), MySQL 8 rodando
localmente (ou pule esta seção e use Docker, abaixo).

```bash
# 1. Build e testes
./mvnw clean verify

# 2. Subir com o perfil dev (requer MySQL local — ver Variáveis de ambiente)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

No Windows: use `.\mvnw.cmd` no lugar de `./mvnw`.

Para rodar só com H2 em memória (sem precisar de MySQL), suba com o perfil
`test`, que já é o ativo por padrão em `application.properties`:

```bash
./mvnw spring-boot:run
```

Confirme que a aplicação subiu com:
```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

## Como rodar com Docker

```bash
cp .env.exemple .env
# edite .env se quiser trocar usuário/senha do MySQL

docker compose up --build
```

Isso sobe o MySQL (porta `3306`) e a aplicação (porta `8080`), com o perfil
`dev` ativo automaticamente. Para encerrar:

```bash
docker compose down
```

## Variáveis de ambiente

Definidas em `.env` (veja `.env.exemple`):

| Variável | Padrão | Uso |
|---|---|---|
| `MYSQL_DATABASE` | `sistema_bancario` | Nome do banco criado pelo container MySQL |
| `MYSQL_ROOT_PASSWORD` | — (defina no `.env`) | Senha do usuário `root` do MySQL |
| `DATABASE_URL` | `jdbc:mysql://mysql:3306/sistema_bancario` | URL JDBC usada pela aplicação |
| `DATABASE_USERNAME` | `root` | Usuário do banco |
| `DATABASE_PASSWORD` | — (defina no `.env`) | Senha do banco |

## Migrations (Flyway)

O schema é versionado em `src/main/resources/db/migration`. A aplicação sobe
com `spring.jpa.hibernate.ddl-auto=validate` — ou seja, **nenhuma tabela é
criada pelo Hibernate**; o Flyway é a única fonte de verdade do schema, tanto
no perfil `dev` (MySQL) quanto no `test` (H2).

Para rodar as migrations manualmente contra o MySQL local, basta subir a
aplicação uma vez com o perfil `dev` — o Flyway roda automaticamente no
startup (`spring.flyway.enabled=true`).

## Endpoints da API

Base: `/api/v1`. Todas as datas em ISO-8601, valores monetários com 2 casas
decimais.

### Correntistas

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/correntistas` | Cadastrar correntista |
| `GET` | `/correntistas?documento=&page=&size=` | Listar correntistas (paginado, filtro opcional) |
| `GET` | `/correntistas/{id}` | Buscar correntista por id |
| `GET` | `/correntistas/{id}/contas` | Listar contas de um correntista |

```bash
# Cadastrar correntista
curl -X POST http://localhost:8080/api/v1/correntistas \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria Silva","documento":"12345678900","email":"maria@email.com"}'

# Listar contas de um correntista
curl http://localhost:8080/api/v1/correntistas/1/contas
```

### Contas

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/contas` | Abrir conta (`CORRENTE` ou `POUPANCA`) |
| `GET` | `/contas/{id}` | Buscar conta por id |
| `GET` | `/contas?correntistaId=&numero=&page=&size=` | Listar contas (paginado, filtros opcionais) |
| `POST` | `/contas/{id}/depositos` | Realizar depósito |
| `POST` | `/contas/{id}/saques` | Realizar saque |
| `GET` | `/contas/{id}/extrato?tipo=&dataInicial=&dataFinal=&page=&size=` | Consultar extrato |
| `POST` | `/contas/{id}/rendimento` | Aplicar rendimento (conta poupança) |
| `POST` | `/contas/{id}/juros` | Aplicar juros (conta corrente) |

```bash
# Abrir conta corrente com limite de 500
curl -X POST http://localhost:8080/api/v1/contas \
  -H "Content-Type: application/json" \
  -d '{"correntistaId":1,"tipo":"CORRENTE","limite":500.00}'

# Abrir conta poupança (sem limite)
curl -X POST http://localhost:8080/api/v1/contas \
  -H "Content-Type: application/json" \
  -d '{"correntistaId":1,"tipo":"POUPANCA"}'

# Depositar
curl -X POST http://localhost:8080/api/v1/contas/1/depositos \
  -H "Content-Type: application/json" \
  -d '{"valor":100.00}'

# Sacar
curl -X POST http://localhost:8080/api/v1/contas/1/saques \
  -H "Content-Type: application/json" \
  -d '{"valor":30.00}'

# Extrato filtrado por tipo e período
curl "http://localhost:8080/api/v1/contas/1/extrato?tipo=SAQUE&dataInicial=2026-09-01T00:00:00&dataFinal=2026-09-22T23:59:59"

# Aplicar rendimento — taxa via query string
curl -X POST "http://localhost:8080/api/v1/contas/2/rendimento?taxa=0.005"

# Aplicar rendimento — taxa via corpo
curl -X POST http://localhost:8080/api/v1/contas/2/rendimento \
  -H "Content-Type: application/json" \
  -d '{"taxa":0.005}'

# Aplicar juros (conta corrente com saldo negativo)
curl -X POST http://localhost:8080/api/v1/contas/1/juros \
  -H "Content-Type: application/json" \
  -d '{"taxa":0.02}'
```

### Documentação interativa (Swagger)

Com a aplicação rodando:
```
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

## Regras de negócio

- **Saque em conta corrente**: permitido até `saldo + limite`; o saldo pode
  ficar negativo (uso do limite/cheque especial).
- **Saque em conta poupança**: permitido apenas até o saldo disponível.
- **Depósito**: valor deve ser estritamente positivo em ambos os tipos.
- **Rendimento** (`POST /contas/{id}/rendimento`): só em conta poupança, com
  saldo positivo e taxa em `(0, 1]`. Fórmula: `saldo += saldo * taxa`.
- **Juros** (`POST /contas/{id}/juros`): só em conta corrente, com saldo
  negativo e taxa em `(0, 1]`. Fórmula: `saldo -= |saldo| * taxa`.
- **Período mínimo de 30 dias**: rendimento e juros só podem ser reaplicados
  na mesma conta 30 dias após a última aplicação do mesmo tipo.
- **Arredondamento**: todos os cálculos usam `BigDecimal`, escala 2,
  `RoundingMode.HALF_EVEN`.
- **Unicidade**: `documento` do correntista é sempre único; `email` e
  `telefone`, quando informados, também precisam ser únicos. `numero` da
  conta é gerado pela aplicação e é único.

Detalhamento completo em [`docs/01-levantamento-requisitos.md`](./docs/01-levantamento-requisitos.md)
e [`docs/02-casos-de-uso.md`](./docs/02-casos-de-uso.md).

## Formato padrão de erro

Toda resposta de erro segue o mesmo formato, produzido pelo `GlobalExceptionHandler`:

```json
{
  "timestamp": "2026-09-22T14:32:00.123-03:00",
  "status": 422,
  "error": "Saldo insuficente",
  "message": "Saque de R$ 500.00 excede o valor disponível de R$ 300.00",
  "path": "/api/v1/contas/12/saques",
  "fieldErrors": []
}
```

`fieldErrors` só é preenchido quando o erro vem de validação de campo
(`400`, ex.: campo obrigatório ausente), listando `field` e `message` por
campo inválido.

| Status | Quando ocorre |
|---|---|
| `400` | Campo obrigatório ausente/inválido; valor de depósito/saque/limite inválido; taxa fora de `(0,1]`; período de extrato inválido |
| `404` | Correntista ou conta inexistente |
| `409` | Documento, e-mail ou telefone já cadastrados |
| `422` | Saldo/limite insuficiente para saque; tipo de conta incompatível com rendimento/juros; período mínimo de 30 dias não cumprido |

## Testes

```bash
./mvnw test
```

Cobertura atual:
- **Unitários de domínio** (`ContaCorrenteTest`, `ContaPoupancaTest`):
  depósito, saque (com e sem limite), rendimento, juros, validação de taxa e
  garantia de que operações inválidas não alteram o saldo.
- **Contrato via MockMvc** (`ContaControllerTest`, `ContaControllerGetTest`,
  `CorrentistaControllerTest`): status HTTP, `Location` no `201`, formato de
  erro, filtros de listagem e extrato.
- **Contexto** (`ApiApplicationTests`): sobe o contexto Spring com o perfil
  `test` (H2) e confirma que a aplicação inicializa corretamente com o schema
  do Flyway.

**Ainda não implementado:**
- Teste automatizado de concorrência (duas operações simultâneas de
  saque/depósito na mesma conta).
- Teste de integração de ponta a ponta (correntista → conta → depósito →
  saque → extrato) contra um banco real.

## Limitações conhecidas

- Sem bloqueio otimista/pessimista de saldo — risco de *lost update* em
  operações concorrentes na mesma conta (ver documento 04 e 06).
- Sem anotações OpenAPI customizadas — o Swagger gerado é o default da
  introspecção do Spring, sem descrições de negócio.
- Sem chave de idempotência nas operações financeiras — reenviar a mesma
  requisição de depósito/saque duas vezes gera duas transações.
