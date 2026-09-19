# Levantamento de Requisitos — API de Conta Bancária (Cooperativa de Crédito)

## 1. Visão Geral

Sistema RESTful para gerenciamento de correntistas, contas bancárias (corrente e poupança) e transações financeiras (depósito, saque, rendimento, juros) de uma cooperativa de crédito.

## 2. Requisitos Funcionais (RF)

| ID | Descrição | Prioridade |
|----|-----------|------------|
| RF01 | O sistema deve permitir cadastrar um correntista (nome, documento, dados de contato) | Obrigatório |
| RF02 | O sistema deve permitir consultar correntista(s) cadastrado(s) | Obrigatório |
| RF03 | O sistema deve permitir abrir uma conta vinculada a um correntista, do tipo Corrente ou Poupança | Obrigatório |
| RF04 | O sistema deve permitir consultar conta(s) por id, número ou por correntista | Obrigatório |
| RF05 | O sistema deve permitir realizar depósito em uma conta, aumentando o saldo e registrando a transação | Obrigatório |
| RF06 | O sistema deve permitir realizar saque em uma conta, reduzindo o saldo e registrando a transação | Obrigatório |
| RF07 | O sistema deve permitir consultar o extrato (listagem de transações) de uma conta | Obrigatório |
| RF08 | Uma Conta Corrente deve possuir um limite de crédito, permitindo saque até `saldo + limite` | Obrigatório |
| RF09 | Uma Conta Poupança não deve permitir saque além do saldo disponível | Obrigatório |
| RF10 | Um correntista pode possuir várias contas | Obrigatório |
| RF11 | O sistema deve permitir aplicar rendimento mensal em Conta Poupança, com taxa informada por parâmetro, atualizando saldo e registrando transação | Diferencial |
| RF12 | O sistema deve permitir aplicar juros sobre saldo negativo de Conta Corrente, com taxa informada por parâmetro, atualizando saldo e registrando transação | Diferencial |
| RF13 | O sistema deve documentar a API via Swagger/OpenAPI | Diferencial |

## 3. Requisitos Não Funcionais (RNF)

| ID | Descrição |
|----|-----------|
| RNF01 | Deve ser desenvolvido em Java 8+ com Spring Boot |
| RNF02 | Persistência via Spring Data JPA/Hibernate |
| RNF03 | Banco de dados relacional: MySQL (produção/dev) ou H2 (testes) |
| RNF04 | Versionamento de schema via migrations (Flyway), em `resources/db/migration` |
| RNF05 | Ambiente de execução conteinerizado via Docker/Docker Compose (app + MySQL) |
| RNF06 | API deve seguir convenções REST: verbos HTTP corretos e códigos de status adequados |
| RNF07 | Tratamento de erros padronizado (GlobalExceptionHandler, respostas de erro consistentes) |
| RNF08 | Código organizado em camadas: Controller, Service, Repository, DTO, Entity |
| RNF09 | Repositório Git público com histórico de commits incrementais |
| RNF10 | README com instruções de execução local, script SQL/migration e exemplos de requisições |
| RNF11 | Testes unitários cobrindo regras de saque, depósito e cálculo de rendimento/juros (diferencial) |

## 4. Regras de Negócio (RN)

| ID | Regra |
|----|-------|
| RN01 | Correntista 1:N Contas — um correntista pode ter zero ou várias contas |
| RN02 | Conta é uma entidade abstrata especializada em ContaCorrente e ContaPoupanca (herança) |
| RN03 | Saque em ContaCorrente: permitido se `valorSaque <= saldo + limite` |
| RN04 | Saque em ContaPoupanca: permitido se `valorSaque <= saldo` |
| RN05 | Todo depósito e saque gera um registro de Transação vinculado à conta de origem |
| RN06 | Rendimento (Poupança) é calculado sobre o saldo positivo: `saldo += saldo * taxa` |
| RN07 | Juros (Corrente) é calculado sobre saldo negativo: `saldo += |saldo| * taxa` (agrava a dívida) |
| RN08 | Toda transação registra tipo, valor, data/hora e conta de origem |
| RN09 | Número da conta é único no sistema |
| RN10 | Documento do correntista (CPF/CNPJ) é único no sistema |

## 5. Escopo de Endpoints (visão geral)

- `POST /correntistas` — cadastrar correntista
- `GET /correntistas` / `GET /correntistas/{id}` — consultar correntista(s)
- `POST /contas` — abrir conta (corrente ou poupança)
- `GET /contas/{id}` / `GET /contas?correntistaId=` — consultar conta(s)
- `POST /contas/{id}/depositos` — realizar depósito
- `POST /contas/{id}/saques` — realizar saque
- `GET /contas/{id}/transacoes` — extrato da conta
- `POST /contas/{id}/rendimento` — aplicar rendimento (poupança, diferencial)
- `POST /contas/{id}/juros` — aplicar juros (corrente, diferencial)

## 6. Fora de Escopo

- Transferências entre contas (não solicitado no desafio)
- Múltiplas moedas
- Notificações (e-mail/SMS)
- Interface gráfica (frontend) — apenas API REST
