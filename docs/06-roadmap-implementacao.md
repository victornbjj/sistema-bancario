# Roadmap de Implementacao — API de Conta Bancaria

## 1. Objetivo

Construir uma API REST para a cooperativa de credito, cobrindo correntistas, contas corrente/poupanca, movimentacoes, extrato, autenticacao JWT e operacoes financeiras diferenciais.

A implementacao deve preservar as regras dos documentos 01 a 05 e ser entregue em incrementos executaveis. Cada marco so e considerado concluido quando sua validacao passar.

## 2. Decisoes de contrato

- Base da API: `/api/v1`.
- Valores monetarios: JSON numerico com duas casas, representado internamente por `BigDecimal`; nunca usar `double`.
- Datas: ISO-8601, por exemplo `2026-09-18T14:30:00Z`.
- IDs: `Long` gerado pelo banco; numero da conta gerado pela aplicacao e unico.
- Operacoes financeiras sao atomicas com `@Transactional`.
- `RENDIMENTO` e `JUROS` registram em `valor` somente o valor aplicado, sempre positivo; o saldo atualizado e retornado na resposta.
- Taxas sao decimais entre `0` e `1`: `0.005` representa `0,5%`.
- Nao sera criado endpoint publico para cadastrar `Usuario` nesta primeira versao. Um usuario inicial sera criado por seed/migration segura ou configuracao de bootstrap.
- `OPERADOR` pode executar as operacoes de negocio. `ADMIN` pode executar as mesmas operacoes e administrar usuarios em uma evolucao futura; como nao existe requisito de CRUD de usuarios, nenhum endpoint adicional e necessario agora.
- Todos os endpoints, exceto `POST /api/v1/auth/login`, exigem `Authorization: Bearer <jwt>`.

## 3. Contrato de endpoints

### Autenticacao

| Metodo | Endpoint | Entrada | Saida | Erros principais |
|---|---|---|---|---|
| POST | `/api/v1/auth/login` | `{ "username": "operador", "senha": "..." }` | `200` com `{ token, tipo: "Bearer", expiraEm }` | `400`, `401` |

### Correntistas

| Metodo | Endpoint | Entrada | Saida | Erros principais |
|---|---|---|---|---|
| POST | `/api/v1/correntistas` | `{ nome, documento, email?, telefone? }` | `201` com recurso criado e `Location` | `400`, `409` |
| GET | `/api/v1/correntistas` | filtros opcionais `documento`, `page`, `size` | `200` paginado | `400` |
| GET | `/api/v1/correntistas/{id}` | - | `200` | `404` |

### Contas

| Metodo | Endpoint | Entrada | Saida | Erros principais |
|---|---|---|---|---|
| POST | `/api/v1/contas` | `{ correntistaId, tipo: "CORRENTE"|"POUPANCA", limite? }` | `201` com conta e `Location` | `400`, `404`, `409` |
| GET | `/api/v1/contas/{id}` | - | `200` com saldo, tipo e titular | `404` |
| GET | `/api/v1/contas` | `correntistaId?`, `numero?`, `page`, `size` | `200` paginado | `400`, `404` quando filtro referencia recurso inexistente |

Regras de abertura:

- `correntistaId` deve existir.
- `tipo=CORRENTE` exige `limite >= 0`; se ausente, usar zero.
- `tipo=POUPANCA` rejeita `limite` informado ou diferente de zero.
- saldo inicial e zero.

### Movimentacoes

| Metodo | Endpoint | Entrada | Saida | Erros principais |
|---|---|---|---|---|
| POST | `/api/v1/contas/{id}/depositos` | `{ "valor": 100.00 }` | `201` com transacao e saldoAtual | `400`, `404` |
| POST | `/api/v1/contas/{id}/saques` | `{ "valor": 50.00 }` | `201` com transacao e saldoAtual | `400`, `404`, `422` |
| GET | `/api/v1/contas/{id}/transacoes` | `dataInicial?`, `dataFinal?`, `tipo?`, `page`, `size` | `200` ordenado por data decrescente | `400`, `404` |
| POST | `/api/v1/contas/{id}/rendimento` | query `taxa` ou `{ "taxa": 0.005 }` | `201` com transacao e saldoAtual | `400`, `404`, `422` |
| POST | `/api/v1/contas/{id}/juros` | query `taxa` ou `{ "taxa": 0.02 }` | `201` com transacao e saldoAtual | `400`, `404`, `422` |

Regras de movimentacao:

- Todo valor de deposito e saque deve ser maior que zero.
- Conta corrente permite saque quando `valor <= saldo + limite`.
- Conta poupanca permite saque somente quando `valor <= saldo`.
- Rendimento exige conta poupanca, saldo positivo e taxa entre `0` e `1`.
- Juros exige conta corrente, saldo negativo e taxa entre `0` e `1`.
- Concorrencia deve impedir perda de atualizacao; usar bloqueio otimista ou pessimista e testar duas operacoes simultaneas.

### Resposta de erro

Todos os erros devem seguir o mesmo formato:

```json
{
  "timestamp": "2026-09-18T14:30:00Z",
  "status": 422,
  "error": "Saldo insuficiente",
  "message": "O valor excede o saldo e o limite disponivel.",
  "path": "/api/v1/contas/12/saques",
  "fieldErrors": []
}
```

## 4. Etapas e marcos

### Marco 0 — Fundacao e ambiente

**Entregas**

- Criar projeto Spring Boot com Java 8+ e dependencias Web, Validation, JPA, Security, JWT, Flyway, MySQL, H2 e OpenAPI.
- Definir pacotes conforme a arquitetura: `controller`, `service`, `database.entity`, `database.repository`, `dto`, `enums`, `security`.
- Configurar perfis `test` e `dev`.
- Adicionar `Dockerfile`, `docker-compose.yml`, `.gitignore` e pipeline minima de build/teste.

**Validacao de saida**

- `mvn clean verify` passa sem testes ignorados.
- `docker compose up --build` inicia MySQL e aplicacao.
- `GET /actuator/health` ou endpoint equivalente responde `200`.
- Aplicacao sobe com `ddl-auto=validate`; nenhuma tabela e criada fora do Flyway.

### Marco 1 — Schema e persistencia

**Entregas**

- Implementar entidades `Correntista`, `Conta`, `ContaCorrente`, `ContaPoupanca`, `Transacao` e `Usuario`.
- Aplicar heranca JPA `JOINED`.
- Criar `V1__create_schema.sql` com chaves, FKs, uniques, decimais e enums.
- Implementar repositories e seed do usuario inicial.

**Validacao de saida**

- Teste de contexto confirma execucao do Flyway em banco limpo.
- Migration falha se documento, numero da conta ou username duplicar.
- Teste de persistencia confirma que uma conta corrente e uma poupanca usam as tabelas filhas corretas.
- Teste confirma que transacao nao pode existir sem conta.

### Marco 2 — Dominio financeiro

**Entregas**

- Encapsular saldo sem setter publico.
- Implementar `depositar`, `sacar`, `aplicarRendimento` e `aplicarJuros` nas entidades/servicos de dominio.
- Implementar geracao de numero unico de conta.
- Criar excecoes de negocio: recurso inexistente, saldo insuficiente, tipo de conta invalido, valor invalido e duplicidade.

**Validacao de saida**

- Testes unitarios cobrem deposito positivo e rejeicao de valor zero/negativo.
- Conta corrente aceita saque dentro do saldo mais limite e rejeita acima dele.
- Conta poupanca rejeita saque acima do saldo.
- Rendimento usa `saldo + saldo * taxa`.
- Juros em saldo negativo aumenta o valor da divida conforme a regra definida.
- Nenhuma operacao invalida altera saldo ou cria transacao.

### Marco 3 — Correntistas e contas

**Entregas**

- Criar DTOs de request/response com Bean Validation.
- Implementar `CorrentistaController` e `ContaController`.
- Implementar servicos de cadastro, consulta, abertura e filtros/paginacao.
- Retornar `201 Created` e cabecalho `Location` nos POST de criacao.

**Validacao de saida**

- Testes MockMvc verificam payload valido, `201`, `Location` e formato da resposta.
- Documento duplicado retorna `409`.
- Correntista inexistente ao abrir conta retorna `404`.
- Limite em poupanca e dados ausentes obrigatorios retornam `400`.
- Consultas por ID, numero e correntista retornam resultados corretos.

### Marco 4 — Deposito, saque e extrato

**Entregas**

- Implementar endpoints de deposito, saque e transacoes.
- Garantir `@Transactional` em cada operacao financeira.
- Persistir exatamente uma transacao por deposito/saque confirmado.
- Implementar filtros de periodo, tipo e ordenacao do extrato.

**Validacao de saida**

- Teste de integracao valida saldo antes/depois e quantidade de transacoes.
- Falha de saque retorna `422` e deixa saldo/extrato inalterados.
- Extrato retorna transacoes da conta correta, ordenadas por data decrescente.
- Teste concorrente demonstra ausencia de lost update.
- Repeticao de uma requisicao com identificador de idempotencia, se adotado, nao duplica movimento.

### Marco 5 — Rendimento e juros

**Entregas**

- Implementar `/rendimento` para poupanca.
- Implementar `/juros` para corrente negativa.
- Validar taxa, tipo de conta, sinal do saldo e registro da transacao.
- Documentar que as operacoes sao disparadas por requisicao administrativa; agendamento automatico fica fora da primeira entrega.

**Validacao de saida**

- Rendimento em poupanca positiva altera saldo pelo valor exato e cria `RENDIMENTO`.
- Rendimento em corrente, poupanca zerada ou taxa invalida retorna `422` sem efeitos.
- Juros em corrente negativa altera saldo pelo valor exato e cria `JUROS`.
- Juros em poupanca ou corrente nao negativa retorna `422` sem efeitos.
- Testes de arredondamento confirmam escala de duas casas e politica definida.

### Marco 6 — Seguranca e autorizacao

**Entregas**

- Implementar `SecurityConfig`, `JwtUtil`, `JwtAuthFilter`, `UserDetailsService` e BCrypt.
- Liberar somente `POST /api/v1/auth/login`, OpenAPI e health check.
- Proteger recursos com role `OPERADOR` ou `ADMIN`.
- Padronizar respostas `401` e `403`.

**Validacao de saida**

- Login valido retorna JWT com subject, role e expiracao.
- Senha invalida retorna `401` sem revelar qual credencial falhou.
- Requisicao sem token retorna `401`.
- Token expirado ou assinatura invalida retorna `401`.
- Token valido sem permissao retorna `403`.
- Testes confirmam que senha nunca aparece em respostas ou logs.

### Marco 7 — Tratamento de erros, contrato e observabilidade

**Entregas**

- Implementar `GlobalExceptionHandler` para excecoes de negocio, validacao, autenticacao e acesso.
- Padronizar mensagens, timestamp, status, path e erros de campo.
- Adicionar OpenAPI/Swagger com schemas, autenticacao Bearer, exemplos e codigos de resposta.
- Adicionar logs estruturados sem senha, token ou dados sensiveis.

**Validacao de saida**

- Cada endpoint possui documentacao OpenAPI acessivel em `/swagger-ui.html` e `/v3/api-docs`.
- Testes de contrato conferem os status `200`, `201`, `400`, `401`, `403`, `404`, `409` e `422` previstos.
- Payloads de erro sao consistentes em todos os controllers.
- Revisao manual confirma que exemplos Swagger podem ser executados na ordem de um fluxo completo.

### Marco 8 — Empacotamento e aceite final

**Entregas**

- Finalizar README com pre-requisitos, configuracao, migrations, Docker, autenticacao e exemplos curl.
- Criar testes de ponta a ponta do fluxo: login -> correntista -> conta -> deposito -> saque -> extrato.
- Executar build, testes, verificacao de migration e subida via Docker.
- Revisar requisitos RF01-RF14 e RNF01-RNF12 com rastreabilidade.

**Validacao de saida**

- Ambiente limpo reproduz a aplicacao com `docker compose up --build`.
- Suite unitaria, integracao e E2E passa sem testes desabilitados.
- Fluxo principal completo funciona com JWT.
- Regras de corrente, poupanca, rendimento e juros possuem evidencia automatizada.
- README permite que outra pessoa execute e consuma a API sem conhecimento adicional.
- Checklist de aceite marca cada RF/RNF como implementado, testado ou explicitamente fora do escopo.

## 5. Ordem recomendada de execucao

1. Marco 0: fundacao e ambiente.
2. Marco 1: banco e persistencia.
3. Marco 2: dominio financeiro.
4. Marco 3: correntistas e contas.
5. Marco 4: deposito, saque e extrato.
6. Marco 5: rendimento e juros.
7. Marco 6: seguranca.
8. Marco 7: erros e documentacao.
9. Marco 8: aceite final e empacotamento.

Cada marco deve ser entregue em commits pequenos, com testes incluidos no mesmo incremento. O proximo marco nao deve comecar enquanto a validacao do marco anterior estiver falhando.

## 6. Riscos e pontos de atencao

- **Concorrencia de saldo:** sem bloqueio, dois saques simultaneos podem produzir saldo incorreto. Deve ser resolvido no Marco 4 e coberto por teste.
- **Arredondamento:** definir escala e `RoundingMode` antes dos testes de rendimento/juros; a recomendacao e `HALF_EVEN` para valores monetarios calculados.
- **JWT e segredo:** segredo deve vir de variavel de ambiente, nunca do repositorio.
- **Flyway e H2:** validar compatibilidade real do SQL em H2; se houver divergencia, usar migration especifica por banco sem duplicar regra de negocio.
- **Idempotencia:** a especificacao original nao exige chave de idempotencia. Para operacoes financeiras em producao, recomenda-se adicionar `Idempotency-Key` antes de liberar clientes externos.
- **Agendamento:** os documentos mencionam o sistema automatico, mas os endpoints definidos sao disparados por operador. Agendamento mensal automatico deve ser tratado como evolucao separada.

## 7. Criterio final de pronto

A API esta pronta quando o fluxo autenticado completo passa em ambiente limpo, todas as regras RN03-RN08 possuem testes unitarios e de integracao, o schema e reproduzivel por Flyway, os erros seguem um contrato unico, a documentacao OpenAPI esta atualizada e cada requisito dos documentos de origem possui evidencia de implementacao ou justificativa de fora de escopo.
