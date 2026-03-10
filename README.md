# WikiAim Backend

Backend API du wiki collaboratif WikiAim.

## Stack technique

- **Java 25** / **Micronaut 4**
- **PostgreSQL 17** + Flyway (migrations)
- **JPA / Hibernate** (ORM)
- **JWT** (authentification) + **Discord OAuth** (connexion)
- **Docker** + **Traefik** (déploiement)

## Architecture

```
src/main/java/com/wikiaim/backend/
├── auth/           # Discord OAuth, JWT, DevTokenController
├── core/           # GlobalExceptionHandler, TipTapTextExtractor
├── issues/         # Signalement de problèmes (CRUD)
├── pages/          # Pages du wiki (lecture publique)
├── revisions/      # Propositions de modifications + diff + approbation
└── users/          # Entité User, rôles (USER, CONTRIBUTOR, MODERATOR, ADMIN)
```

## Lancer en local

### Prérequis

- Java 25+
- Maven 3.9+
- Docker (pour PostgreSQL)

### Démarrage

```bash
# 1. Lancer PostgreSQL
docker compose up -d

# 2. Lancer l'application
./mvnw mn:run -Dmicronaut.environments=local
```

L'API est disponible sur `http://localhost:8080`.
Swagger UI : `http://localhost:8080/swagger-ui/index.html`.

### Données de test (optionnel)

Un script de seed est disponible pour peupler la base avec des données réalistes (utilisateurs, pages, révisions, issues) :

```bash
docker exec -i wikiaim-postgres psql -U wikiaim -d wikiaim < src/main/resources/db/seed-local.sql
```

Le script est idempotent (re-exécutable sans erreur grâce à `ON CONFLICT DO NOTHING`).

### Tester avec Swagger

En environnement `local`, un endpoint de dev est disponible :

1. Appeler `POST /api/v1/dev/token?role=ADMIN` pour générer un token JWT
2. Cliquer sur **Authorize** dans Swagger UI
3. Coller le token
4. Tous les endpoints protégés sont accessibles

### Lancer les tests

```bash
./mvnw verify
```

Les tests utilisent [Micronaut Test Resources](https://micronaut-projects.github.io/micronaut-test-resources/latest/guide/) qui lance automatiquement un PostgreSQL en container.

## Configuration

| Variable | Description | Défaut |
|----------|-------------|--------|
| `DB_HOST` | Hôte PostgreSQL | `localhost` |
| `DB_PORT` | Port PostgreSQL | `5432` |
| `DB_NAME` | Nom de la base | `wikiaim` |
| `DB_USERNAME` | Utilisateur DB | `wikiaim` |
| `DB_PASSWORD` | Mot de passe DB | _(requis)_ |
| `JWT_SECRET` | Clé de signature JWT (32+ chars) | _(requis)_ |
| `CORS_ORIGIN` | Origine autorisée (frontend) | _(requis)_ |

Voir [`.env.example`](.env.example) pour un exemple complet.

## Déploiement

Le projet utilise GitHub Actions pour le CI/CD :

- **`develop`** → build + deploy en **preprod**
- **`main`** → build + deploy en **prod**

L'image Docker est poussée sur GHCR et déployée via `docker compose` sur un VPS avec Traefik.

Voir [`CONTRIBUTING.md`](CONTRIBUTING.md) pour le workflow Git.

## Licence

[MIT](LICENSE)
