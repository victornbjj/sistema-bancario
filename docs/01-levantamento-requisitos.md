# Levantamento de Requisitos — API de Conta Bancária (Cooperativa de Crédito)

> Documento atualizado para refletir o que está de fato implementado no código
> (pacote `br.com.sistemabancario.api`), com status por item. Itens marcados
> como **Novo** foram adicionados durante a implementação e não faziam parte
> do levantamento original.

## 1. Visão Geral

Sistema RESTful para gerenciamento de correntistas, contas bancárias (corrente
e poupança) e transações financeiras (depósito, saque, rendimento, juros) de
uma cooperativa de crédito.

## 2. Requisitos Funcionais (RF)

| ID | Descrição | Prioridade | Status |
|----|-----------|------------|--------|
| RF01 | Cadastrar um correntista (nome, documento, dados de contato) | Obrigatório | ✅ Implementado |
| RF02 | Consultar correntista(s) cadastrado(s) — lista paginada e por id | Obrigatório | ✅ Implementado |
| RF03 | Abrir uma conta vinculada a um correntista, do tipo Corrente ou Poupança | Obrigatório | ✅ Implementado |
| RF04 | Consultar conta(s) por id, número ou por correntista | Obrigatório | ✅ Implementado |
| RF05 | Realizar depósito em uma conta, aumentando o saldo e registrando a transação | Obrigatório | ✅ Implementado |
| RF06 | Realizar saque em uma conta, reduzindo o saldo e registrando a transação | Obrigatório | ✅ Implementado |
| RF07 | Consultar o extrato (listagem de transações) de uma conta, com filtros de tipo e período | Obrigatório | ✅ Implementado |
| RF08 | Conta Corrente possui limite de crédito, permitindo saque até `saldo + limite` | Obrigatório | ✅ Implementado |
| RF09 | Conta Poupança não permite saque além do saldo disponível | Obrigatório | ✅ Implementado |
| RF10 | Um correntista pode possuir várias contas | Obrigatório | ✅ Implementado |
| RF11 | Aplicar rendimento mensal em Conta Poupança, com taxa por parâmetro | Diferencial | ✅ Implementado |
| RF12 | Aplicar juros sobre saldo negativo de Conta Corrente, com taxa por parâmetro | Diferencial | ✅ Implementado |
| RF13 | Documentar a API via Swagger/OpenAPI | Diferencial | ⚠️ Dependência incluída (`springdoc-openapi-ui`); anotações de schema/exemplos ainda não escritas nos controllers |
| RF14 *(Novo)* | Listar as contas de um correntista (`GET /correntistas/{id}/contas`) | — | ✅ Implementado |
| RF15 *(Novo)* | Limitar a frequência de aplicação de rendimento/juros a uma vez a cada 30 dias por conta | — | ✅ Implementado |

## 3. Requisitos Não Funcionais (RNF)

| ID | Descrição | Status |
|----|-----------|--------|
| RNF01 | Java 8 com Spring Boot 2.7 | ✅ (`pom.xml`: `java.version=1.8`, `spring-boot-starter-parent:2.7.18`) |
| RNF02 | Persistência via Spring Data JPA/Hibernate | ✅ |
| RNF03 | Banco relacional: MySQL (produção/dev) ou H2 (testes) | ✅ (`application-dev.properties` / `application-test.properties`) |
| RNF04 | Versionamento de schema via Flyway em `resources/db/migration` | ✅ (`V1__create_schema.sql`) |
| RNF05 | Ambiente conteinerizado via Docker/Docker Compose (app + MySQL) | ✅ |
| RNF06 | Convenções REST: verbos HTTP e códigos de status corretos | ⚠️ Parcial — ver observação sobre `criarConta` no documento 05 |
| RNF07 | Tratamento de erros padronizado (`GlobalExceptionHandler`) | ✅ |
| RNF08 | Código organizado em camadas: Controller, Service, Repository, DTO, Entity | ✅ |
| RNF09 | Repositório Git com histórico de commits incrementais | ✅ |
| RNF10 | README com instruções de execução, migration e exemplos de requisição | ✅ (ver `README.md` na raiz) |
| RNF11 | Testes unitários cobrindo saque, depósito e rendimento/juros | ✅ (`ContaCorrenteTest`, `ContaPoupancaTest`, `ContaControllerTest`, `ContaControllerGetTest`, `CorrentistaControllerTest`) |
| RNF12 *(Novo)* | Teste automatizado de concorrência (lost update) em saque/depósito | ❌ Pendente |

## 4. Regras de Negócio (RN)

| ID | Regra | Status |
|----|-------|--------|
| RN01 | Correntista 1:N Contas | ✅ |
| RN02 | Conta é entidade abstrata especializada em ContaCorrente e ContaPoupanca (herança JOINED) | ✅ |
| RN03 | Saque em ContaCorrente: permitido se `valorSaque <= saldo + limite` | ✅ |
| RN04 | Saque em ContaPoupanca: permitido se `valorSaque <= saldo` | ✅ |
| RN05 | Todo depósito e saque gera um registro de Transação vinculado à conta | ✅ |
| RN06 | Rendimento (Poupança): `saldo += saldo * taxa`, apenas com saldo positivo | ✅ |
| RN07 | Juros (Corrente): `saldo -= |saldo| * taxa`, apenas com saldo negativo | ✅ |
| RN08 | Toda transação registra tipo, valor, data/hora e conta de origem | ✅ |
| RN09 | Número da conta é único no sistema (gerado pela aplicação) | ✅ |
| RN10 | Documento do correntista é único no sistema | ✅ |
| RN11 *(Novo)* | Email e telefone do correntista, quando informados, também devem ser únicos | ✅ |
| RN12 *(Novo)* | Taxa de rendimento/juros deve estar no intervalo `(0, 1]` | ✅ |
| RN13 *(Novo)* | Rendimento/juros só podem ser aplicados novamente na mesma conta após 30 dias da última aplicação do mesmo tipo | ✅ — regra adicionada durante a implementação, **não fazia parte do roadmap original** |
| RN14 *(Novo)* | Valores monetários usam `BigDecimal` com escala 2 e arredondamento `HALF_EVEN` | ✅ |

## 5. Escopo de Endpoints (implementados)

Base: `/api/v1`

- `POST /correntistas` — cadastrar correntista
- `GET /correntistas` — listar correntistas (filtro opcional `documento`, paginado)
- `GET /correntistas/{id}` — buscar correntista por id
- `GET /correntistas/{id}/contas` *(Novo)* — listar contas de um correntista
- `POST /contas` — abrir conta (corrente ou poupança)
- `GET /contas/{id}` — buscar conta por id
- `GET /contas` — listar contas (filtros opcionais `correntistaId`, `numero`, paginado)
- `POST /contas/{id}/depositos` — realizar depósito
- `POST /contas/{id}/saques` — realizar saque
- `GET /contas/{id}/extrato` — extrato da conta (filtros opcionais `tipo`, `dataInicial`, `dataFinal`, paginado)
- `POST /contas/{id}/rendimento` — aplicar rendimento (poupança)
- `POST /contas/{id}/juros` — aplicar juros (corrente)

> O caminho do extrato ficou `GET /contas/{id}/extrato` (não `/transacoes`, como
> constava na primeira versão deste documento) — ajustado para bater com a
> implementação real.

## 6. Fora de Escopo

- Transferências entre contas
- Múltiplas moedas
- Notificações (e-mail/SMS)
- Interface gráfica (frontend) — apenas API REST
- Bloqueio otimista/pessimista para concorrência de saldo — **ver observação de risco no roadmap (documento 06); ainda não implementado**