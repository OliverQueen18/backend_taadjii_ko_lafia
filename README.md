# Fuel Ticket Backend

Une application Spring Boot pour la gestion des tickets de carburant.

## Fonctionnalités

- **Authentification JWT** : Système d'authentification sécurisé avec tokens JWT
- **Gestion des utilisateurs** : CRUD complet pour les utilisateurs (Citoyens, Stations, Admins)
- **Gestion des stations** : CRUD complet pour les stations-service
- **Gestion des tickets** : Création automatique de tickets avec numéros d'ordre et dates d'approvisionnement
- **Système de rôles** : Protection des endpoints basée sur les rôles
- **Documentation API** : Swagger UI intégré

## Architecture

- **Backend** : Spring Boot 3.1.4 avec Java 17
- **Base de données** : PostgreSQL
- **Sécurité** : Spring Security avec JWT
- **Documentation** : OpenAPI 3 (Swagger)
- **Containerisation** : Docker + Docker Compose

## Démarrage rapide

### Avec Docker Compose (Recommandé)

```bash
# Cloner le repository
git clone <repository-url>
cd fuel-ticket-backend

# Lancer l'application avec Docker Compose
docker-compose up --build

# L'application sera disponible sur http://localhost:8080
# La documentation API sur http://localhost:8080/swagger-ui.html
```

### Développement local

```bash
# Prérequis : PostgreSQL installé et configuré
# 1. Créer une base de données PostgreSQL :
#    - Nom: fuelticketdb
#    - Utilisateur: fuelticket
#    - Mot de passe: changeit

# 2. Compiler l'application
.\mvnw.cmd clean package -DskipTests

# 3. Lancer l'application
# Option A: Avec le script PowerShell
.\start.ps1

# Option B: Avec le script Batch
start.bat

# Option C: Directement avec Java
java -jar target/fuel-ticket-backend-0.0.1-SNAPSHOT.jar

# Option D: Avec Maven
.\mvnw.cmd spring-boot:run
```

## API Endpoints

### Authentification
- `POST /api/auth/login` - Connexion utilisateur
- `POST /api/auth/register` - Inscription utilisateur
- `GET /api/auth/me` - Informations utilisateur actuel

### Utilisateurs (Admin uniquement)
- `GET /api/users` - Liste tous les utilisateurs
- `GET /api/users/{id}` - Détails d'un utilisateur
- `POST /api/users` - Créer un utilisateur
- `PUT /api/users/{id}` - Modifier un utilisateur
- `DELETE /api/users/{id}` - Supprimer un utilisateur

### Stations
- `GET /api/stations` - Liste toutes les stations (public)
- `GET /api/stations/{id}` - Détails d'une station (public)
- `POST /api/stations` - Créer une station (Admin)
- `PUT /api/stations/{id}` - Modifier une station (Admin/Station)
- `DELETE /api/stations/{id}` - Supprimer une station (Admin)

### Tickets
- `POST /api/tickets` - Créer un ticket (Citoyen)
- `GET /api/tickets` - Liste tous les tickets (Admin)
- `GET /api/tickets/{id}` - Détails d'un ticket
- `GET /api/tickets/my-tickets` - Mes tickets (Citoyen)
- `GET /api/tickets/station/{stationId}` - Tickets d'une station (Station/Admin)
- `PUT /api/tickets/{id}/status` - Modifier le statut d'un ticket (Station/Admin)

## Rôles et Permissions

- **CITOYEN** : Peut créer des tickets et voir ses propres tickets
- **STATION** : Peut gérer les tickets de sa station et modifier les stations
- **ADMIN** : Accès complet à toutes les fonctionnalités

## Configuration

### Variables d'environnement

```properties
# Base de données
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/fuelticketdb
SPRING_DATASOURCE_USERNAME=fuelticket
SPRING_DATASOURCE_PASSWORD=changeit

# JWT
JWT_SECRET=mySecretKey
JWT_EXPIRATION=86400000
```

## Utilisation

1. **Inscription** : Créer un compte via `/api/auth/register`
2. **Connexion** : Se connecter via `/api/auth/login` pour obtenir un token JWT
3. **Authentification** : Inclure le token dans l'en-tête `Authorization: Bearer <token>`
4. **Création de tickets** : Les citoyens peuvent créer des tickets de carburant
5. **Gestion** : Les stations peuvent gérer leurs tickets et stocks

## Développement

```bash
# Tests
mvn test

# Build
mvn clean package

# Docker build
docker build -t fuel-ticket-backend .
```
