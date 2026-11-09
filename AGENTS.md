# dave-backend

## Build and verification

- Java 21; use `pom.xml` for dependency versions (the README's Spring Boot version is stale).
- `mvn clean install` generates clients, builds, tests, and checks formatting. The default Maven profile `server` excludes tests under `**/spring/**`.
- `mvn -Plocal test` includes the Spring tests. Focus a class with `mvn -Plocal "-Dtest=ClassName" test`, or a method with `mvn -Plocal "-Dtest=ClassName#method" test` (quotes also work in PowerShell).
- Maven `-Plocal` controls test selection; it is distinct from the Spring runtime profile `local`. Spring tests use `unittest,no-security`, H2 with Flyway disabled, and mocked Elasticsearch repositories; follow the nearby test's schema-initializing H2 URL when needed.
- `mvn spotless:check` checks Java formatting; `mvn spotless:apply` applies itm-java-codeformat, import ordering, and unused-import removal. Standalone Checkstyle XML files are not wired into `pom.xml`.
- Surefire supplies Java `--add-opens` flags and the tests' German locale; the local-run scripts also supply the `--add-opens` flags.

## Code boundaries and generation

- One Spring service, bootstrapped by `src/main/java/de/muenchen/dave/DaveBackendApplication.java`; it enables scheduling and scans `de.muenchen.dave` for components, entities, and JPA repositories.
- Search/station/count metadata lives in `domain/elasticsearch` and `repositories/elasticsearch`; count intervals and computed results use JPA entities in `domain` and `repositories/relationaldb`. Changes may cross both storage systems.
- OpenAPI clients in `de.muenchen.dave.geodateneai.gen.*` and `de.muenchen.dave.documentstorage.gen.*` are generated from `src/main/resources/api/geodateneai.json` and `document-storage.json` during `generate-sources`. Edit the specs/configuration, not generated sources; regenerate with `mvn generate-sources`.
- MapStruct implementations are generated during compilation; edit interfaces in `domain/mapper`. Maven configures Lombok and `lombok-mapstruct-binding` together; the explicit Lombok processor version must match Spring Boot's managed version.
- SQL schema changes belong in `src/main/resources/db/migration/`. Local Flyway uses `${db.schema}` and Hibernate validates the resulting schema; V1 contains PostgreSQL-specific SQL and assigns table ownership to role `dave`.

## Local runtime

- `src/main/resources/application-local.yml` is Git-ignored and must be supplied on a fresh checkout. `stack/readme.md` documents setup; `application-sample.yml` only selects sample SQL and is not a local configuration template.
- Start bundled infrastructure with `docker compose up -d` from `stack/` (PostgreSQL, TLS Elasticsearch, Kibana, pgAdmin). The optional `sample-stack` profile runs published backend/frontend images.
- Local database settings: `jdbc:postgresql://localhost:5432/dave-db`, user `dave`, password `1234`, schema `dave_ng`. Use Flyway migrations with `ddl-auto: validate`.
- Elasticsearch uses custom `elasticsearch.*` properties, not Spring Boot's default connection properties. Configure host, port, user, password, and `http-ca-certificate`; a nonblank fingerprint enables TLS, while an empty value uses HTTP. See `stack/readme.md` for certificate fingerprint extraction.
- Start with `./runLocal.sh` / `.\runLocal.bat`, or `./runLocalNoSecurity.sh` / `.\runLocalNoSecurity.bat` for Spring profiles `local,no-security`. These scripts run `mvn clean spring-boot:run`. Local port is `50001`; Swagger UI is `/swagger-ui/index.html`.
- External client base URLs use `geodaten.eai.url` and `document-storage.url`; both default to empty in their configuration classes. Set them before exercising those integrations. `no-security` selects unsecured client beans as well as disabling servlet security autoconfiguration.
- Scheduling is enabled globally. The current local config disables `dave.messstelle.cron` and `dave.unauffaellige-tage.cron`, but leaves status updates and email polling active: their actual keys are `dave.zaehlung.status-updater.cron` and `dave.email.receiver.cron`; use `"-"` to disable them locally.
- The Dockerfile only copies `target/*.jar`; build first with `mvn -DskipTests package`, then `docker build -t dave-backend:dev .`.
