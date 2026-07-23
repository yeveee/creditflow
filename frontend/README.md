# CreditFlow — Frontend (Angular)

Interface web du microservice **CreditFlow** (Nexus Bank). Application Angular 20
(standalone + signals) consommant l'API REST `/api/v1`.

## Stack

- Angular 20 (composants standalone, signals, control flow `@if`/`@for`)
- Client HTTP avec intercepteur JWT
- Design system maison (SCSS, thème fintech indigo)

## Prérequis

- Node.js 20.19+ ou 22.12+
- **Le backend CreditFlow doit tourner sur `http://localhost:8080`**
  (voir `../creditflow`). Le serveur de dev proxifie `/api` et `/actuator`
  vers ce port — aucune configuration CORS n'est requise côté backend.

## Démarrage

```bash
npm install      # à la première utilisation
npm start        # démarre le serveur de dev sur http://localhost:4200
```

Le proxy est configuré dans `proxy.conf.json` (déclaré dans `angular.json`).

## Comptes de démonstration

| Utilisateur  | Rôle       | Mot de passe  |
|--------------|------------|---------------|
| `client1`    | CLIENT     | `password123` |
| `client2`    | CLIENT     | `password123` (second compte client, pour vérifier l'isolation entre clients) |
| `analyste1`  | ANALYSTE   | `password123` |
| `directeur1` | DIRECTEUR  | `password123` |

Des boutons de connexion rapide sont disponibles sur l'écran de login.

## Fonctionnalités par rôle

- **CLIENT** — soumettre une demande (`POST /demandes`), consulter la liste de ses propres demandes (« Mes demandes ») et leur détail.
- **ANALYSTE** — lister les demandes (paginé), consulter le détail, faire évoluer le statut.
- **DIRECTEUR** — lister les demandes, faire évoluer le statut (depuis la liste).

Les transitions de statut proposées respectent la machine à états du backend :
`SOUMISE → EN_INSTRUCTION → SCORING_EN_COURS → {APPROUVEE | REFUSEE | EN_ATTENTE_PIECES}`,
`EN_ATTENTE_PIECES → EN_INSTRUCTION`.

## Structure

```
src/app/
├── core/
│   ├── models/         # DemandeCredit, Page<T>, Auth
│   ├── services/       # AuthService, DemandeService, ToastService
│   ├── interceptors/   # authInterceptor (Bearer + gestion 401)
│   ├── guards/         # authGuard, roleGuard
│   └── util/           # statut.util (labels, badges, transitions)
├── layout/shell/       # barre de navigation + <router-outlet>
├── shared/             # StatutBadge, StatutActions
└── features/
    ├── auth/login/
    ├── demandes/       # demande-list / demande-create / demande-detail
    ├── home/           # redirection selon le rôle
    └── errors/forbidden/
```

## Scripts

```bash
npm start     # serveur de dev (proxy activé)
npm run build # build de production dans dist/
npm test      # tests unitaires (Karma/Jasmine)
```
