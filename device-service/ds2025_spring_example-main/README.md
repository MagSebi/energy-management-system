# Demo — Spring Boot API

A simple Spring Boot REST API (people service) with PostgreSQL db associated. Includes a ready-to-use Postman collection for quick testing.

## Contents

## Project structure
```
demo/
├── .mvn
│   └── wrapper
│       └── maven-wrapper.properties
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── demo
│   │   │               ├── controllers
│   │   │               │   └── PersonController.java
│   │   │               ├── dtos
│   │   │               │   ├── builders
│   │   │               │   │   └── PersonBuilder.java
│   │   │               │   ├── validators
│   │   │               │   │   ├── annotation
│   │   │               │   │   │   └── AgeLimit.java
│   │   │               │   │   └── AgeValidator.java
│   │   │               │   ├── PersonDetailsDTO.java
│   │   │               │   └── PersonDTO.java
│   │   │               ├── entities
│   │   │               │   └── Person.java
│   │   │               ├── handlers
│   │   │               │   ├── exceptions
│   │   │               │   │   └── model
│   │   │               │   │       ├── CustomException.java
│   │   │               │   │       ├── ExceptionHandlerResponseDTO.java
│   │   │               │   │       └── ResourceNotFoundException.java
│   │   │               │   └── RestExceptionHandler.java
│   │   │               ├── repositories
│   │   │               │   └── PersonRepository.java
│   │   │               ├── services
│   │   │               │   └── PersonService.java
│   │   │               └── DemoApplication.java
│   │   └── resources
│   │       ├── static
│   │       ├── templates
│   │       └── application.properties
│   └── test
│       └── java
│           └── com
│               └── example
│                   └── demo
│                       └── DemoApplicationTests.java
├── .gitattributes
├── .gitignore
├── HELP.md
├── mvnw
├── mvnw.cmd
├── pom.xml
└── postman_collection.json
```

- `src/main/...` — SpringBoot source
- `src/main/resources/application.properties` — app configuration
- `postman_collection.json` — Postman collection to import
- `pom.xml` — Maven project wht Spring Boot 4.0.0-SNAPSHOT and Java 25

## Prerequisites
- **Java JDK 25**
- **PostgreSQL** server accessible from the app (can be changed to any other db from application.properties)
- **Postman** account to import & run the test collection

## Database (PostgreSQL) — ( !!! Create it first !!!)
The app expects a PostgreSQL database to already exist. Default connection values:
```
DB_IP=localhost
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=root
DB_DBNAME=example-db
```

> Note: Hibernate is set to `spring.jpa.hibernate.ddl-auto=update`, so tables will be created/updated automatically on first run

## Configuration
All important settings are in `src/main/resources/application.properties`. You can override them via environment variables:

| Purpose | Property | Env var | Default |
|---|---|---|---|
| DB host | `database.ip` | `DB_IP` | `localhost` |
| DB port | `database.port` | `DB_PORT` | `5432` |
| DB user | `database.user` | `DB_USER` | `postgres` |
| DB password | `database.password` | `DB_PASSWORD` | `root` |
| DB name | `database.name` | `DB_DBNAME` | `example-db` |
| HTTP port | `server.port` | `PORT` | `8080` |

Effective JDBC URL:
```
jdbc:postgresql://${DB_IP}:${DB_PORT}/${DB_DBNAME}
```

## How to run (local)
From the project root (`demo/`), run with the Maven Wrapper:

```bash
# 1) export env vars if you need non-defaults
export DB_IP=localhost
export DB_PORT=5432
export DB_USER=postgres
export DB_PASSWORD=root
export DB_DBNAME=example-db
export PORT=8080

# 2) start the app
./mvnw spring-boot:run
```

The app will start on: **http://localhost:8080** (unless you changed `PORT`).

## API quick peek
The included Postman collection targets the **people** resource defined by the **Person** entity.
Examples once the app is running:
- `GET /people` — list all
- `POST /people` — create (body: JSON person)
- `GET /people/{personId}` — fetch one
- `PUT /people/{personId}` — update
- `DELETE /people/{personId}` — delete

## Test with Postman
1. Create/sign in to your **Postman** account;
2. **Import** the collection file: [`postman_collection.json`];
3. In Postman, verify the collection variables so that you know everything is set up correctly:
   - `baseUrl` → `http://localhost:8080`
   - `resource` → `people`
4. Run the requests in order (the collection includes a test that remembers `personId` after create) 

## Where it runs
By default the app binds to `PORT` (default **8080**) on your machine

---