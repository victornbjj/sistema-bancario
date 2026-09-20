# Sistema Bancário

API REST para gerenciamento de correntistas, contas corrente/poupança e
transações financeiras.

## Pré-requisitos

- Java 8 ou superior
- Docker Desktop com Docker Compose
- Git

## Executar os testes localmente

No Windows:

```powershell
.\mvnw.cmd clean verify
```

Os testes usam o perfil `test` e o banco H2 em memória. O Flyway executa as
migrations durante a inicialização do contexto de teste.

## Executar com Docker Compose

1. Copie o arquivo de exemplo:

```powershell
Copy-Item .env.exemple .env
```

2. Ajuste os valores do arquivo `.env` se necessário. O arquivo deve manter a
mesma senha para `MYSQL_ROOT_PASSWORD` e `DATABASE_PASSWORD`.

3. Construa e inicie o MySQL e a aplicação:

```powershell
docker compose up --build
```

O serviço da aplicação usa o perfil `dev`, aguarda o healthcheck do MySQL e
executa as migrations do Flyway. A API fica disponível na porta `8080`.

## Verificar a saúde da aplicação

Em outro terminal:

```powershell
Invoke-WebRequest http://localhost:8080/actuator/health
```

Uma aplicação funcionando deve responder HTTP `200` com:

```json
{
  "status": "UP"
}
```

Também é possível acessar no navegador:

```text
http://localhost:8080/actuator/health
```

## Encerrar o ambiente

```powershell
docker compose down
```

O comando encerra os containers e preserva o volume do MySQL para a próxima
execução.

## Migrations

As migrations ficam em:

```text
src/main/resources/db/migration
```

O Hibernate está configurado com `ddl-auto=validate`, portanto as tabelas são
criadas e versionadas pelo Flyway, não pelo Hibernate.
