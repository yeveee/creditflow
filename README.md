# CreditFlow — Nexus Bank

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen)
![Angular](https://img.shields.io/badge/Angular-20-red)
![Tests](https://img.shields.io/badge/tests-43%20passing-success)
![Coverage](https://img.shields.io/badge/coverage-96%25-success)

Microservice de gestion des demandes de crédit, avec interface web dédiée.

CreditFlow couvre le cycle de vie complet d'un dossier de crédit — soumission, scoring automatisé, instruction, décision — avec authentification JWT, contrôle d'accès par rôle (`CLIENT` / `ANALYSTE` / `DIRECTEUR`), notifications RabbitMQ et journalisation immuable des décisions.

---

## Structure du dépôt

```
.
├── creditflow/   API back-end — Java 21, Spring Boot 4.0.3      → creditflow/README.md
├── frontend/     Interface web — Angular 20                     → frontend/README.md
└── docs/         Dossier de conception et développement          → docs/BLOC2_Conception_Developpement.md
```

---

## Démarrage rapide

```bash
# 1. Services tiers (PostgreSQL + RabbitMQ)
cd creditflow
docker compose up -d

# 2. Back-end
mvn spring-boot:run

# 3. Front-end (nouveau terminal)
cd ../frontend
npm install
npm start
```

| Service | URL |
|---|---|
| Interface web | http://localhost:4200 |
| API | http://localhost:8080 |
| Documentation API (Swagger) | http://localhost:8080/swagger-ui.html |

Comptes de démonstration (détail dans [frontend/README.md](frontend/README.md)) :

| Utilisateur | Rôle | Mot de passe |
|---|---|---|
| `client1` | CLIENT | `password123` |
| `analyste1` | ANALYSTE | `password123` |
| `directeur1` | DIRECTEUR | `password123` |

---

## Stack technique

| Composant | Technologies |
|---|---|
| **Back-end** (`creditflow/`) | Java 21, Spring Boot 4.0.3, PostgreSQL, RabbitMQ, Spring Security (JWT), MapStruct, Resilience4j |
| **Front-end** (`frontend/`) | Angular 20 (standalone, signals), TypeScript, SCSS |
| **CI/CD** | GitHub Actions (`.github/workflows/ci.yml`), Dependabot |

## Fonctionnalités

- Soumission et instruction des demandes de crédit (machine à états)
- Scoring externe avec circuit breaker (Resilience4j)
- Authentification JWT et contrôle d'accès par rôle
- Notifications RabbitMQ et journalisation immuable des décisions
- Monitoring : Actuator, Micrometer, Prometheus
- Interface web accessible (RGAA 4.1)

## Qualité

- 43 tests unitaires et d'intégration — couverture JaCoCo 96,1 % (instructions) / 95,4 % (lignes)
- Pipeline CI : compilation, tests, build de l'image Docker à chaque `push`/`pull request` sur `main`
- Historique de versions SemVer, tags Git annotés `v0.1.0` → `v1.0.0` (`git tag -l -n1`)

---

## Documentation

| Document | Contenu |
|---|---|
| [creditflow/README.md](creditflow/README.md) | API back-end : endpoints, architecture, tests, Docker |
| [frontend/README.md](frontend/README.md) | Interface Angular : démarrage, comptes de démo, structure |
| [docs/BLOC2_Conception_Developpement.md](docs/BLOC2_Conception_Developpement.md) | Dossier de conception et développement complet |
