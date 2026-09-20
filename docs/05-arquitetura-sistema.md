# Arquitetura do Sistema

## 1. Estilo Arquitetural

API REST em camadas (estilo MVC adaptado para back-end), seguindo a estrutura de pacotes já definida:

```
br.com.edu.uniesp
├── controller        → recebe requisições HTTP, valida entrada (DTO), delega ao service
├── database
│   ├── entitity       → entidades JPA (@Entity)
│   └── repository     → interfaces Spring Data JPA
├── dto                → objetos de entrada/saída da API (Request/Response)
├── enums              → TipoConta, TipoTransacao
└── service            → regras de negócio e orquestração de transações
```

Fluxo de uma requisição:
```
Cliente HTTP → Controller → Service → Repository → Banco de Dados
                  ↑ DTO         ↑ Entity/Domain
```

- **Controller**: apenas orquestra — recebe DTO, chama Service, converte resposta em DTO, define status HTTP.
- **Service**: contém as regras de negócio (RN01–RN10), orquestra transações (`@Transactional`), lança exceções de negócio.
- **Repository**: interfaces `JpaRepository<Entity, Long>`, sem lógica além de queries derivadas/JPQL.
- **DTO**: desacopla o contrato da API do modelo de persistência (ex.: `ContaRequestDTO`, `ContaResponseDTO`, `TransacaoResponseDTO`).

## 2. Tratamento de Erros

`GlobalExceptionHandler` (`@RestControllerAdvice`) centraliza o mapeamento de exceções de negócio para respostas HTTP padronizadas:

| Exceção | Status HTTP |
|---------|-------------|
| `RecursoNaoEncontradoException` | 404 |
| `SaldoInsuficienteException` | 422 |
| `DocumentoJaCadastradoException` | 409 |
| `MethodArgumentNotValidException` (Bean Validation) | 400 |

Resposta de erro padrão (exemplo):
```json
{
  "timestamp": "2026-09-17T14:32:00",
  "status": 422,
  "error": "Saldo Insuficiente",
  "message": "Saque de R$ 500,00 excede o limite disponível de R$ 300,00",
  "path": "/contas/12/saques"
}
```

## 3. Persistência e Migrations

- Spring Data JPA/Hibernate sobre MySQL (produção/dev) ou H2 (perfil de testes).
- Flyway gerencia versionamento do schema em `resources/db/migration` (`V1__create_schema.sql`, `V2__...`).
- `spring.jpa.hibernate.ddl-auto=validate` em produção (o schema é fonte de verdade via Flyway, não o Hibernate auto-DDL).

## 4. Containerização (Docker)

```
docker-compose.yml
├── mysql       → imagem mysql:8, porta 3306, volume persistente, healthcheck
└── app         → build a partir do Dockerfile (multi-stage: build Maven + runtime JRE), porta 8080
```

Esboço de `docker-compose.yml`:
```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: sistema_bancario
      MYSQL_ROOT_PASSWORD: root
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 5s
      retries: 10

  app:
    build: .
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/sistema_bancario
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
    ports:
      - "8080:8080"

volumes:
  mysql_data:
```

Dockerfile (multi-stage, compatível com Java 8+):
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

## 5. Documentação da API

- SpringDoc OpenAPI (`springdoc-openapi-starter-webmvc-ui`) expõe `/swagger-ui.html` e `/v3/api-docs`.
- README documenta endpoints, exemplos de payload JSON e passos de execução (`docker compose up`).

## 6. Resumo das Dependências Principais (pom.xml)

- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `mysql-connector-j` / `com.h2database:h2` (escopo test)
- `org.flywaydb:flyway-mysql`
- `org.springdoc:springdoc-openapi-starter-webmvc-ui`
- `org.projectlombok:lombok`
