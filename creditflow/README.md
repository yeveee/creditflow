# CreditFlow — Microservice de Gestion des Demandes de Crédit

**Projet :** CreditFlow  
**Organisation :** Nexus Bank — Direction des Systèmes d'Information  
**Stack :** Java 21, Spring Boot 4.0.3, PostgreSQL, RabbitMQ, Docker  

---

## Description

CreditFlow est un microservice Java/Spring Boot conçu pour gérer le cycle de vie complet d'une demande de crédit (soumission, instruction, scoring, décision). Il respecte l'architecture **SOL V1** : isolation stricte des couches, 3 types de modèles (API / Interne / Accesseur), MapStruct pour le mapping, et aucune dépendance partagée.

---

## Fonctionnalités

| ID  | Fonctionnalité                          | Statut |
|-----|-----------------------------------------|--------|
| F01 | Soumettre une demande de crédit         | ✅      |
| F02 | Lister les demandes (paginé)            | ✅      |
| F03 | Consulter une demande                   | ✅      |
| F04 | Changer le statut (machine à états)     | ✅      |
| F05 | Scoring externe + Circuit Breaker       | ✅      |
| F06 | Authentification JWT + RBAC             | ✅      |
| F07 | Journalisation immuable des décisions   | ✅      |
| F08 | Notifications RabbitMQ                  | ✅      |
| F10 | Monitoring Actuator + Prometheus        | ✅      |

---

## Architecture SOL V1

```
com.nexusbank.creditflow/
├── api/            ← Couche API (contrôleurs, modèles API, mappeurs API)
├── service/        ← Couche Métier (services, modèles internes)
├── isolation/      ← Couche Isolation (accès DB, scoring externe, mappeurs accesseur)
└── commun/         ← Interfaces communes (ModeleApi, ModeleInterne, ModeleAccesseur, MappeurUtils)
```

---

## Prérequis

- Java JDK 21
- Maven 3.9+
- Docker & Docker Compose

---

## Démarrage Rapide

### 1. Démarrer les services tiers

```bash
docker compose up -d
```

### 2. Lancer l'application

```bash
cd creditflow
mvn spring-boot:run
```

### 3. Vérifier la santé

```bash
curl http://localhost:8080/actuator/health
# → {"status":"UP"}
```

### 4. Accéder à Swagger UI

```
http://localhost:8080/swagger-ui.html
```

---

## Dépannage

**`error: release version 21 not supported` au lancement de `mvn spring-boot:run`**

Maven compile avec le JDK qu'il trouve dans son environnement — pas forcément celui renvoyé par `java -version` si plusieurs JDK sont installés sur la machine.

1. Vérifier le JDK réellement utilisé par Maven : `mvn -version` → ligne `Java version: ...` (doit afficher `21.x`)
2. Si ce n'est pas le cas, installer un JDK 21 (ex. Temurin : `brew install temurin@21` sur Mac, ou via [adoptium.net](https://adoptium.net))
3. Faire pointer `JAVA_HOME` dessus — macOS : `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`
4. Revérifier `mvn -version`, puis relancer `mvn spring-boot:run`

---

## Endpoints REST

| Méthode | Endpoint                        | Rôle requis          | Description                    |
|---------|---------------------------------|----------------------|--------------------------------|
| `POST`  | `/api/v1/auth/login`            | PUBLIC               | Authentification, retour JWT   |
| `POST`  | `/api/v1/demandes`              | CLIENT               | Soumettre une demande          |
| `GET`   | `/api/v1/demandes`              | ANALYSTE, DIRECTEUR, CLIENT | Lister les demandes (paginé) — complète pour le staff, scopée au client appelant sinon |
| `GET`   | `/api/v1/demandes/{id}`         | ANALYSTE, CLIENT     | Détail d'une demande           |
| `PATCH` | `/api/v1/demandes/{id}/statut`  | ANALYSTE, DIRECTEUR  | Changer le statut              |
| `GET`   | `/actuator/health`              | PUBLIC               | Santé du service               |
| `GET`   | `/actuator/prometheus`          | PUBLIC               | Métriques Prometheus           |

---

## Machine à États

```
SOUMISE → EN_INSTRUCTION → SCORING_EN_COURS → APPROUVEE
                                             → REFUSEE
                                             → EN_ATTENTE_PIECES → EN_INSTRUCTION
```

---

## Tests

```bash
cd creditflow
mvn test
```

43 tests unitaires et d'intégration (couverture JaCoCo : 96%, `mvn verify -Pcoverage`) couvrant :
- `DemandeCreditServiceTest` — service métier (4 tests)
- `StatutTransitionValidatorTest` — transitions d'état (2 tests)
- `JwtServiceTest` — JWT génération/validation (4 tests)
- `NotificationPublisherTest` — notifications RabbitMQ (1 test)
- `MappeurParametreDemandeTest` — mapping API → Interne (2 tests)
- `ApplicationTests` — chargement du contexte Spring (1 test)
- `GlobalExceptionHandlerTest` — mapping des exceptions métier (3 tests)
- `JwtAuthenticationFilterTest` — filtre d'authentification (4 tests)
- `AuthControllerTest` — endpoint de login (3 tests)
- `DemandeCreditControllerTest` — endpoints REST, dont contrôle anti-IDOR (9 tests)
- `DbIsolationManagerTest` — accès aux données, audit (8 tests)
- `ScoringIsolationManagerTest` — scoring externe, circuit breaker (2 tests)

---

## Docker

### Build

```bash
docker build -t creditflow:latest .
```

### Run

```bash
docker run -p 8080:8080 creditflow:latest
```

---

## CI/CD

Pipeline GitHub Actions (`.github/workflows/ci.yml`) :
1. Checkout code
2. Setup JDK 21
3. `mvn clean verify` (compile + tests)
4. Build Docker image

---

## Technologies

| Composant      | Technologie                     |
|----------------|---------------------------------|
| Langage        | Java 21                         |
| Framework      | Spring Boot 4.0.3               |
| Persistance    | PostgreSQL + Spring Data JPA    |
| Messaging      | RabbitMQ (Spring AMQP)          |
| Mapping        | MapStruct                       |
| Sécurité       | Spring Security + JWT (JJWT)    |
| Résilience     | Resilience4j (Circuit Breaker)  |
| Monitoring     | Actuator + Micrometer + Prometheus |
| Logging        | Logback (JSON structuré)        |
| Build          | Maven                           |
| Conteneurs     | Docker (multi-stage)            |
| CI/CD          | GitHub Actions                  |
