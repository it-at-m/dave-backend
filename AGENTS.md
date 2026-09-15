Repository-specific guidance for agents working on dave-backend. Keep changes minimal and trust executable config over prose.

Quick Start

- Build once to generate clients and run checks: mvn clean install
- Fix formatting failures: mvn spotless:apply
- Run locally with security: ./runLocal.sh or runLocal.bat
- Run locally without Keycloak: ./runLocalNoSecurity.sh or runLocalNoSecurity.bat
- Local server port: 50001 (see src/main/resources/application-local.yml)

Required Services For Local Run

- PostgreSQL
  - Create DB and user to match application-local.yml: url jdbc:postgresql://localhost:5432/dave-db, username dave, password 1234
  - Create schema dave_ng (db.schema). If missing, the app may fail while Flyway checks. Either create the schema manually or temporarily set spring.jpa.hibernate.ddl-auto=create for first boot, then revert to validate.
  - Flyway runs migrations from classpath:db/migration and uses schema dave_ng.

- Elasticsearch 8.15
  - Configure host, user, password, and http-ca-certificate fingerprint in application-local.yml.
  - If ES is unreachable, the app may fail at startup. For a ready-made stack, see README link to docker-compose in dave-frontend repo.

- External services used by generated clients
  - Geodaten EAI base URL: http://localhost:8088 (property geodaten.eai.url)
  - Document Storage base URL: http://localhost:8089 (property document-storage.url)
  - Endpoints are defined via OpenAPI specs in src/main/resources/api. Calls will fail if these services are not running when invoked.

Maven Profiles And Tests

- Maven profiles: server (active by default), local
- Surefire in server profile excludes tests under **/spring/**. To run the full test set locally, use: mvn -Plocal test
- Run a single test method locally: mvn -Plocal -Dtest=ClassName#method test
- Java 21 add-opens flags are already wired for tests/run via surefire and runLocal scripts; do not add them manually.

Generated Code (OpenAPI)

- OpenAPI clients are generated at build from src/main/resources/api/*.json into packages:
  - de.muenchen.dave.geodateneai.gen.*
  - de.muenchen.dave.documentstorage.gen.*
- Do not edit generated sources. Regenerate via: mvn clean install or mvn -DskipTests generate-sources

Formatting/Linting

- Spotless with itm-java-codeformat runs as a check during build. If the build fails on formatting, run: mvn spotless:apply

Coding Conventions

- Follow the project coding conventions: https://github.com/it-at-m/dave/blob/sprint/docs/de/coding-conventions.md

Security

- Keycloak/OAuth2 is enabled by default. For local development without SSO, use the no-security runtime profile via the provided runLocalNoSecurity scripts (spring security autoconfig is excluded by application-no-security.yml).

Scheduling

- Several scheduled jobs are disabled in local profile by setting cron to "-" in application-local.yml. Keep this when developing locally to avoid unintended background processing.

Swagger/OpenAPI UI

- When running locally: http://localhost:50001/swagger-ui/index.html

CI/Release (FYI)

- GitHub Actions use it-at-m/lhm_actions templates for Maven build, image build, and releases. Releases are triggered via the release workflow (workflow_dispatch inputs). No local action needed unless working on the release process.

Docker Image

- The Dockerfile expects a built JAR at target/*.jar. Build with: mvn -DskipTests package, then docker build -t dave-backend:dev .
