# Spring WebFlux JWT

Microservice de autenticacao reativa com JWT, Spring WebFlux e MongoDB.

## Stack

- **Java 21**
- **Spring Boot 3.4.3** (WebFlux, Security 6, Data MongoDB Reactive, Validation, Cache)
- **MongoDB** (reativo)
- **JWT** implementado com JDK puro (`javax.crypto.Mac` + `java.util.Base64`)
- **Sem bibliotecas externas em runtime** - apenas Spring e JDK

## Arquitetura

```
ui/rest/controller/          Controllers REST (Login, User, Role)
application/
  service/                   Servicos (Login, User, Role)
  command/                   Command Pattern (SaveCommand, UpdateCommand, FindAllCommand, FindByIdCommand)
domain/
  document/                  Entidades MongoDB (User, Role, Authority)
  repository/                Repositorios reativos (UserRepository, RoleRepository)
infrastructure/
  security/                  JWT, SecurityConfig, AuthenticationManager
  cache/                     ConcurrentMapCacheManager
  exception/                 GlobalExceptionHandler
```

## Endpoints

| Metodo | Path              | Auth | Permissao      | Descricao                  |
|--------|-------------------|------|----------------|----------------------------|
| POST   | `/login`          | Nao  | -              | Autenticar e obter JWT     |
| POST   | `/user`           | Sim  | SAVE_USER      | Criar usuario              |
| PUT    | `/user`           | Sim  | SAVE_USER      | Atualizar usuario          |
| GET    | `/user`           | Sim  | RETRIEVE_USER  | Listar usuarios            |
| GET    | `/user/{id}`      | Sim  | RETRIEVE_USER  | Buscar usuario por ID      |
| GET    | `/user/currentUser` | Sim | RETRIEVE_USER | Usuario autenticado atual  |
| GET    | `/role`           | Sim  | RETRIEVE_ROLE  | Listar roles               |
| GET    | `/role/{id}`      | Sim  | RETRIEVE_ROLE  | Buscar role por ID         |

## Autenticacao

O login retorna um token JWT no header `Authorization`:

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@email.com","password":"123456"}'
```

Use o token nas requisicoes autenticadas:

```bash
curl http://localhost:8080/user \
  -H "Authorization: Bearer <token>"
```

O token expira em 10 dias e e assinado com HMAC-SHA256.

## Configuracao

### application.properties

| Propriedade     | Descricao                          | Obrigatoria |
|-----------------|------------------------------------|-------------|
| `jwt.secret`    | Chave secreta para assinatura JWT  | Sim         |

Em producao, configure `jwt.secret` via variavel de ambiente:

```bash
JWT_SECRET=sua-chave-secreta-aqui java -jar springwebfluxjwt.jar
```

### MongoDB

Por padrao conecta em `localhost:27017`. Configure via propriedades Spring Data MongoDB:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/nome-do-banco
```

## Build e Testes

### Pre-requisitos

- Java 21+
- Maven 3.9+
- MongoDB 7.0+ (para execucao local)

### Compilar

```bash
mvn clean compile
```

### Executar testes

Os testes usam embedded MongoDB (flapdoodle) e nao requerem banco externo.

```bash
mvn test
```

### Executar testes com cobertura

```bash
mvn clean test
```

O relatorio de cobertura (JaCoCo) e gerado em `target/site/jacoco/index.html`.

### Executar a aplicacao

```bash
mvn spring-boot:run
```

## Testes

54 testes com **96% de cobertura** de codigo (instrucoes JaCoCo).

### Estrutura de testes

```
test/
  ui/rest/controller/test/
    LoginRestControllerTest      5 testes - autenticacao, validacao, credenciais invalidas
    UserRestControllerTest       8 testes - CRUD, duplicata, auth, current user
    RoleRestControllerTest       2 testes - listagem e busca por ID
  application/service/
    UserServiceTest              9 testes - save, update, find, duplicata, erros
    RoleServiceTest              6 testes - save, update, find, propagacao para users
  infrastructure/
    security/
      JWTServiceTest            10 testes - geracao, validacao, claims, assinatura adulterada
    exception/
      GlobalExceptionHandlerTest 2 testes - IllegalArgument (400), Exception generica (500)
  domain/document/
    UserTest                     7 testes - construtores, equals, hashCode, authorities, encode
    RoleTest                     5 testes - construtores, null authorities, equals, hashCode
```

### Cobertura por camada

| Camada | Cobertura |
|--------|-----------|
| Controllers | 100% |
| Services | 92-100% |
| Security | 88-100% |
| Domain | 100% |
| Commands | 100% |
| Exception Handler | 100% |

## Seguranca

- Senhas codificadas com **BCrypt**
- JWT assinado com **HMAC-SHA256** (JDK puro)
- Autorizacao por metodo via `@PreAuthorize`
- CSRF desabilitado (API stateless)
- CORS configuravel via `SecurityConfig`
- Tratamento global de erros via `GlobalExceptionHandler`

## Decisoes Tecnicas

- **Zero bibliotecas externas em runtime**: JWT implementado com `javax.crypto.Mac`, cache com `ConcurrentMapCacheManager` do Spring, sem Lombok (construtores explicitos + Java Records)
- **Java Records** para DTOs imutaveis: `SaveCommand`, `UpdateCommand`, `FindByIdCommand`, `FindAllCommand`, `Authority`, `AccountCredentials`
- **Totalmente reativo**: `Mono`/`Flux` em todas as camadas, sem chamadas bloqueantes
- **Spring Security 6** com Lambda DSL e `useAuthorizationManager`
