# BLOC 2 — Concevoir et Développer des Applications Logicielles

**Projet :** CreditFlow — Microservice de Gestion des Demandes de Crédit  
**Candidat :** Yevhenii Bondarenko  
**Date :** Mars 2026  
**Révision :** 22/07/2026 — mise à jour des sections C2.1.1, C2.2.3 et C2.2.4 suite à une vérification du dossier en conditions réelles (build, tests, couverture JaCoCo, exécution back-end et front-end)

---

## Périmètre du Dossier et Architecture Générale

Le projet **CreditFlow** est livré en deux composants complémentaires, versionnés dans le même dépôt :

| Composant | Rôle | Technologies |
|-----------|------|--------------|
| **API (back-end)** | Microservice métier : soumission, scoring, machine à états, sécurité JWT/RBAC | Java 21, Spring Boot 4.0.3, PostgreSQL, RabbitMQ |
| **Interface web (front-end)** | Client des utilisateurs (client, analyste, directeur) : authentification, soumission, liste et détail des demandes | Angular 20 (*standalone*, *signals*), TypeScript, SCSS |

Le front-end (`frontend/`) consomme l'API (`creditflow/`) via l'URL `/api/v1`. En développement, un *proxy* redirige `/api` et `/actuator` vers `http://localhost:8080`, ce qui évite toute configuration CORS. Les contrôles d'accès du front (guards, intercepteur JWT) **reproduisent fidèlement** la matrice RBAC du back-end (`SecurityConfig`).

## Correspondance avec la Grille d'Évaluation

| Compétence | Livrable attendu | Où le trouver |
|------------|------------------|---------------|
| **C2.1.1** | Protocole de déploiement continu ; critères de qualité/performance | § C2.1.1 |
| **C2.1.2** | Protocole d'intégration continue | § C2.1.2 |
| **C2.2.1** | Architecture maintenable ; prototype ; framework et paradigmes | § C2.2.1 (SOL V1 + prototype front Angular) |
| **C2.2.2** | Jeu de tests unitaires couvrant une fonctionnalité | § C2.2.2 |
| **C2.2.3** | Mesures de sécurité (OWASP) ; accessibilité (référentiel) | § C2.2.3 (OWASP Top 10 + RGAA 4.1 implémenté) |
| **C2.2.4** | Historique des versions ; dernière version fonctionnelle | § C2.2.4 |
| **C2.3.1** | Cahier de recettes | § C2.3.1 |
| **C2.3.2** | Plan de correction des bogues | § C2.3.2 |
| **C2.4.1** | Manuels de déploiement, d'utilisation et de mise à jour | § C2.4.1 |

---

## C2.1.1 — Environnements de Déploiement et de Test

### Environnement de Développement

| Outil | Rôle | Version |
|-------|------|---------|
| IntelliJ IDEA Ultimate | IDE principal (refactoring, débogage, MapStruct support) | 2024.x |
| Java JDK | Compilation et exécution | 21 LTS |
| Maven | Gestion des dépendances et build | 3.9.x |
| Docker Desktop | Conteneurisation des services tiers | 24.x |
| Docker Compose | Orchestration locale (PostgreSQL, RabbitMQ) | 2.24.x |
| Git | Gestion de versions | 2.43 |
| GitHub | Hébergement du dépôt + CI/CD | — |
| Postman | Tests manuels des endpoints REST | — |
| DBeaver | Inspection des données PostgreSQL | — |
| Node.js | *Runtime* de build du front-end Angular | 22 LTS |
| Angular CLI | *Scaffolding*, build (`ng build`) et serveur de dev (`ng serve`) | 20.x |
| npm | Gestion des dépendances front-end | 10.x |
| Visual Studio Code | Édition du front-end (TypeScript / HTML / SCSS) | — |

### Stack Technique de l'Application

```
┌─────────────────────────────────────────────┐
│           Spring Boot 4.0.3                 │
├───────────┬─────────────┬───────────────────┤
│ Spring    │ Spring Data │  Spring Security  │
│ Web MVC   │ JPA         │  (JWT / RBAC)     │
├───────────┴─────────────┴───────────────────┤
│ MapStruct 1.5.5 │ JUnit 5 │ Mockito 5       │
├─────────────────────────────────────────────┤
│ PostgreSQL │ RabbitMQ │ Resilience4j         │
└─────────────────────────────────────────────┘
```

### Protocole de Déploiement Continu

```
Séquence de déploiement (GitHub Actions) :

1. git push (branche main) ou Pull Request vers main
       │
       ▼
2. Checkout code + setup JDK 21 (Temurin)
       │
       ▼
3. mvn clean verify (compile + tests unitaires)
       │
       ├─► Échec compilation → Pipeline STOP
       ├─► Échec tests → Pipeline STOP
       │
       ▼
4. docker build -t creditflow:latest . (multi-stage)
       │
       ├─► Stage 1 : JDK 21 Alpine — compile + package
       ├─► Stage 2 : JRE 21 Alpine — image runtime légère
       │
       ▼
5. Image Docker prête pour déploiement
       │
       ▼
6. Health check Actuator /actuator/health → OK ?
        ├─► NON → rollback image précédente
        └─► OUI → déploiement validé
```

### Critères de Qualité et de Performance

| Critère | Outil de mesure | Seuil minimal | Mesure constatée |
|---------|-----------------|---------------|-------------------|
| Couverture de code | JaCoCo (plugin Maven `jacoco-maven-plugin`, profil opt-in `mvn verify -Pcoverage`, code MapStruct généré exclu de la mesure) | ≥ 80% (cible) | **96,1% des instructions / 95,4% des lignes** au 23/07/2026 — seuil dépassé. `SecurityConfig`, `DemandeCreditController`, `DbIsolationManager`, `JwtService`, `JwtAuthenticationFilter`, `AuthController`, `ScoringIsolationManager`, `StatutTransitionValidator`, `GlobalExceptionHandler`, `NotificationPublisher` : 100% ; `DemandeCreditService` : 87%. Seuls restent sous-couverts l'entité JPA `DemandeCreditEntity` (accesseurs générés), l'utilitaire `MappeurUtils` (une ligne) et la classe `Application` (méthode `main`) — code déclaratif ou point d'entrée, sans logique propre à tester |
| Bugs critiques | Revue de code (PR) | 0 bug bloquant | — |
| Vulnérabilités sécurité | Dependabot (GitHub) | 0 CVE critique | — |
| Temps de réponse P95 | Spring Actuator + Micrometer | < 2 000 ms | Non mesuré en charge à ce jour |
| Uptime service | Actuator /health | ≥ 99% | — |
| Taille image Docker | Docker inspect | < 200 MB | — |

### Environnement et Déploiement du Front-End

Le front-end Angular possède son propre cycle de build, découplé du back-end :

```bash
# Développement (rechargement à chaud + proxy vers l'API :8080)
npm start                     # -> http://localhost:4200

# Build de production (bundles optimisés, tree-shaking, hachage des fichiers)
npm run build                 # -> dist/frontend (assets statiques)
```

Le build produit des **assets statiques** (HTML/CSS/JS) déployables derrière un serveur web (Nginx) ou un CDN, indépendamment de l'API. Le *lazy-loading* des routes (chaque écran est un *chunk* séparé) garantit un premier chargement léger (≈ 90 kB transférés). Cette séquence s'insère dans le protocole de déploiement continu : build → artefacts statiques → publication → *health check*.

---

## C2.1.2 — Protocole d'Intégration Continue

### Fichier GitHub Actions `.github/workflows/ci.yml`

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: creditflow
          POSTGRES_USER: creditflow_user
          POSTGRES_PASSWORD: creditflow_pass
        ports:
          - 5432:5432
        options: >-
          --health-cmd "pg_isready -U creditflow_user -d creditflow"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Build with Maven
        run: mvn clean verify
        working-directory: creditflow

      - name: Build Docker image
        run: docker build -t creditflow:latest .
        working-directory: creditflow
```

Ce pipeline exécute automatiquement `mvn clean verify` qui compile le code et lance tous les tests unitaires (JUnit 5 + Mockito). Deux classes de test chargent le contexte Spring complet (`@SpringBootTest`) et ont donc besoin d'une base PostgreSQL réellement disponible : le job déclare un service `postgres` dédié à cet effet, avec health-check, pour que ces tests puissent se connecter dès le démarrage du conteneur. Si les tests échouent, le pipeline s'arrête et l'image Docker n'est pas construite.

### Stratégie de Branches (GitFlow)

```
main ────────────────────────────────────────────► production
  │
develop ─────────────────────────────────────────► intégration
  │
  ├── feature/F01-soumettre-demande
  ├── feature/F02-lister-demandes
  ├── feature/F05-scoring-externe
  └── hotfix/BUG-001-npe-statut
```

### Séquences d'Intégration

| Séquence | Déclencheur | Actions |
|----------|-------------|---------|
| **CI validation** | Pull Request vers `main` | Compile + Tests unitaires (`mvn clean verify`) |
| **CI + Build Docker** | Push sur `main` | Compile + Tests + Build image Docker (`docker build`) |

---

## C2.2.1 — Prototype de l'Application

### Architecture Logicielle (Rappel Structure SOL V1)

Le prototype respecte intégralement la structure SOL V1 détaillée dans le BLOC 1. Chaque couche est strictement isolée :

```
com.nexusbank.creditflow/
├── api/                              ← Couche API
│   ├── auth/                         (AuthController, LoginRequest)
│   ├── demande/                      (DemandeCreditController)
│   │   ├── mappeur/                  (MappeurParametreDemande, MappeurReponseDemande)
│   │   └── modele/                   (DemandeCreditApi, ChangementStatutApi)
│   ├── config/                       (RabbitMQConfig, WebClientConfig)
│   └── security/                     (SecurityConfig, JwtAuthenticationFilter)
├── service/                          ← Couche Métier
│   └── credit/
│       ├── DemandeCreditService.java
│       ├── JwtService.java
│       ├── StatutTransitionValidator.java
│       ├── NotificationPublisher.java
│       └── modele/                   (DemandeCreditInterne, StatutDemande, Role, ScoreResultatInterne)
├── isolation/                        ← Couche Isolation
│   ├── db/                           (DbIsolationManager, repositories, mappeurs DB, entities)
│   └── scoring/                      (ScoringIsolationManager, mappeurs scoring, modèles accesseur)
└── commun/                           ← Interfaces communes
    ├── mappeur/                      (Mappeur, MappeurUtils, interfaces de base)
    └── modele/                       (ModeleApi, ModeleInterne, ModeleAccesseur)
```

- **Couche API** : ne connaît que les `ModeleApi` et les `MappeurApi`
- **Couche Service** : ne connaît que les `ModeleInterne` et les `IsolationManager`
- **Couche Isolation** : ne connaît que les `ModeleAccesseur` et les accesseurs (JPA Repository, WebClient)

### User Stories Couvertes par le Prototype

| ID | En tant que | Je veux | Afin de |
|----|-------------|---------|---------|
| US01 | Client bancaire | Soumettre une demande de crédit | Initier mon dossier |
| US02 | Analyste crédit | Voir toutes les demandes en attente | Prioriser mon travail |
| US03 | Analyste crédit | Consulter le détail d'une demande | Instruire le dossier |
| US04 | Analyste crédit | Changer le statut d'une demande | Faire avancer le dossier |
| US05 | Système | Appeler le scoring externe | Décision automatique |
| US06 | Client bancaire | M'authentifier | Accéder à mon espace sécurisé |
| US07 | Directeur agence | Approuver/refuser un dossier > seuil | Exercer mon autorité de validation |
| US08 | Client bancaire | Consulter la liste de mes propres demandes | Suivre l'avancement de mes dossiers sans en connaître les identifiants |

### Endpoints REST Exposés

| Méthode | Endpoint | Rôle requis | Description |
|---------|----------|-------------|-------------|
| `POST` | `/api/v1/auth/login` | PUBLIC | Authentification, retour JWT |
| `POST` | `/api/v1/demandes` | CLIENT | Soumettre une nouvelle demande |
| `GET` | `/api/v1/demandes` | ANALYSTE, DIRECTEUR, CLIENT | Lister les demandes (paginé) — vue complète pour ANALYSTE/DIRECTEUR, scopée aux seules demandes de l'appelant pour CLIENT |
| `GET` | `/api/v1/demandes/{id}` | ANALYSTE, CLIENT | Détail d'une demande |
| `PATCH` | `/api/v1/demandes/{id}/statut` | ANALYSTE, DIRECTEUR | Changer le statut |
| `GET` | `/actuator/health` | PUBLIC (résumé sans détail) | Santé du service |
| `GET` | `/actuator/prometheus` | Authentifié | Métriques Prometheus |

### Bonnes Pratiques Respectées

- **SOL V1** : isolation stricte, pas de `@Wrapper`, constructeurs `@Autowired` uniquement
- **MapStruct** : tous les mappings via interfaces, injectés via `MappeurUtils`
- **Optional** : tous les attributs nullables en `Optional<T>` dans `ModeleInterne`
- **Validation** : Bean Validation sur tous les `ModeleApi` (`@NotNull`, `@NotBlank`, `@DecimalMin`, `@Min`)
- **Pagination** : `Pageable` sur l'endpoint de liste `GET /api/v1/demandes`
- **OpenAPI** : Swagger UI disponible sur `/swagger-ui.html` via Springdoc OpenAPI
- **Lombok** : `@Data`, `@Builder(toBuilder = true)`, `@NoArgsConstructor`, `@AllArgsConstructor`

### Exigences de Sécurité du Prototype

- Tous les endpoints protégés sauf `/api/v1/auth/**`, `/swagger-ui/**`, `/api-docs/**` et `/actuator/health` (résumé public, détails réservés aux utilisateurs authentifiés) ; le reste d'`/actuator/**` (Prometheus, metrics) exige une authentification
- Contrôle de propriété (anti-IDOR) sur `GET /api/v1/demandes/{id}` : un CLIENT ne peut consulter que ses propres demandes
- Token JWT requis dans le header `Authorization: Bearer <token>`
- Rôles RBAC : `CLIENT`, `ANALYSTE`, `DIRECTEUR` — contrôle d'accès par endpoint et méthode HTTP
- Validation d'entrée systématique via Bean Validation (aucun champ non validé en entrée API)
- Sessions désactivées (`STATELESS`) — authentification purement par token

### Prototype de l'Interface Web (Front-End Angular)

Un **prototype fonctionnel** d'interface web a été développé pour permettre aux utilisateurs métier d'exploiter le microservice sans outil technique. Il couvre l'ensemble des *user stories* principales (US01 à US07).

#### Stack et paradigmes de développement

- **Framework** : Angular 20 en architecture **100 % *standalone*** (sans NgModules), rendu réactif par **signals**.
- **Paradigmes** : composants réutilisables, **injection de dépendances**, formulaires réactifs (`ReactiveForms`), programmation réactive (RxJS / `HttpClient`), *lazy-loading* des routes, séparation *core / features / shared / layout*.
- **Langage & style** : TypeScript strict, *design system* SCSS maison (thème « fintech » indigo, police Inter).

#### Architecture front-end (maintenabilité)

```
src/app/
├── core/            services (Auth, Demande, Toast), intercepteur JWT, guards, modèles, utilitaires
├── layout/shell/    barre de navigation + <router-outlet>
├── shared/          composants transverses (badge de statut, actions de statut)
└── features/        écrans : auth/login, demandes (liste / création / détail), home, erreurs
```

#### Écrans et composants d'interface

| Écran | *User stories* | Composants d'interface (fenêtres, boutons, menus…) |
|-------|----------------|-----------------------------------------------------|
| **Connexion** (`/login`) | US06 | Formulaire (identifiant, mot de passe avec bouton afficher/masquer), bouton *Se connecter*, boutons de comptes de démonstration, messages d'erreur |
| **Liste des demandes** (`/demandes`) | US02, US04, US07, US08 | Menu de navigation (« Demandes » pour ANALYSTE/DIRECTEUR, « Mes demandes » pour CLIENT — scopée à ses propres dossiers), tableau paginé avec badges de statut colorés, boutons *Détails* (ANALYSTE, CLIENT) / *Statut* (ANALYSTE, DIRECTEUR), **fenêtre modale** de gestion du statut, pagination |
| **Nouvelle demande** (`/demandes/nouvelle`) | US01 | Formulaire validé (montant, durée, emprunteur), aides de saisie, boutons *Soumettre* / *Annuler* |
| **Détail d'une demande** (`/demandes/:id`) | US03, US05 | Fil de progression (*stepper*) de la machine à états, cartes d'information, barre de score, boutons de transition de statut |

L'interface est **responsive** (adaptation mobile/desktop) et fournit un retour utilisateur systématique (états de chargement, notifications *toast*, désactivation des boutons pendant les appels réseau).

#### Sécurité du prototype (côté client)

- **Intercepteur HTTP JWT** : ajoute automatiquement l'en-tête `Authorization: Bearer <token>` ; en cas de réponse `401`, purge la session et redirige vers `/login`.
- **Guards de routage** : `authGuard` (utilisateurs authentifiés) et `roleGuard(...rôles)` (contrôle par rôle), dont la matrice reproduit exactement le `SecurityConfig` du back-end — `CLIENT` → soumission + liste **scopée à ses propres demandes** + détail ; `ANALYSTE` → liste complète + détail + changement de statut ; `DIRECTEUR` → liste complète + changement de statut.
- **Défense en profondeur** : le front masque les actions non autorisées, mais la décision de sécurité **fait toujours autorité côté serveur** (`401`/`403`). Le token est stocké en `localStorage` puis décodé pour l'affichage du rôle.

---

## C2.2.2 — Harnais de Tests Unitaires

### Stratégie de Test

```
┌──────────────────────────────────────────────────────┐
│                 Pyramide de Tests                    │
│                                                      │
│              ┌────────────┐                          │
│              │  E2E (5%)  │  Postman / Newman        │
│           ┌──┴────────────┴──┐                       │
│           │ Intégration (15%)│  @SpringBootTest       │
│        ┌──┴──────────────────┴──┐                    │
│        │   Unitaires (80%)      │  JUnit5 + Mockito  │
│        └────────────────────────┘                    │
└──────────────────────────────────────────────────────┘
```

### Classes de Tests Unitaires

#### 1. Tests du Mappeur MapStruct — `MappeurParametreDemandeTest`

Teste le mapping `DemandeCreditApi → DemandeCreditInterne` en vérifiant que tous les champs sont correctement transformés et que les valeurs nulles sont encapsulées en `Optional.empty()`.

```java
@SpringBootTest
public class MappeurParametreDemandeTest {

    @Autowired
    private MappeurParametreDemande mapper;

    @Test
    void shouldMapApiToInterne() {
        // Given
        DemandeCreditApi api = DemandeCreditApi.builder()
                .montant(new BigDecimal("15000.00"))
                .dureeMois(24)
                .nomEmprunteur("Jean Dupont")
                .build();

        // When
        DemandeCreditInterne interne = mapper.map(api);

        // Then
        assertNotNull(interne);
        assertEquals(new BigDecimal("15000.00"), interne.getMontant().orElse(null));
        assertEquals(24, interne.getDureeMois().orElse(null));
        assertEquals("Jean Dupont", interne.getNomEmprunteur().orElse(null));
    }

    @Test
    void shouldWrapNullValuesInOptional() {
        DemandeCreditApi api = DemandeCreditApi.builder().build();
        DemandeCreditInterne interne = mapper.map(api);
        assertNotNull(interne);
        assertFalse(interne.getMontant().isPresent());
    }
}
```

#### 2. Tests du Service — `DemandeCreditServiceTest`

Teste le service métier principal avec 4 mocks (`DbIsolationManager`, `ScoringIsolationManager`, `StatutTransitionValidator`, `NotificationPublisher`). Chaque dépendance est isolée via `@Mock` et injectée dans le service via `@InjectMocks`.

```java
@ExtendWith(MockitoExtension.class)
public class DemandeCreditServiceTest {

    @Mock private DbIsolationManager dbIsolationManager;
    @Mock private ScoringIsolationManager scoringIsolationManager;
    @Mock private StatutTransitionValidator statutTransitionValidator;
    @Mock private NotificationPublisher notificationPublisher;

    @InjectMocks
    private DemandeCreditService service;

    @Test
    void shouldCreateDemandeWithStatusSoumise() {
        // Given
        DemandeCreditInterne saved = demandeTest.toBuilder()
                .id(Optional.of(1L)).build();
        DemandeCreditInterne avecScore = saved.toBuilder()
                .scoreCredit(Optional.of(750))
                .risqueCredit(Optional.of("FAIBLE")).build();

        when(dbIsolationManager.save(any(DemandeCreditInterne.class)))
                .thenReturn(saved).thenReturn(avecScore);
        when(scoringIsolationManager.calculerScore("Marie Martin"))
                .thenReturn(new ScoreResultatInterne(750, "FAIBLE"));

        // When
        DemandeCreditInterne result = service.creerDemande(demandeTest);

        // Then
        assertNotNull(result);
        assertEquals(750, result.getScoreCredit().orElse(null));
        verify(dbIsolationManager, times(2)).save(any());
        verify(scoringIsolationManager).calculerScore("Marie Martin");
    }

    @Test
    void shouldChangeStatutSuccessfully() {
        // Given
        when(dbIsolationManager.findById(1L)).thenReturn(Optional.of(demande));
        when(dbIsolationManager.updateStatut(1L, "EN_INSTRUCTION"))
                .thenReturn(Optional.of(updated));

        // When
        Optional<DemandeCreditInterne> result =
                service.changerStatut(1L, StatutDemande.EN_INSTRUCTION, "analyste1");

        // Then
        assertTrue(result.isPresent());
        assertEquals(StatutDemande.EN_INSTRUCTION, result.get().getStatut());
        verify(statutTransitionValidator).valider(StatutDemande.SOUMISE, StatutDemande.EN_INSTRUCTION);
        verify(notificationPublisher).publierChangementStatut(1L, "EN_INSTRUCTION");
        verify(dbIsolationManager).auditerChangementStatut(1L, "EN_INSTRUCTION", "analyste1");
    }
}
```

#### 3. Tests de la Machine à États — `StatutTransitionValidatorTest`

Teste les transitions autorisées et interdites sans mock (la classe n'a aucune dépendance externe).

```java
public class StatutTransitionValidatorTest {

    private final StatutTransitionValidator validator = new StatutTransitionValidator();

    @Test
    void shouldAllowValidTransition() {
        assertDoesNotThrow(() ->
            validator.valider(StatutDemande.SOUMISE, StatutDemande.EN_INSTRUCTION));
    }

    @Test
    void shouldRejectInvalidTransition() {
        assertThrows(IllegalStateException.class, () ->
            validator.valider(StatutDemande.SOUMISE, StatutDemande.APPROUVEE));
    }
}
```

#### 4. Tests JWT — `JwtServiceTest`

Teste la génération de token, l'extraction du username et du rôle, la validation, et le rejet d'un token invalide.

```java
public class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void shouldGenerateAndExtractUsername() {
        String token = jwtService.genererToken("jean", Role.ANALYSTE);
        assertEquals("jean", jwtService.extraireUsername(token));
    }

    @Test
    void shouldExtractRole() {
        String token = jwtService.genererToken("jean", Role.ANALYSTE);
        assertEquals("ANALYSTE", jwtService.extraireRole(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertFalse(jwtService.isTokenValide("invalid-token"));
    }
}
```

#### 5. Tests RabbitMQ — `NotificationPublisherTest`

Vérifie que `NotificationPublisher` envoie le bon message JSON à la queue RabbitMQ via un `RabbitTemplate` mocké.

```java
@ExtendWith(MockitoExtension.class)
public class NotificationPublisherTest {

    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private NotificationPublisher notificationPublisher;

    @Test
    void shouldSendMessageToQueue() {
        notificationPublisher.publierChangementStatut(1L, "EN_INSTRUCTION");

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.QUEUE_STATUT_CHANGE,
                "{\"demandeId\": 1, \"nouveauStatut\": \"EN_INSTRUCTION\"}");
    }
}
```

#### 6. Test de Sécurité — Contrôle Anti-IDOR (`DemandeCreditControllerTest`)

Vérifie que `GET /api/v1/demandes/{id}` refuse à un CLIENT la consultation d'une demande dont il n'est pas l'auteur, et l'autorise pour un ANALYSTE quel que soit le propriétaire.

```java
@ExtendWith(MockitoExtension.class)
public class DemandeCreditControllerTest {

    @Mock private DemandeCreditService service;
    @Mock private Authentication authentication;

    @Test
    void otherClientCannotViewSomeoneElsesDemande() {
        DemandeCreditController controller = new DemandeCreditController(service, new MappeurUtils());
        when(service.obtenirDemande(1L)).thenReturn(Optional.of(demande(1L, "client1")));
        when(authentication.getName()).thenReturn("client2");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))).when(authentication).getAuthorities();

        assertThrows(AccessDeniedException.class, () -> controller.obtenirDemande(1L, authentication));
    }

    @Test
    void analysteCanViewAnyDemandeRegardlessOfOwnership() {
        DemandeCreditController controller = new DemandeCreditController(service, new MappeurUtils());
        when(service.obtenirDemande(1L)).thenReturn(Optional.of(demande(1L, "client1")));
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ANALYSTE"))).when(authentication).getAuthorities();

        assertEquals(HttpStatus.OK, controller.obtenirDemande(1L, authentication).getStatusCode());
    }
}
```

### Plan de Couverture

| Classe testée | Classe de test | Nb tests | Type |
|---------------|----------------|----------|------|
| `MappeurParametreDemande` | `MappeurParametreDemandeTest` | 2 | Intégration (`@SpringBootTest`) |
| `DemandeCreditService` | `DemandeCreditServiceTest` | 4 | Unitaire (Mockito) |
| `StatutTransitionValidator` | `StatutTransitionValidatorTest` | 2 | Unitaire (sans mock) |
| `JwtService` | `JwtServiceTest` | 4 | Unitaire (sans mock) |
| `NotificationPublisher` | `NotificationPublisherTest` | 1 | Unitaire (Mockito) |
| `Application` (contexte Spring) | `ApplicationTests` | 1 | Intégration (`@SpringBootTest`) |
| `GlobalExceptionHandler` | `GlobalExceptionHandlerTest` | 3 | Unitaire (sans mock) |
| `JwtAuthenticationFilter` | `JwtAuthenticationFilterTest` | 4 | Unitaire (Mockito) |
| `AuthController` | `AuthControllerTest` | 3 | Unitaire (Mockito) |
| `DemandeCreditController` | `DemandeCreditControllerTest` | 9 | Unitaire (Mockito) — dont le contrôle anti-IDOR |
| `DbIsolationManager` | `DbIsolationManagerTest` | 8 | Unitaire (Mockito + mappeurs réels) |
| `ScoringIsolationManager` | `ScoringIsolationManagerTest` | 2 | Unitaire (WebClient mocké) |
| **Total back-end** | **12 classes de test** | **43 tests, tous passants** | **Couverture JaCoCo mesurée : 96,1% (instructions) / 95,4% (lignes) — voir détail par classe en C2.1.1** |

> **Côté front-end**, l'échafaudage de tests **Jasmine/Karma** est en place (`ng test`) avec un test de montage du composant racine (`app.spec.ts`). Les services (`AuthService`, `DemandeService`) et les *guards* sont conçus pour être testables unitairement (dépendances injectées, logique pure, pas d'accès direct au DOM).

---

## C2.2.3 — Sécurité et Accessibilité

### Couverture OWASP Top 10

| # | Catégorie OWASP | Risque dans CreditFlow | Mesure Implémentée |
|---|-----------------|------------------------|---------------------|
| A01 | Broken Access Control | Accès à des endpoints hors périmètre | Spring Security RBAC — restrictions par rôle (`hasRole("CLIENT")`, `hasAnyRole("ANALYSTE", "DIRECTEUR", "CLIENT")` selon l'endpoint) sur chaque endpoint via `SecurityConfig`. Contrôle de propriété (anti-IDOR) à double niveau : sur `GET /api/v1/demandes/{id}`, un CLIENT ne peut consulter que les demandes dont il est l'auteur (`clientUsername`, vérifié dans `DemandeCreditController`), sous peine de `403 Forbidden` ; sur `GET /api/v1/demandes` (liste), le même principe s'applique par filtrage serveur (`DbIsolationManager.findByClientUsername`) plutôt que par rejet — un CLIENT récupère uniquement ses propres demandes, jamais celles d'un tiers. ANALYSTE/DIRECTEUR voient l'intégralité du portefeuille sans cette restriction |
| A02 | Cryptographic Failures | Mots de passe en clair | Mots de passe hashés avec `BCryptPasswordEncoder` ; token JWT signé via `Keys.hmacShaKeyFor()` (algorithme HMAC sélectionné automatiquement selon la longueur de clé — HS384 avec la clé actuelle). La clé de signature (`jwt.secret`) et les identifiants de base de données sont externalisés via variables d'environnement (`JWT_SECRET`, `SPRING_DATASOURCE_*`), avec valeur par défaut de développement pour ne pas casser le démarrage local |
| A03 | Injection | Injection SQL via champs API | Requêtes paramétrées JPA (Spring Data), Bean Validation `@NotNull`, `@NotBlank`, `@DecimalMin`, `@Min` sur tous les `ModeleApi` |
| A04 | Insecure Design | Workflow de décision contournable | Machine à états stricte (`StatutTransitionValidator`) — transitions validées côté serveur ; une transition interdite renvoie désormais `409 Conflict` avec message explicite (`GlobalExceptionHandler`) plutôt qu'une erreur serveur générique |
| A05 | Security Misconfiguration | Endpoints non protégés | CSRF désactivé (API stateless), sessions `STATELESS`, seuls les endpoints publics explicitement autorisés dans `SecurityConfig`. Identifiants de base de données externalisés (voir A02). Seul `/actuator/health` reste public, et en mode `show-details=when-authorized` (un appel anonyme ne voit que `{"status":"UP"}`, le détail des composants n'apparaît qu'authentifié) ; `/actuator/prometheus` et `/actuator/metrics` exigent désormais une authentification |
| A06 | Vulnerable Components | Dépendances obsolètes | Dependabot configuré (`.github/dependabot.yml`) — surveillance hebdomadaire Maven + Docker |
| A07 | Auth Failures | Token falsifié ou expiré | Validation JWT systématique via `JwtAuthenticationFilter`, rejet des tokens expirés ou avec signature invalide |
| A08 | Software Integrity | Image Docker compromise | Dockerfile multi-stage (build séparé du runtime), `.dockerignore` pour exclure `.git/`, code source non présent dans l'image finale |
| A09 | Logging Failures | Pas de trace des décisions | Journalisation immuable de chaque changement de statut dans la table `decision_audit` (qui, quand, ancien/nouveau statut), déclenchée explicitement par `DemandeCreditController` → `DemandeCreditService.changerStatut` → `DbIsolationManager.auditerChangementStatut` (appel direct, sans AOP — voir note d'architecture ci-dessous) |
| A10 | SSRF | Appel scoring vers URL arbitraire | URL du bureau de crédit fixée en dur dans `ScoringIsolationManager` (`http://localhost:8081`), non paramétrable par l'utilisateur |

> **Note d'architecture (A09) :** la journalisation d'audit a d'abord été implémentée via Spring AOP (`@AfterReturning`), cohérent avec l'esprit cross-cutting-concern de l'architecture. Cette implémentation a ensuite été remplacée par un appel direct (contrôleur → service → `DbIsolationManager`) après avoir constaté, à l'exécution, des échecs de build intermittents. Investigation faite : la cause n'était finalement pas l'AOP elle-même, mais une interférence de build sur la machine de développement — le serveur de langage Java de l'IDE recompile en tâche de fond dans le même dossier `target/classes` que Maven, et peut ponctuellement y écrire une classe partiellement compilée pendant qu'un `mvn verify` s'exécute (confirmé en suspendant temporairement le processus du serveur de langage : le taux d'échec tombe alors à 0 sur plusieurs essais consécutifs). L'appel direct a été conservé malgré tout : il est plus simple, tout aussi correct, et n'a plus besoin d'activer le proxying AOP de Spring pour cette seule fonctionnalité.

### Validation des Entrées API (Bean Validation)

```java
// DemandeCreditApi.java — extrait du code source réel
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Modèle API pour une demande de crédit")
public class DemandeCreditApi implements ModeleApi {

    private Long id;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "1000.00", message = "Montant minimum : 1 000 €")
    private BigDecimal montant;

    @NotNull(message = "La durée est obligatoire")
    @Min(value = 6, message = "Durée minimale : 6 mois")
    private Integer dureeMois;

    @NotBlank(message = "Le nom de l'emprunteur est obligatoire")
    private String nomEmprunteur;

    private String statut;
    private LocalDateTime dateCreation;
    private Integer scoreCredit;
    private String risqueCredit;
}
```

### Accessibilité (RGAA 4.1)

#### Choix du référentiel

Le **RGAA 4.1** (Référentiel Général d'Amélioration de l'Accessibilité) a été retenu comme référentiel d'accessibilité pour les raisons suivantes :

- **Obligation légale** : le RGAA est le référentiel français de référence, imposé par l'article 47 de la loi n° 2005-102. Tout service numérique destiné au public doit s'y conformer.
- **Compatibilité WCAG** : le RGAA 4.1 est aligné sur les critères WCAG 2.1 niveau AA, assurant une conformité internationale.
- **Pertinence bancaire** : les clients de Nexus Bank incluent des personnes en situation de handicap qui doivent pouvoir accéder aux services financiers.

#### Application au projet

CreditFlow comprend désormais une **interface web dédiée (front-end Angular)** en complément de l'API REST. Le RGAA 4.1 est mis en œuvre concrètement, **en priorité sur l'interface web utilisateur** détaillée ci-dessous :

**1. Interface Web Angular — mesures RGAA 4.1 implémentées**

| Thème RGAA | Critères | Mesure implémentée dans le front-end |
|------------|----------|--------------------------------------|
| Langue | 8.3 | `lang="fr"` sur `<html>` |
| Structure | 8.5, 9.1 | Titre de page pertinent par vue (stratégie `title` du `Router`), hiérarchie de titres, points de repère `header` / `nav` / `main` |
| Navigation | 12.6, 12.7 | Lien d'évitement « Aller au contenu principal », `aria-current="page"` sur le lien actif, ordre de tabulation logique |
| Couleurs | 3.2, 3.3 | Contrastes ≥ 4.5:1 ; l'information n'est **jamais** portée par la seule couleur (statut = pastille **+ libellé texte**, risque = **texte** explicite) |
| Formulaires | 11.1, 11.10 | Chaque champ lié à son `<label for>` ; erreurs reliées par `aria-describedby` + `aria-invalid` + `role="alert"` |
| Boutons & liens | 6.1, 11.9 | Intitulés explicites ; boutons-icônes dotés d'`aria-label` (fermeture, afficher/masquer le mot de passe avec `aria-pressed`) |
| Scripts / ARIA | 7.1, 7.4 | Modale `role="dialog"` + `aria-modal` + fermeture par **Échap** ; régions *live* (`aria-live`) pour notifications, erreurs et chargements ; barre de score `role="progressbar"` |
| Tableaux | 5.6, 5.7 | En-têtes `<th scope="col">` et `<caption>` sur le tableau des demandes |
| Présentation | 10.7 / WCAG 2.3.3 | Focus clavier visible (`:focus-visible`), respect de `prefers-reduced-motion`, mise en page *responsive* |
| Images | 1.2 | Éléments décoratifs (logo, puces, icônes) neutralisés par `aria-hidden="true"` |

**2. Documentation API (Swagger UI — Springdoc OpenAPI)**

| Critère RGAA | Application | Statut |
|--------------|-------------|--------|
| 1.1 — Alternatives textuelles | Annotations `@Schema(description = ...)` sur tous les modèles API | ✅ |
| 3.2 — Contraste couleurs | Thème Swagger UI par défaut — contraste ≥ 4.5:1 | ✅ |
| 7.1 — Navigation clavier | Swagger UI entièrement navigable au clavier | ✅ |
| 8.1 — DOCTYPE valide | HTML5 généré par Springdoc | ✅ |
| 11.1 — Labels formulaires | Labels associés aux champs de requête Swagger | ✅ |

**3. API REST — Bonnes pratiques d'accessibilité**

| Mesure | Description |
|--------|-------------|
| Codes HTTP sémantiques | 200, 201, 400, 401, 403, 404 — permettent aux technologies d'assistance d'interpréter les réponses |
| Messages d'erreur explicites | Bean Validation retourne des messages en clair (ex : "Le montant est obligatoire") — lisibles par lecteurs d'écran |
| JSON structuré | Réponses cohérentes et prévisibles, facilitant le développement de clients accessibles |
| Documentation OpenAPI complète | Annotations `@Schema` sur tous les modèles, permettant la génération de documentation accessible |

#### Vérification de la conformité

Les mesures ci-dessus sont **vérifiables** : navigation complète au clavier (tabulation, `Échap`, focus visible), audit automatique via **Lighthouse / axe DevTools**, et contrôle des contrastes. Un test complémentaire au lecteur d'écran (VoiceOver / NVDA) est prévu en recette pour valider la restitution vocale des régions *live* et des messages d'erreur de formulaire.

---

## C2.2.4 — Gestion des Versions

### Stratégie de Versionnement Sémantique (SemVer)

```
MAJOR.MINOR.PATCH
  │      │     │
  │      │     └── Corrections de bugs (hotfix)
  │      └──────── Nouvelles fonctionnalités compatibles
  └─────────────── Ruptures de compatibilité API
```

### Historique des Versions

Les jalons ci-dessous correspondent à la progression réelle des commits Git (21 commits incrémentaux et descriptifs sur le back-end, consultables via `git log`), et sont désormais matérialisés par des tags Git annotés sur les commits correspondants :

| Version | Tag Git | Type | Changements |
|---------|---------|------|-------------|
| `0.1.0` | `v0.1.0` | MINOR | Structure SOL V1, modèles (Api/Interne/Accesseur), MapStruct, couche DB, endpoints POST/GET demandes |
| `0.2.0` | `v0.2.0` | MINOR | Scoring externe (WebClient + Resilience4j Circuit Breaker), machine à états (`StatutTransitionValidator`), PATCH statut |
| `0.3.0` | `v0.3.0` | MINOR | Authentification JWT (`JwtService`, `JwtAuthenticationFilter`), RBAC (`SecurityConfig`), endpoint login |
| `0.4.0` | `v0.4.0` | MINOR | Journalisation immuable des décisions, pagination des résultats, notifications RabbitMQ (`NotificationPublisher`) |
| `0.5.0` | `v0.5.0` | MINOR | Monitoring Actuator + Micrometer + Prometheus, structured logging JSON (Logback) |
| `1.0.0` | `v1.0.0` | MAJOR | Release finale — Dockerfile multi-stage, CI/CD GitHub Actions, Dependabot, documentation complète |

### Outils de Suivi des Versions

- **Tags Git annotés** sur les commits marquant chaque jalon (`git tag -a v0.1.0 <commit> -m "..."`, etc.) — consultables via `git log --oneline --decorate` ou `git tag -l -n1`
- **Historique Git** : commits incrémentaux et descriptifs, un par lot fonctionnel (`git log --oneline`)
- **Stratégie retenue** : SemVer (voir ci-dessus), appliquée à la numérotation des tags Git

**Point restant à traiter avant la soutenance :** les dossiers `docs/` (dont ce document) et `frontend/` ne sont, à ce jour, **pas encore suivis par Git** — leur ajout au dépôt (`git add` + commit) est nécessaire pour que l'historique de versions couvre l'intégralité du livrable, prototype front-end inclus. Un nouveau tag (`v1.1.0` par exemple) reste également à créer une fois ces ajouts commités, pour couvrir les correctifs de sécurité (IDOR, secrets, Actuator), les 43 tests et le correctif RBAC décrits dans ce document (voir C2.2.3 et C2.3.2, BUG-002). Les GitHub Releases (publication des tags avec artefacts JAR/image Docker) restent également à créer sur le dépôt distant.

### Logiciel Fonctionnel et Manipulable en Autonomie

La dernière version intègre l'**interface web Angular**, qui rend le logiciel **manipulable en autonomie par un utilisateur** non technique : après authentification, un client soumet une demande et en suit le score / statut, tandis qu'un analyste ou un directeur instruit les dossiers (liste paginée, changement de statut) directement depuis le navigateur, **sans appel d'API manuel**. Le front-end est versionné dans le même dépôt (`frontend/`) et évolue avec le back-end.

---

## C2.3.1 — Cahier de Recettes

### Scénario de Test 1 — Soumission d'une Demande de Crédit

**Objectif :** Vérifier qu'un client authentifié peut soumettre une demande valide

| Étape | Action | Données d'entrée | Résultat Attendu | Statut |
|-------|--------|-----------------|------------------|--------|
| 1 | S'authentifier | `POST /api/v1/auth/login` `{"username":"client1","password":"password123"}` | HTTP 200, `{"token": "eyJ..."}` retourné | ✅ |
| 2 | Soumettre demande valide | `POST /api/v1/demandes` `{"montant":15000,"dureeMois":60,"nomEmprunteur":"Jean Dupont"}` | HTTP 201, demande créée avec statut SOUMISE, score calculé | ✅ |
| 3 | Vérifier persistance | `GET /api/v1/demandes/{id}` avec token CLIENT | HTTP 200, demande trouvée avec `scoreCredit` et `risqueCredit` renseignés | ✅ |
| 4 | Soumettre montant invalide | `POST /api/v1/demandes` `{"montant":100,"dureeMois":6,"nomEmprunteur":"Test"}` | HTTP 400, message "Montant minimum : 1 000 €" | ✅ |
| 5 | Soumettre sans token | `POST /api/v1/demandes` sans header Authorization | HTTP 401 Unauthorized | ✅ |

---

### Scénario de Test 2 — Transition de Statut (Machine à États)

**Objectif :** Vérifier les transitions autorisées et interdites

| Étape | Action | Données | Résultat Attendu | Statut |
|-------|--------|---------|------------------|--------|
| 1 | SOUMISE → EN_INSTRUCTION | `PATCH /{id}/statut` `{"statut":"EN_INSTRUCTION"}` (rôle ANALYSTE) | HTTP 200, statut mis à jour, notification RabbitMQ envoyée | ✅ |
| 2 | EN_INSTRUCTION → SCORING_EN_COURS | `PATCH /{id}/statut` `{"statut":"SCORING_EN_COURS"}` | HTTP 200, transition valide | ✅ |
| 3 | SCORING_EN_COURS → APPROUVEE | `PATCH /{id}/statut` `{"statut":"APPROUVEE"}` (rôle DIRECTEUR) | HTTP 200, décision journalisée dans `decision_audit` | ✅ |
| 4 | Tenter SOUMISE → APPROUVEE (invalide) | `PATCH /{id}/statut` `{"statut":"APPROUVEE"}` | HTTP 409 Conflict, message "Transition interdite : SOUMISE → APPROUVEE" (`GlobalExceptionHandler`) | ✅ |
| 5 | Transition avec rôle CLIENT | `PATCH /{id}/statut` avec token CLIENT | HTTP 403 Forbidden | ✅ |

---

### Scénario de Test 3 — Scoring Externe et Circuit Breaker

**Objectif :** Vérifier le comportement du scoring externe et la résilience

| Étape | Action | Résultat Attendu | Statut |
|-------|--------|------------------|--------|
| 1 | Soumettre demande (service scoring disponible) | Score calculé via WebClient, `scoreCredit` et `risqueCredit` renseignés | ✅ |
| 2 | Simuler panne service scoring (timeout/erreur) | Circuit breaker Resilience4j activé, fallback retourne score=500, risque="MOYEN" | ✅ |
| 3 | Vérifier métriques circuit breaker | `GET /actuator/metrics/resilience4j.circuitbreaker.state` | ✅ |

---

### Scénario de Test 4 — Sécurité et Authentification

**Objectif :** Vérifier la robustesse de l'authentification JWT et du RBAC

| Étape | Attaque simulée | Résultat Attendu | Statut |
|-------|----------------|------------------|--------|
| 1 | Login avec identifiants invalides | HTTP 401, `{"error": "Identifiants invalides"}` | ✅ |
| 2 | CLIENT appelle GET /demandes (liste) | HTTP 200, liste scopée aux seules demandes de ce client (voir BUG-002 en C2.3.2 — avant correctif, cet appel était refusé à tort) | ✅ |
| 3 | ANALYSTE tente POST /demandes (réservé CLIENT) | HTTP 403 Forbidden | ✅ |
| 4 | DIRECTEUR tente POST /demandes (réservé CLIENT) | HTTP 403 Forbidden | ✅ |
| 5 | Token JWT falsifié (signature invalide) | HTTP 401, filtre JWT rejette le token | ✅ |
| 6 | Requête sans header Authorization | HTTP 401 Unauthorized | ✅ |
| 7 | `client2` tente `GET /demandes/{id}` sur une demande créée par `client1` (test anti-IDOR) | HTTP 403 Forbidden | ✅ |
| 8 | `analyste1` consulte la même demande que l'étape 7 | HTTP 200 (pas de restriction de propriété pour ce rôle) | ✅ |
| 9 | `client2` appelle `GET /demandes` (liste) après l'étape 7 | HTTP 200, la demande de `client1` n'apparaît pas dans la liste de `client2` (isolation confirmée aussi côté listing, pas seulement à l'accès direct par ID) | ✅ |

---

### Scénario de Test 5 — Monitoring et Supervision

**Objectif :** Vérifier les endpoints de supervision

| Étape | Action | Résultat Attendu | Statut |
|-------|--------|------------------|--------|
| 1 | Vérifier health check | `GET /actuator/health` → HTTP 200 `{"status":"UP","components":{"db":...}}` | ✅ |
| 2 | Vérifier métriques Prometheus | `GET /actuator/prometheus` → métriques JVM, HTTP, Resilience4j | ✅ |
| 3 | Vérifier info applicative | `GET /actuator/info` → informations du service | ✅ |

---

### Scénario de Test 6 — Recette Fonctionnelle de l'Interface Web (Front-End)

**Objectif :** Vérifier le parcours utilisateur complet via le navigateur

| Étape | Action (UI) | Résultat Attendu | Statut |
|-------|-------------|------------------|--------|
| 1 | Se connecter en tant que `client1` (écran de login) | Redirection vers l'écran « Mes demandes » (liste des demandes de ce client) | ✅ |
| 2 | Menu « Nouvelle demande » → soumettre une demande (15 000 €, 60 mois) | Notification de succès, redirection vers le détail avec score/risque affichés | ✅ |
| 3 | Saisir un montant < 1 000 € | Message d'erreur sous le champ, bouton *Soumettre* inopérant tant que le formulaire est invalide | ✅ |
| 4 | Revenir sur « Mes demandes » via le menu | La demande soumise à l'étape 2 apparaît dans la liste, avec son score et son statut | ✅ |
| 5 | Se déconnecter puis se reconnecter en tant que `client1` | « Mes demandes » réaffiche la même liste (persistance, pas de régression sur le scoping après reconnexion) | ✅ |
| 6 | Se connecter en tant que `client2` et ouvrir « Mes demandes » | La demande de `client1` n'apparaît pas — chaque client ne voit que ses propres demandes | ✅ |
| 7 | Se connecter en tant que `analyste1` | Écran « Demandes » (liste paginée **complète**, tous clients confondus) affiché | ✅ |
| 8 | Ouvrir la modale *Statut* et appliquer une transition | Statut mis à jour + notification ; transitions interdites non proposées | ✅ |
| 9 | Token expiré/falsifié puis appel API | Réponse `401` interceptée → purge de session + redirection `/login` | ✅ |

---

### Scénario de Test 7 — Recette d'Accessibilité (RGAA 4.1)

**Objectif :** Vérifier l'accessibilité de l'interface web

| Étape | Contrôle | Résultat Attendu | Statut |
|-------|----------|------------------|--------|
| 1 | Navigation entièrement au clavier | Tous les contrôles atteignables, focus visible, lien d'évitement fonctionnel | ✅ |
| 2 | Fermeture de la modale par `Échap` | La fenêtre modale se ferme et rend la main | ✅ |
| 3 | Restitution des erreurs de formulaire | Messages `role="alert"` annoncés, champs `aria-invalid` | ✅ |
| 4 | Audit automatique (Lighthouse / axe) | Aucune erreur bloquante attendue (contrastes, labels, rôles ARIA en place) | ✅ |

---

## C2.3.2 — Plan de Correction des Bogues

### Processus de Qualification des Anomalies

```
Détection anomalie
       │
       ▼
Reproduction locale → Impossible ? → Demander logs + environnement
       │
       ▼
Identification root cause (logs, débogueur)
       │
       ▼
Classification :
  P1 (bloquant)    → Hotfix branch, correction < 24h, release immédiate
  P2 (majeur)      → Sprint suivant, correction < 1 semaine
  P3 (mineur)      → Backlog, traité selon priorité
       │
       ▼
Écrire test unitaire reproduisant le bug (TDD)
       │
       ▼
Corriger le code
       │
       ▼
Valider test vert + couverture maintenue ≥ 80%
       │
       ▼
PR + review + merge via CI/CD
       │
       ▼
Documenter dans le message de commit + journal des anomalies
```

### Fiche de Bogue Type

| Champ | Contenu |
|-------|---------|
| **ID** | BUG-001 |
| **Titre** | NullPointerException lors du calcul du score si nomEmprunteur absent |
| **Priorité** | P1 — Bloquant |
| **Environnement** | Développement, Spring Boot 4.0.3, Java 21 |
| **Étapes de reproduction** | 1. `POST /api/v1/demandes` avec `nomEmprunteur` null 2. Le scoring est appelé avec `null` comme paramètre |
| **Comportement observé** | HTTP 500 — NPE dans `ScoringIsolationManager.calculerScore()` |
| **Comportement attendu** | HTTP 400 — Validation Bean Validation bloquante avant appel service |
| **Cause racine** | Annotation `@NotBlank` manquante sur `DemandeCreditApi.nomEmprunteur` |
| **Correction** | Ajout `@NotBlank(message = "Le nom de l'emprunteur est obligatoire")` + fallback `"INCONNU"` dans le service |
| **Statut** | Corrigé — v0.1.1 |

### Second Exemple — Anomalie Trouvée en Recette Utilisateur Réelle

| Champ | Contenu |
|-------|---------|
| **ID** | BUG-002 |
| **Titre** | Un CLIENT ne peut pas consulter la liste de ses propres demandes |
| **Priorité** | P2 — Majeur (fonctionnalité manquante, aucune perte ni fuite de données) |
| **Environnement** | Recette manuelle, Spring Boot 4.0.3 + Angular 20, backend et frontend lancés en local |
| **Étapes de reproduction** | 1. Se connecter en tant que `client1` 2. Soumettre une demande de crédit 3. Se déconnecter puis se reconnecter en `client1` 4. Aucun écran ni lien de menu ne permet de retrouver la demande soumise |
| **Comportement observé** | `GET /api/v1/demandes` renvoie HTTP 403 pour un rôle CLIENT ; la route Angular `/demandes` et le lien de navigation associé étaient réservés à ANALYSTE/DIRECTEUR ; seule la création (`/demandes/nouvelle`) était accessible |
| **Comportement attendu** | Un CLIENT doit pouvoir consulter la liste de ses propres demandes, au même titre qu'il peut déjà consulter le détail d'une demande individuelle (`GET /demandes/{id}`) |
| **Cause racine** | Règle `SecurityConfig` sur `GET /api/v1/demandes` limitée à `hasAnyRole("ANALYSTE", "DIRECTEUR")` ; aucune méthode de filtrage par `clientUsername` dans `DemandeCreditRepository` ; route Angular et lien de menu correspondants restreints aux mêmes rôles côté front |
| **Correction** | Ajout de `CLIENT` à la règle de sécurité ; nouvelle méthode `DemandeCreditRepository.findByClientUsername` + `DbIsolationManager`/`DemandeCreditService` associés ; `DemandeCreditController.obtenirToutesLesDemandes` renvoie désormais la liste complète pour ANALYSTE/DIRECTEUR et la liste scopée au `clientUsername` de l'appelant pour CLIENT ; ouverture de la route `/demandes` et ajout du lien « Mes demandes » côté Angular ; 2 tests ajoutés dans `DemandeCreditControllerTest` (liste complète pour le staff, liste scopée pour un client) |
| **Statut** | Corrigé et vérifié manuellement (voir Scénario de Test 6, étapes 1-6) — en attente de commit/tag de version (voir C2.2.4) |

Cette anomalie illustre concrètement le processus décrit ci-dessus : détectée lors d'une recette utilisateur réelle (et non lors d'une revue de code), elle a été qualifiée en P2, sa cause racine identifiée avant correction (plutôt qu'un correctif superficiel), puis validée par de nouveaux tests unitaires avant d'être considérée comme résolue.

---

## C2.4.1 — Documentation Technique

### Manuel de Déploiement

#### Prérequis

- Docker Engine ≥ 24.x et Docker Compose ≥ 2.24.x
- Java JDK 21 (pour build local)
- Maven 3.9.x (pour build local)

#### Déploiement Local (Développement)

```bash
# 1. Cloner le dépôt
git clone https://github.com/yeveee/creditflow.git
cd creditflow

# 2. Démarrer les services tiers via Docker Compose (PostgreSQL + RabbitMQ)
docker compose up -d

# 3. Lancer l'application
mvn spring-boot:run

# 4. Vérifier la santé
curl http://localhost:8080/actuator/health
# → {"status":"UP"}

# 5. Accéder à Swagger UI
open http://localhost:8080/swagger-ui.html
```

#### Dépannage

**`error: release version 21 not supported` au lancement de `mvn spring-boot:run`** — anomalie rencontrée lors d'un test réel du manuel par un tiers n'ayant jamais touché au projet, ce qui a permis de compléter cette section.

Maven compile avec le JDK qu'il trouve dans son environnement, qui peut différer de celui renvoyé par `java -version` si plusieurs JDK sont installés sur la machine.

1. Vérifier le JDK réellement utilisé par Maven : `mvn -version` → la ligne `Java version: ...` doit afficher `21.x`
2. Si ce n'est pas le cas, installer un JDK 21 (ex. Temurin, cohérent avec le Dockerfile/CI du projet)
3. Faire pointer `JAVA_HOME` sur ce JDK 21 (macOS : `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`)
4. Revérifier `mvn -version`, puis relancer `mvn spring-boot:run`

#### Déploiement Conteneurisé (Docker)

```bash
# 1. Builder l'image (multi-stage : JDK 21 Alpine → JRE 21 Alpine)
docker build -t creditflow:1.0.0 .

# 2. Lancer avec Docker Compose
docker compose up -d

# 3. Vérifier les logs
docker compose logs -f
```

#### Variables d'Environnement

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| `SPRING_DATASOURCE_URL` | URL PostgreSQL | `jdbc:postgresql://localhost:5432/creditflow` |
| `SPRING_DATASOURCE_USERNAME` | Utilisateur DB | `creditflow_user` |
| `SPRING_DATASOURCE_PASSWORD` | Mot de passe DB | `creditflow_pass` |
| `SPRING_RABBITMQ_HOST` | Hôte RabbitMQ | `localhost` |
| `SPRING_RABBITMQ_PORT` | Port RabbitMQ | `5672` |
| `SPRING_RABBITMQ_USERNAME` | Utilisateur RabbitMQ | `guest` |
| `SPRING_RABBITMQ_PASSWORD` | Mot de passe RabbitMQ | `guest` |
| `JWT_SECRET` | Clé de signature des tokens JWT | valeur de développement (à changer en production) |
| `SERVER_PORT` | Port de l'application | `8080` |

Toutes ces variables ont une valeur par défaut adaptée au développement local (`docker compose up -d` + `mvn spring-boot:run` fonctionnent sans rien configurer) ; en production, elles doivent être surchargées, en particulier `JWT_SECRET` et les identifiants PostgreSQL/RabbitMQ.

#### Déploiement du Front-End (Interface Web)

```bash
cd frontend
npm install            # installation des dépendances (première fois)
npm run build          # bundles de production dans dist/frontend
# Servir dist/frontend derrière Nginx en production, ou en développement :
npm start              # http://localhost:4200 (proxy /api -> :8080)
```

Le front nécessite que l'API soit accessible (proxy `/api` → `http://localhost:8080` en développement, URL configurable en production).

---

### Manuel d'Utilisation (API)

#### Authentification

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "analyste1",
  "password": "password123"
}

# Réponse
HTTP 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Quatre utilisateurs pré-configurés dans `data.sql` :

| Username | Rôle | Accès |
|----------|------|-------|
| `client1` | CLIENT | Soumettre des demandes, consulter ses propres demandes |
| `client2` | CLIENT | Second compte client — sert à vérifier qu'un client ne peut pas consulter les demandes d'un autre (contrôle anti-IDOR, voir C2.2.3/C2.3.1) |
| `analyste1` | ANALYSTE | Lister/consulter les demandes, changer le statut |
| `directeur1` | DIRECTEUR | Lister les demandes, approuver/refuser |

#### Soumettre une Demande

```http
POST /api/v1/demandes
Authorization: Bearer <token>
Content-Type: application/json

{
  "montant": 25000.00,
  "dureeMois": 84,
  "nomEmprunteur": "Jean Dupont"
}

# Réponse
HTTP 201 Created
{
  "id": 1,
  "montant": 25000.00,
  "dureeMois": 84,
  "nomEmprunteur": "Jean Dupont",
  "statut": "SOUMISE",
  "scoreCredit": 720,
  "risqueCredit": "FAIBLE",
  "dateCreation": "2026-03-16T20:00:00"
}
```

#### Changer le Statut

```http
PATCH /api/v1/demandes/1/statut
Authorization: Bearer <token_analyste>
Content-Type: application/json

{
  "statut": "EN_INSTRUCTION"
}

# Réponse
HTTP 200 OK
{
  "id": 1,
  "statut": "EN_INSTRUCTION",
  ...
}
```

---

### Manuel d'Utilisation (Interface Web)

1. Ouvrir `http://localhost:4200` et se connecter (comptes de démonstration proposés sur l'écran).
2. **Client** : atterrit sur « Mes demandes » (ses propres demandes, potentiellement vide au premier accès) ; menu « Nouvelle demande » → saisir montant / durée / emprunteur → *Soumettre* ; le score et le statut s'affichent sur l'écran de détail, et la demande apparaît ensuite dans « Mes demandes ».
3. **Analyste** : menu « Demandes » → consulter la liste paginée, ouvrir le détail, faire évoluer le statut (transitions valides uniquement).
4. **Directeur** : « Demandes » → gérer le statut (approuver / refuser) depuis la liste.
5. Bouton **Déconnexion** (en haut à droite) pour clore la session.

| Utilisateur | Mot de passe | Rôle |
|-------------|--------------|------|
| `client1` | `password123` | CLIENT |
| `client2` | `password123` | CLIENT (pour tester l'isolation entre clients) |
| `analyste1` | `password123` | ANALYSTE |
| `directeur1` | `password123` | DIRECTEUR |

---

### Manuel de Mise à Jour

#### Mise à Jour des Dépendances

Dependabot est configuré (`.github/dependabot.yml`) pour surveiller automatiquement les mises à jour :

- **Maven** : dépendances Java (Spring Boot, MapStruct, JJWT, Resilience4j, etc.) — vérification hebdomadaire
- **Docker** : image de base (`eclipse-temurin`) — vérification hebdomadaire

Mise à jour manuelle :

```bash
# Vérifier les dépendances obsolètes
mvn versions:display-dependency-updates

# Mettre à jour les dépendances de patch
mvn versions:use-latest-releases -DallowMajorUpdates=false
```

Pour le **front-end**, les dépendances npm se mettent à jour via `npm outdated` puis `npm update` ; les montées de version majeures d'Angular s'effectuent avec `ng update @angular/core @angular/cli`.

#### Procédure de Mise à Jour Applicative

1. Créer une branche `release/x.y.z`
2. Mettre à jour `pom.xml` (version)
3. Exécuter la CI complète (`mvn clean verify` + build Docker)
4. Créer un tag Git annoté : `git tag -a vx.y.z -m "Description"`
5. Merger vers `main`, pousser le tag
6. Pipeline CI/CD GitHub Actions déclenché automatiquement
7. Vérifier le health check : `curl http://localhost:8080/actuator/health`
8. Documenter dans le journal des versions (BLOC 4)

---

*Document rédigé dans le cadre du projet final de formation — CreditFlow Microservice — Mars 2026*
