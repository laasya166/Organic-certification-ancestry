# Organic Certification Ancestry (SOA-S03-T08, Team 8)

Course 24SDCS03. A processor blends input lots into one output lot; the blend is ORGANIC_ELIGIBLE only if
every input lot has a valid, non-revoked certificate on the processing date. Otherwise it is UNCERTIFIED.

## Who owns what
| Person | Modules | Focus |
|---|---|---|
| **A** Platform and Security | parent pom, docker, `eureka-server`, `api-gateway`, `auth-service`, `common/security` | CO2, CO3 |
| **B** Producer | `certificate-service`, `lot-ancestry-service` (publishes BlendCreated) | CO1 |
| **C** Consumer and Quality | `blend-validation-service`, ALL tests, GitHub Actions | CO4 |

Only edit your own modules. Found a bug in someone else's? Message them. The shared files are the parent `pom.xml`
and `common/` (tell the team before changing `BlendCreatedEvent`).

## Ports
| Service | Port | Database |
|---|---|---|
| eureka-server | 8761 | none |
| api-gateway | 8080 | none |
| auth-service | 8081 | auth_db |
| lot-ancestry-service | 8082 | ancestry_db |
| certificate-service | 8083 | certificate_db |
| blend-validation-service | 8084 | validation_db |
| Kafka / Postgres | 9092 / 5432 | |

## Start it
```bash
docker compose up -d                 # Kafka + Postgres (4 databases created by docker/init.sql)
mvn -q -DskipTests install           # build all modules (needs Java 17+, Maven 3.9+)
mvn -pl eureka-server spring-boot:run        # start first
mvn -pl api-gateway spring-boot:run
mvn -pl auth-service spring-boot:run
mvn -pl certificate-service spring-boot:run
mvn -pl lot-ancestry-service spring-boot:run
mvn -pl blend-validation-service spring-boot:run
```
Each in its own terminal. Check http://localhost:8761 (all services listed) and `GET http://localhost:8080/...`.
Second instance for the failover demo:
`mvn -pl certificate-service spring-boot:run -Dspring-boot.run.arguments=--server.port=8093`

Smoke test through the gateway (before real endpoints exist): there is a `GET /ping` in each domain service, e.g. call
the service directly at `http://localhost:8083/ping`.

## Security switch
`app.security.enabled` is `false` in every `application.yml` so you can work without tokens.
At **Checkpoint 1** set it to `true` everywhere (A announces it). Same `JWT_SECRET` for everyone
(default dev secret is in each yml; use an env var for the demo).

## Contracts (agreed, do not change silently)
See `docs/contracts.md`.

## Checkpoints (do these together)
1. **Checkpoint 1 (about 3h in):** all services up and registered in Eureka, stubs removed, security enabled,
   one blend created through the gateway ends as UNCERTIFIED/ORGANIC_ELIGIBLE in blend-validation.
2. **Checkpoint 2 (rehearsal):** fresh clone, follow this README, run the demo twice.

## TODO per person

### Person A
- [ ] Push this scaffold to GitHub first (everyone clones it)
- [ ] `auth-service`: POST /auth/login, HS256 JWT with `sub`, `roles`, `exp`; users `proc1` (processor), `rev1` (certificationReviewer)
- [ ] Gateway: JWT validation (signature + expiry), explicit CORS origins, TLS keystore for the demo
- [ ] Actuator + correlationId in logs (filter exists in `common`), redacted audit logs
- [ ] Two-instance failover demo; small load check (k6/JMeter) with p95 latency + error rate
- [ ] README final, service diagram, OAuth2 issuer explanation paragraph

### Person B
- [ ] `certificate-service`: entity, endpoints, `GET /certificates/validity?lotIds=&on=`, `data.sql` (lots A,B,C,D as in docs/contracts.md)
- [ ] `lot-ancestry-service`: Lot, Blend, BlendInput; POST /lots, POST /blends; publish `BlendCreatedEvent` (key = blendId)
- [ ] Record-level access (processor sees only own lots, else 403)
- [ ] Postman collection, event schema doc, 6 user stories

### Person C
- [ ] Domain rule class + JUnit 5/AssertJ tests (all valid, one uncertified, expiry boundary, expired, revoked, empty input)
- [ ] `@KafkaListener`, `processed_events` (eventId unique), call certificate-service by Eureka name, persist result, GET /validations/blends/{id}
- [ ] Dead-letter topic, replay demo, duplicate-event test
- [ ] SpringBootTest + EmbeddedKafka, DataJpaTest, contract tests (one that fails on purpose)
- [ ] GitHub Actions green run, test review notes, retrospective, screenshots

## Git rules
Branches: `a/platform`, `b/producer`, `c/consumer`. Pull `main` before each block, small commits, merge to `main` often.
