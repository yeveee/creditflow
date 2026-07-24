# BLOC 2 — Concevoir et Développer des Applications Logicielles

**Projet :** CreditFlow — Microservice de Gestion des Demandes de Crédit  
**Candidat :** Yevhenii Bondarenko  
**Date :** Juillet 2026  
**Repository GitHub :** [https://github.com/yeveee/creditflow](https://github.com/yeveee/creditflow)

---

## Périmètre du Dossier et Architecture Générale

Le projet **CreditFlow** est livré en deux composants complémentaires, versionnés dans le même dépôt :

| Composant | Rôle | Technologies |
|-----------|------|--------------|
| **API (back-end)** | Microservice métier : soumission, scoring, machine à états, sécurité JWT/RBAC | Java 21, Spring Boot 4.0.3, PostgreSQL, RabbitMQ |
| **Interface web (front-end)** | Client des utilisateurs (client, analyste, directeur) : authentification, soumission, liste et détail des demandes | Angular 20 (*standalone*, *signals*), TypeScript, SCSS |

Le front-end (`frontend/`) appelle l'API (`creditflow/`) sur `/api/v1`. En développement, le serveur Angular redirige automatiquement les appels `/api` et `/actuator` vers `http://localhost:8080` : pas besoin de configurer CORS. Le front applique les mêmes règles d'accès par rôle que le back-end (`SecurityConfig`), pour n'afficher aux utilisateurs que ce qu'ils ont le droit de voir.

## Correspondance avec la Grille d'Évaluation

| Compétence | Livrable attendu | Où le trouver |
|------------|------------------|---------------|
| **C2.1.1** | Protocole de déploiement continu ; critères de qualité/performance | § C2.1.1 |
| **C2.1.2** | Protocole d'intégration continue | § C2.1.2 |
| **C2.2.1** | Architecture maintenable ; prototype ; framework et paradigmes | § C2.2.1 (SOL V1 + prototype front Angular) |
| **C2.2.2** | Jeu de tests unitaires couvrant une fonctionnalité | § C2.2.2 |
| **C2.2.3** | Mesures de sécurité (OWASP) ; accessibilité (référentiel) | § C2.2.3 (OWASP Top 10 + RGAA 4.1 implémenté) |
| **C2.3.1** | Cahier de recettes | § C2.3.1 |
| **C2.3.2** | Plan de correction des bogues | § C2.3.2 |
| **C2.4.1** | Manuels de déploiement, d'utilisation et de mise à jour | § C2.4.1 |

---

## C2.1.1 — Environnements de Déploiement et de Test

### Environnement de Développement

| Outil | Rôle | Version |
|-------|------|---------|
| Visual Studio Code | Éditeur principal, back-end et front-end (extensions Java + TypeScript) | — |
| Java JDK | Compilation et exécution du back-end | 21 LTS |
| Maven | Gestion des dépendances et build back-end | 3.9.x |
| Tomcat (embarqué) | Serveur qui fait tourner l'API — livré directement avec Spring Boot, rien à installer à part (`spring-boot-starter-webmvc`) | 11.0.x |
| Docker Desktop | Fait tourner les services tiers dans des conteneurs | 24.x |
| Docker Compose | Démarre PostgreSQL et RabbitMQ ensemble en une commande | 2.24.x |
| Git | Suivi des versions du code | 2.43 |
| GitHub | Hébergement du dépôt + intégration continue | — |
| Node.js | Fait tourner les outils de build du front-end Angular | 22 LTS |
| Angular CLI | Génère les fichiers du projet, build (`ng build`), serveur de dev (`ng serve`) | 20.x |
| npm | Gestion des dépendances front-end | 10.x |

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
│ PostgreSQL │ RabbitMQ │ Resilience4j        │
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
| Couverture de code | JaCoCo (le code généré automatiquement par MapStruct n'est pas compté) | ≥ 80% (objectif) | **96% — objectif dépassé** |
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

Le build produit des fichiers statiques (HTML/CSS/JS) : on peut les héberger sur n'importe quel serveur web (Nginx, un CDN…), sans dépendre de l'API pour les servir. Chaque écran se charge séparément (*lazy-loading*), donc la première page reste légère (environ 90 kB). Ce build s'intègre à la même chaîne que le back-end : build → fichiers → mise en ligne → vérification que ça fonctionne.

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

À chaque push ou pull request, ce pipeline lance `mvn clean verify` : il compile le code et fait tourner tous les tests (JUnit 5 + Mockito). Deux tests démarrent une vraie application Spring (`@SpringBootTest`) et ont donc besoin d'une base PostgreSQL — le job en démarre une dédiée, avec un contrôle de santé, pour qu'elle soit prête au bon moment. Si un test échoue, le pipeline s'arrête là : l'image Docker n'est pas construite.

### Stratégie Git

Projet développé seul : une seule branche `main`, un commit par fonctionnalité ajoutée (ex. `added RabbitMQ Notifications on status change`), sans branches parallèles. Chaque commit correspond à un lot fonctionnel identifiable dans l'historique (`git log --oneline`), ce qui suffit à tracer les évolutions sur un projet à un seul développeur. Sur une équipe plus grande, un modèle par branches (une branche par fonctionnalité, fusionnée après revue) serait préférable.

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

Une interface web a été développée pour que les utilisateurs métier puissent se servir du microservice sans outil technique (pas de Postman, pas de ligne de commande). Elle couvre les principales *user stories* (US01 à US08).

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
- **Guards de routage** : `authGuard` bloque l'accès à qui n'est pas connecté ; `roleGuard` bloque l'accès selon le rôle. Les règles copient exactement celles du back-end — `CLIENT` : soumettre une demande, voir sa propre liste et son détail ; `ANALYSTE` : voir la liste complète, le détail, changer un statut ; `DIRECTEUR` : voir la liste complète, changer un statut.
- **Sécurité en double** : le front cache les boutons et actions non autorisés, mais c'est toujours le back-end qui décide vraiment (`401`/`403`) — même en contournant le front, le serveur refuse. Le token est stocké dans `localStorage` et lu pour afficher le rôle à l'écran.

---

## C2.2.2 — Harnais de Tests Unitaires

### Stratégie de Test

```
┌──────────────────────────────────────────────────────┐
│                 Pyramide de Tests                    │
│                                                      │
│              ┌────────────┐                          │
│              │  E2E (5%)  │  Recette manuelle        │
│           ┌──┴────────────┴──┐                       │
│           │ Intégration (15%)│  @SpringBootTest      │
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
| **Total back-end** | **12 classes de test** | **43 tests, tous passants** | **Couverture JaCoCo mesurée : 96%** |

Côté front-end, les tests sont en place avec un test de montage du composant racine (`app.spec.ts`). Les services (`AuthService`, `DemandeService`) et les *guards* sont conçus pour être testables unitairement (dépendances injectées, logique pure, pas d'accès direct au DOM).

---

## C2.2.3 — Sécurité et Accessibilité

### Couverture OWASP Top 10

| # | Catégorie OWASP | Risque dans CreditFlow | Mesure Implémentée |
|---|-----------------|------------------------|---------------------|
| A01 | Broken Access Control | Accès à des endpoints hors périmètre | Chaque endpoint vérifie le rôle via Spring Security (`SecurityConfig`). Un CLIENT ne voit que ses propres demandes, jamais celles d'un autre : refusé (`403`) s'il essaie d'accéder directement à la demande d'un tiers par son identifiant, et filtré automatiquement côté serveur pour la liste. ANALYSTE et DIRECTEUR voient tout, sans cette limite |
| A02 | Cryptographic Failures | Mots de passe en clair | Mots de passe hachés avec `BCryptPasswordEncoder` (jamais stockés en clair). Les tokens JWT sont signés avec une clé secrète (`jwt.secret`). Cette clé et les identifiants de base de données ne sont pas écrits en dur dans le code : ils viennent de variables d'environnement (`JWT_SECRET`, `SPRING_DATASOURCE_*`), avec une valeur par défaut pratique pour développer en local |
| A03 | Injection | Injection SQL via champs API | Requêtes paramétrées via Spring Data (pas de SQL construit à la main), et validation systématique des champs d'entrée (`@NotNull`, `@NotBlank`, `@DecimalMin`, `@Min`) |
| A04 | Insecure Design | Workflow de décision contournable | Une machine à états (`StatutTransitionValidator`) vérifie côté serveur qu'une transition de statut est autorisée ; une transition interdite renvoie une erreur claire (`409 Conflict`) plutôt qu'un plantage générique |
| A05 | Security Misconfiguration | Endpoints non protégés | L'API ne garde pas de session (`STATELESS`), donc pas de protection CSRF à gérer. Seuls les endpoints listés explicitement dans `SecurityConfig` sont publics — tout le reste demande une authentification. Seul `/actuator/health` reste accessible sans compte, et encore : un appel anonyme ne voit que `{"status":"UP"}`, sans détail. `/actuator/prometheus` et `/actuator/metrics` demandent désormais un compte |
| A06 | Vulnerable Components | Dépendances obsolètes | Dependabot configuré (`.github/dependabot.yml`) — surveillance hebdomadaire Maven + Docker |
| A07 | Auth Failures | Token falsifié ou expiré | Validation JWT systématique via `JwtAuthenticationFilter`, rejet des tokens expirés ou avec signature invalide |
| A08 | Software Integrity | Image Docker compromise | Dockerfile multi-stage (build séparé du runtime), `.dockerignore` pour exclure `.git/`, code source non présent dans l'image finale |
| A09 | Logging Failures | Pas de trace des décisions | Chaque changement de statut est enregistré de façon immuable dans la table `decision_audit` (qui, quand, ancien et nouveau statut). L'enregistrement passe par un appel direct — contrôleur → service → accès aux données — sans AOP (voir la note ci-dessous) |
| A10 | SSRF | Appel scoring vers URL arbitraire | URL du bureau de crédit fixée en dur dans `ScoringIsolationManager` (`http://localhost:8081`), non paramétrable par l'utilisateur |

> **Note d'architecture (A09) :** la journalisation d'audit a d'abord été codée avec Spring AOP (`@AfterReturning`), une technique qui déclenche du code automatiquement autour d'une méthode. Mais le build échouait parfois, sans raison apparente. Après investigation, la vraie cause n'était pas l'AOP : l'éditeur de code recompilait le projet en arrière-plan pendant que Maven faisait la même chose, et les deux écrivaient parfois en même temps dans le même dossier `target/classes` — ce qui cassait le build de temps en temps. En suspendant temporairement le processus de l'éditeur pendant un build, plus aucun échec ne s'est produit, ce qui a confirmé cette cause. L'appel direct (contrôleur → service → accès aux données) a quand même été gardé à la place de l'AOP : plus simple à lire, tout aussi correct, et ça évite d'activer un mécanisme Spring supplémentaire pour une seule fonctionnalité.

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

CreditFlow a maintenant une interface web (front-end Angular) en plus de l'API REST. Le RGAA 4.1 est appliqué concrètement, d'abord sur cette interface :

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

Ces mesures peuvent être vérifiées concrètement : navigation complète au clavier (tabulation, `Échap`, focus visible), audit automatique avec Lighthouse ou axe DevTools, et contrôle des contrastes de couleur. Un test avec un lecteur d'écran (VoiceOver ou NVDA) est aussi prévu en recette, pour vérifier que les notifications et messages d'erreur sont bien lus à voix haute.

---

## C2.3.1 — Cahier de Recettes

Ce cahier couvre les trois familles de tests attendues : tests **fonctionnels** (scénarios 1, 2, 3, 6 — parcours utilisateur et métier de bout en bout), tests **structurels** (couverture du code par les tests unitaires, détaillée en § C2.2.2 : 43 tests, 96% de couverture) et tests de **sécurité** (scénario 4 — authentification, RBAC, anti-IDOR).

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
| **Cause racine** | La règle de sécurité sur `GET /api/v1/demandes` n'autorisait que ANALYSTE et DIRECTEUR. `DemandeCreditRepository` n'avait aucune méthode pour filtrer les demandes par client. Côté Angular, la route et le lien de menu correspondants étaient réservés aux mêmes rôles |
| **Correction** | Ajout de `CLIENT` à la règle de sécurité. Nouvelle méthode `findByClientUsername` dans le repository, reliée au service et à l'accès aux données. Le contrôleur renvoie désormais la liste complète pour ANALYSTE/DIRECTEUR, et seulement les demandes du client pour CLIENT. Côté Angular : ouverture de la route `/demandes` et ajout du lien « Mes demandes ». Deux tests ajoutés pour vérifier les deux cas (liste complète, liste filtrée) |
| **Statut** | Corrigé et vérifié manuellement (voir Scénario de Test 6, étapes 1-6) — en attente de commit/tag de version |

Cette anomalie illustre concrètement le processus décrit ci-dessus : détectée lors d'une recette utilisateur réelle (et non lors d'une revue de code), elle a été qualifiée en P2, sa cause racine identifiée avant correction (plutôt qu'un correctif superficiel), puis validée par de nouveaux tests unitaires avant d'être considérée comme résolue.

---

## C2.4.1 — Documentation Technique

### Manuel de Déploiement

#### Choix Technologiques

| Technologie | Justification |
|---|---|
| Spring Boot 4.0.3 (Java 21) | Écosystème mature pour un microservice bancaire : injection de dépendances, Spring Security pour le RBAC/JWT, Spring Data JPA, intégration native avec Actuator/Micrometer pour le monitoring |
| PostgreSQL | Base relationnelle avec garanties ACID — cohérence indispensable pour des données financières (montants, statuts de décision) |
| RabbitMQ | Découplage asynchrone des notifications de changement de statut, sans bloquer le flux métier principal |
| MapStruct | Mapping *compile-time* (pas de réflexion à l'exécution) entre les 3 types de modèles de l'architecture SOL V1 — performant, et erreurs de mapping détectées dès la compilation |
| Angular 20 (*standalone*, *signals*) | Framework SPA robuste pour une interface multi-rôles (CLIENT/ANALYSTE/DIRECTEUR), TypeScript strict, réactivité fine sans NgModules |
| Docker multi-stage | Image de production légère (JRE seul embarqué) — outils de build et code source absents de l'image finale |

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

Toutes ces variables ont une valeur par défaut qui fonctionne directement en local (`docker compose up -d` + `mvn spring-boot:run` marchent sans rien régler). En production, il faut les changer — surtout `JWT_SECRET` et les identifiants PostgreSQL/RabbitMQ.

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

1. Mettre à jour le numéro de version dans `pom.xml`
2. Commiter directement sur `main` (pratique actuelle du projet — voir « Stratégie Git » en C2.1.2)
3. Vérifier que la CI passe (`mvn clean verify` + build Docker)
4. Créer un tag Git annoté (`git tag -a vx.y.z -m "Description"`) et le pousser
5. Le pipeline GitHub Actions se relance automatiquement sur ce push
6. Vérifier le health check : `curl http://localhost:8080/actuator/health`

---

*Document rédigé dans le cadre du projet final de formation — CreditFlow Microservice — Mars 2026*
