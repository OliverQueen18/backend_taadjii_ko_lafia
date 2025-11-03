# Docker Deployment Guide - Taadji Ko Lafia Backend

## Configuration Docker Compose avec MySQL

Ce projet utilise Docker Compose avec MySQL 8.0 pour la base de données.

### Prérequis

- Docker Engine 20.10+
- Docker Compose 2.0+

### Démarrage rapide

```bash
# Démarrer tous les services
docker-compose up -d

# Vérifier les logs
docker-compose logs -f app

# Arrêter les services
docker-compose down

# Arrêter et supprimer les volumes (⚠️ supprime les données)
docker-compose down -v
```

### Services

#### MySQL (Port 3306)
- **Image**: mysql:8.0
- **Base de données**: fuelticketdb
- **Utilisateur**: fuelticket
- **Mot de passe**: changeit
- **Root password**: rootpassword
- **Volumes**: Persistance des données dans `mysql_data`

#### Application Spring Boot (Port 8000)
- **Build**: Multi-stage avec Maven et JRE Alpine
- **Healthcheck**: Vérifie automatiquement la disponibilité
- **Dépendances**: Attend que MySQL soit prêt avant de démarrer

### Variables d'environnement

Vous pouvez personnaliser les variables d'environnement dans `docker-compose.yml`:

```yaml
environment:
  JWT_SECRET: votre-secret-jwt
  JWT_EXPIRATION: 86400000
  SPRING_PROFILES_ACTIVE: prod
```

### Configuration de la base de données

La base de données est automatiquement initialisée avec le script `init-mysql.sql`.

### Accès

- **API**: http://localhost:8000
- **Swagger UI**: http://localhost:8000/swagger-ui.html
- **MySQL**: localhost:3306

### Commandes utiles

```bash
# Voir les logs en temps réel
docker-compose logs -f app

# Exécuter une commande dans le conteneur
docker-compose exec app sh

# Accéder à MySQL
docker-compose exec mysql mysql -u root -prootpassword fuelticketdb

# Redémarrer un service spécifique
docker-compose restart app

# Voir l'état des services
docker-compose ps
```

## Déploiement avec Jenkins

Le pipeline Jenkins automatise le build et le déploiement:

1. **Build Maven**: Compile le projet Java
2. **Build Docker**: Crée l'image Docker
3. **Push Docker Hub**: Publie l'image sur Docker Hub
4. **Notification**: Affiche les informations de déploiement

### Configuration Jenkins

1. Créer des credentials dans Jenkins:
   - ID: `dockerhub-credentials`
   - Type: Username/Password
   - Username: Votre Docker Hub username
   - Password: Votre Docker Hub token

2. Paramètres du pipeline:
   - `FORCE_MAVEN_CLEAN`: Nettoyer le cache Maven
   - `DEPLOY_ENV`: Environnement (dev/staging/production)
   - `DOCKER_IMAGE_TAG`: Tag de l'image (par défaut: latest)

### Tags d'image Docker

L'image est taguée avec:
- `${DOCKER_IMAGE_TAG}-${GIT_COMMIT_SHORT}` (ex: latest-abc1234)
- `latest` (toujours mis à jour)

