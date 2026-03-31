# Spring WebFlux JWT

Microservice de autenticacao reativa com JWT, Spring WebFlux e MongoDB.

## Stack

- **Java 21**
- **Spring Boot 3.4.3** (WebFlux, Security 6, Data MongoDB Reactive, Validation, Actuator)
- **MongoDB** (reativo, com indices automaticos)
- **JWT** implementado com JDK puro (`javax.crypto.Mac` + `java.util.Base64`)

## Arquitetura

```
ui/rest/controller/          Controllers REST (Login, User, Role)
application/
  service/                   Servicos (Login, User, Role)
  command/                   Command Pattern (SaveCommand, UpdateCommand, FindAllCommand, FindByIdCommand)
  dto/                       DTOs de resposta (PageResponse)
domain/
  document/                  Entidades MongoDB (User, Role, Authority)
  repository/                Repositorios reativos (UserRepository, RoleRepository)
infrastructure/
  security/                  JWT, SecurityConfig, AuthenticationManager
  exception/                 GlobalExceptionHandler
```

## Endpoints

| Metodo | Path              | Auth | Permissao      | Descricao                  |
|--------|-------------------|------|----------------|----------------------------|
| POST   | `/login`          | Nao  | -              | Autenticar e obter JWT     |
| POST   | `/user`           | Sim  | SAVE_USER      | Criar usuario              |
| PUT    | `/user`           | Sim  | SAVE_USER      | Atualizar usuario          |
| GET    | `/user?page=0&size=20` | Sim | RETRIEVE_USER | Listar usuarios (paginado) |
| GET    | `/user/{id}`      | Sim  | RETRIEVE_USER  | Buscar usuario por ID      |
| GET    | `/user/currentUser` | Sim | RETRIEVE_USER | Usuario autenticado atual  |
| GET    | `/role?page=0&size=20` | Sim | RETRIEVE_ROLE | Listar roles (paginado)    |
| GET    | `/role/{id}`      | Sim  | RETRIEVE_ROLE  | Buscar role por ID         |

### Paginacao

Os endpoints de listagem retornam `PageResponse`:

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

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

O token expira em **1 hora**, inclui claim `iss` (issuer) e e assinado com HMAC-SHA256.

## Configuracao

### Variaveis de ambiente

| Variavel              | Descricao                          | Obrigatoria |
|-----------------------|------------------------------------|-------------|
| `JWT_SECRET`          | Chave secreta para assinatura JWT  | Sim         |
| `CORS_ALLOWED_ORIGINS`| Origens permitidas (separadas por virgula) | Nao (default: `http://localhost:3000`) |

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
mvn clean verify
```

O relatorio de cobertura (JaCoCo) e gerado em `target/site/jacoco/index.html`.

### Executar a aplicacao

```bash
JWT_SECRET=minha-chave mvn spring-boot:run
```

## Testes

56 testes cobrindo todas as camadas.

### Estrutura de testes

```
test/
  ui/rest/controller/test/
    LoginRestControllerTest       5 testes - autenticacao, validacao, credenciais invalidas
    UserRestControllerTest        8 testes - CRUD, paginacao, duplicata, auth, current user
    RoleRestControllerTest        2 testes - listagem paginada e busca por ID
  application/service/
    UserServiceTest               9 testes - save, update com BCrypt, find, paginacao, erros
    RoleServiceTest               6 testes - save, update, find, paginacao, propagacao para users
  infrastructure/
    security/
      JWTServiceTest             12 testes - geracao, validacao, claims, iss, timing-safe
    exception/
      GlobalExceptionHandlerTest  2 testes - IllegalArgument (400), Exception generica (500)
  domain/document/
    UserTest                      7 testes - construtores, equals por id, authorities, encode
    RoleTest                      5 testes - construtores, null authorities, equals por id
```

## Seguranca

- Senhas codificadas com **BCrypt** (encode no save E update)
- BCrypt offloaded para `Schedulers.boundedElastic()` (nao bloqueia event loop)
- JWT assinado com **HMAC-SHA256** (JDK puro), comparacao timing-safe com `MessageDigest.isEqual()`
- Token com expiracao de **1 hora** e claim `iss` validado
- Autorizacao por metodo via `@PreAuthorize`
- CSRF desabilitado (API stateless)
- CORS configuravel via variavel de ambiente
- Tratamento global de erros via `@RestControllerAdvice`
- Secret JWT externalizado via variavel de ambiente
- Indice unico no campo `email` do User (`@Indexed(unique=true)`)

## Performance

- **Paginacao** em todos os endpoints de listagem (previne OOM)
- **Indice MongoDB** unico no campo `email` (evita collection scan)
- **SecretKeySpec cacheada** no JWTService via `@PostConstruct`
- **BCrypt offloaded** para `Schedulers.boundedElastic()` no save, update e login
- **CORS preflight cache** de 1h (`maxAge=3600`)
- **Graceful shutdown** com timeout de 30s
- **HTTP/2** e compressao gzip habilitados
- **Concurrency limit** no `RoleService.update()` flatMap (max 4)

## Decisoes Tecnicas

- **JWT com JDK puro**: `javax.crypto.Mac` + `java.util.Base64`, sem dependencias externas
- **Java Records** para DTOs imutaveis: `SaveCommand`, `UpdateCommand`, `FindByIdCommand`, `FindAllCommand`, `Authority`, `AccountCredentials`, `PageResponse`
- **Totalmente reativo**: `Mono`/`Flux` em todas as camadas, operacoes bloqueantes em scheduler separado
- **Spring Security 6** com Lambda DSL e `useAuthorizationManager`
- **Identity-based equals/hashCode** nas entidades (apenas por `id`)
- **Spring Boot Actuator** com health, info e metrics expostos
