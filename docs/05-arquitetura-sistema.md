# Arquitetura do Sistema

## 1. Estilo Arquitetural

API REST em camadas, seguindo a estrutura de pacotes real do projeto (pacote
raiz `br.com.sistemabancario.api`, e não `br.com.edu.uniesp` como constava na
v1 deste documento):

```
br.com.sistemabancario.api
├── controller        → recebe requisições HTTP, valida entrada (DTO + Bean Validation), delega ao service
├── database
│   ├── entity         → entidades JPA (@Entity)
│   └── repository     → interfaces Spring Data JPA (I-prefixadas: IContaRepository, ICorrentistaRepository, ITransacaoRepository)
├── dto                → objetos de entrada/saída da API (Request/Response)
├── enums              → TipoConta, TipoTransacao
├── exception          → hierarquia de exceções de negócio + GlobalExceptionHandler
└── service            → regras de negócio e orquestração de transações
```

Fluxo de uma requisição:
```
Cliente HTTP → Controller → Service → Repository → Banco de Dados
                  ↑ DTO         ↑ Entity/Domain
```

- **Controller**: apenas orquestra — recebe DTO, chama o service, converte a resposta em DTO, define o status HTTP. Dois controllers: `CorrentistaController` e `ContaController`.
- **Service**: contém as regras de negócio e orquestra transações (`@Transactional`). **Dividido em dois**, diferente da v1 deste documento:
    - `ContaService` — cadastro/abertura, busca por id, listagem com filtros e listagem por correntista.
    - `TransacaoService` — depósito, saque, rendimento, juros e consulta de extrato. Separado do `ContaService` porque lida com uma responsabilidade distinta (movimentação financeira) e depende do `ITransacaoRepository`, que `ContaService` não usa.
    - `CorrentistaService` — cadastro, busca e listagem de correntistas.
- **Repository**: interfaces `JpaRepository<Entity, Long>`, com queries derivadas por nome de método e uma consulta JPQL customizada (`buscarExtrato`, com filtros opcionais via `:param IS NULL OR ...`).
- **DTO**: desacopla o contrato da API do modelo de persistência (`ContaRequest`, `ContaResponse`, `TransacaoResponse`, `ExtratoItemResponse`, `CorrentistaRequest`, `CorrentistaResponse`, `DepositoRequest`, `SaqueRequest`, `RendimentoRequest`, `JurosRequest`).

## 2. Tratamento de Erros

`GlobalExceptionHandler` (`@RestControllerAdvice`) centraliza o mapeamento de
exceções de negócio para respostas HTTP padronizadas. Todas as exceções de
negócio estendem `NegocioException` (abstrata), que carrega o `HttpStatus` e
o rótulo de `error` — isso é o que permite um único `@ExceptionHandler`
tratar todas elas de forma genérica.

| Exceção | Status HTTP | Usada em |
|---------|-------------|----------|
| `RecursoNaoEncontradoException` | 404 | Correntista ou conta inexistente |
| `RegistroDuplicadoException` | 409 | Documento, e-mail ou telefone já cadastrados |
| `SaldoInsuficienteException` | 422 | Saque acima de saldo/limite; rendimento com saldo não positivo; juros com saldo não negativo |
| `TipoContaInvalidoException` | 422 | Rendimento em conta corrente, juros em conta poupança, e (**bug**, ver observação abaixo) poupança com limite informado |
| `PeriodoMinimoNaoAtingidoException` *(Novo)* | 422 | Rendimento/juros aplicados antes de completar 30 dias da última aplicação do mesmo tipo na conta |
| `RequisicaoInvalidaException` | 400 | Período do extrato inválido (`dataInicial > dataFinal`); definida também para poupança-com-limite e limite-negativo, mas **ainda não usada** nesses dois casos (ver abaixo) |
| `ValorInvalidoException` | 400 | Valor de depósito/saque/limite inválido; taxa fora do intervalo `(0, 1]` |
| `MethodArgumentNotValidException` (Bean Validation) | 400 | Campos obrigatórios ausentes ou fora das constraints do DTO (`@NotNull`, `@NotBlank`, `@DecimalMin`, `@Email`) |
| `Exception` (genérica) | 500 | Qualquer erro não mapeado — resposta padronizada, sem vazar stack trace |

### ⚠️ Bug conhecido — status incorreto na criação de conta poupança com limite

`ContaService.criarConta` lança `new TipoContaInvalidoException(...)` (422)
quando uma conta poupança recebe `limite` diferente de zero. O contrato do
roadmap (documento 06) define `400` como status esperado para esse erro em
`POST /contas`, e a exceção `RequisicaoInvalidaException.limiteNaoPermitidoParaPoupanca()`
já existe pronta para esse caso — só falta trocar a chamada no service. Até lá,
os testes escritos conforme o contrato (`ContaControllerTest.deveRetornar400QuandoPoupancaInformaLimite`)
vão falhar contra o código atual.

Resposta de erro padrão (exemplo real, formato `ErrorResponseDTO`):
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

## 3. Persistência e Migrations

- Spring Data JPA/Hibernate sobre MySQL (perfil `dev`) ou H2 (perfil `test`).
- Flyway gerencia o versionamento do schema em `resources/db/migration` (`V1__create_schema.sql`).
- `spring.jpa.hibernate.ddl-auto=validate` em ambos os perfis — o schema é fonte de verdade via Flyway, não o Hibernate auto-DDL.
- `spring.jpa.open-in-view=false` — evita lazy loading fora da transação (importante porque `Correntista.contas` e `Conta.correntista` são `LAZY`).

## 4. Containerização (Docker)

Configuração real do projeto (`docker-compose.yml` e `Dockerfile` na raiz):

```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: ${MYSQL_DATABASE:-sistema_bancario}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h 127.0.0.1 -P 3306 -uroot -p$${MYSQL_ROOT_PASSWORD} --silent"]
      interval: 5s
      timeout: 5s
      retries: 20
      start_period: 10s

  app:
    build: .
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: ${DATABASE_URL:-jdbc:mysql://mysql:3306/sistema_bancario}
      SPRING_DATASOURCE_USERNAME: ${DATABASE_USERNAME:-root}
      SPRING_DATASOURCE_PASSWORD: ${DATABASE_PASSWORD:-root}
    ports:
      - "8080:8080"

volumes:
  mysql_data:
```

```dockerfile
FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

As variáveis de ambiente (`MYSQL_DATABASE`, `MYSQL_ROOT_PASSWORD`,
`DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`) têm um exemplo em
`.env.exemple` na raiz do projeto — copie para `.env` antes de rodar
`docker compose up`.

## 5. Documentação da API

- Dependência `org.springdoc:springdoc-openapi-ui:1.7.0` incluída no `pom.xml` (compatível com Spring Boot 2.7 / Java 8 — a versão 2.x do springdoc exige Spring Boot 3).
- Expõe `/swagger-ui.html` e `/v3/api-docs` automaticamente a partir dos `@RestController`/DTOs existentes.
- **Pendência (RF13):** nenhuma anotação `@Schema`, `@Operation` ou exemplo customizado foi adicionada ainda — o Swagger gerado hoje é o default a partir da introspecção dos controllers/DTOs, sem descrições de negócio.

## 6. Resumo das Dependências Principais (`pom.xml` real)

- `spring-boot-starter-parent` — `2.7.18`
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator` — expõe `/actuator/health`
- `spring-boot-starter-test` (escopo `test`)
- `mysql-connector-j` (escopo `runtime`)
- `com.h2database:h2` (escopo `runtime`, usado no perfil `test`)
- `org.flywaydb:flyway-core` + `org.flywaydb:flyway-mysql`
- `org.springdoc:springdoc-openapi-ui:1.7.0`
- `org.projectlombok:lombok` (`optional`, com `annotationProcessorPaths` fixado em `1.18.32` no `maven-compiler-plugin`)