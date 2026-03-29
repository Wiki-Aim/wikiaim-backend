# Contribuer

## Workflow Git

```
main       ← production (déploiement automatique)
develop    ← preprod (déploiement automatique)
feature/*  ← branches de travail
```

1. Créer une branche depuis `develop` : `git checkout -b feature/ma-feature develop`
2. Développer et tester localement
3. Ouvrir une PR vers `develop`
4. Après review et merge → déploiement automatique en preprod
5. Quand `develop` est stable → PR vers `main` → déploiement en prod

## Setup local

```bash
# Cloner le repo
git clone git@github.com:Wiki-Aim/wikiaim-backend.git
cd wikiaim-backend

# Lancer PostgreSQL
docker compose up -d

# Lancer l'application
./mvnw mn:run -Dmicronaut.environments=local

# Lancer les tests
./mvnw verify
```

## Conventions

### Code

- **Lombok** : `@RequiredArgsConstructor` pour l'injection, `@Slf4j` pour le logging
- **Records** pour les DTOs (`@Serdeable`)
- **Validation** : annotations Jakarta (`@NotBlank`, `@Size`, etc.) sur les DTOs
- **Sécurité** : `@Secured` sur chaque endpoint, `@SecurityRequirement` pour Swagger

### Commits

Format libre mais concis. Exemples :
- `feat: ajout endpoint de recherche`
- `fix: validation du contenu TipTap`
- `refactor: extraction du service de diff`

### Tests

- Tests unitaires pour les services (`*ServiceTest`)
- Tests d'intégration pour les controllers (`*ControllerTest`)
- Les tests de controller utilisent des JWT réels (pas de désactivation de la sécurité)

```bash
# Tout lancer
./mvnw verify

# Un seul test
./mvnw test -Dtest=RevisionServiceTest
```

## Structure du projet

```
src/main/java/com/wikiaim/backend/
├── auth/           # Authentification (Discord OAuth + JWT)
├── core/           # Composants transversaux (exception handler, extracteur TipTap)
├── issues/         # Module issues (Controller → Service → Repository)
├── pages/          # Module pages (Controller → Service → Repository)
├── revisions/      # Module révisions (Controller → Service → Repository)
└── users/          # Entité User + Repository + Role enum
```

Chaque module suit le pattern **Controller → Service → Repository** avec des DTOs pour les entrées/sorties.

## Ajouter un endpoint

1. Créer le DTO dans le module (`@Serdeable`, validations Jakarta)
2. Ajouter la méthode dans le Service
3. Ajouter l'endpoint dans le Controller (`@Secured`, `@Operation`, `@ApiResponse`)
4. Écrire les tests (service + controller)
5. Si migration nécessaire : `src/main/resources/db/migration/V{N}__description.sql`
