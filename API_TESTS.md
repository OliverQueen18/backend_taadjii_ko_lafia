# Tests d'API - Fuel Ticket Backend

## Prérequis
- L'application doit être démarrée sur http://localhost:8080
- PostgreSQL doit être en cours d'exécution

## Tests avec cURL

### 1. Inscription d'un utilisateur
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Test",
    "prenom": "User",
    "email": "test@example.com",
    "password": "password123",
    "role": "CITOYEN"
  }'
```

### 2. Connexion
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 3. Créer une station (Admin requis)
```bash
curl -X POST http://localhost:8080/api/stations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "nom": "Station Test",
    "localisation": "123 Rue de Test",
    "capaciteJournaliere": 5000.0,
    "stockDisponible": 3000.0
  }'
```

### 4. Lister les stations (Public)
```bash
curl -X GET http://localhost:8080/api/stations
```

### 5. Créer un ticket (Citoyen requis)
```bash
curl -X POST "http://localhost:8080/api/tickets?stationId=1&typeCarburant=Essence&quantite=50.0" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 6. Voir mes tickets (Citoyen requis)
```bash
curl -X GET http://localhost:8080/api/tickets/my-tickets \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Tests avec Postman

1. Importez la collection Postman (à créer)
2. Configurez l'environnement avec les variables :
   - `base_url`: http://localhost:8080
   - `jwt_token`: (obtenu après connexion)

## Données de test

### Utilisateurs par défaut
- **Admin**: admin@fuelticket.com / password123
- **Citoyen**: jean.dupont@email.com / password123
- **Station**: station@fuelticket.com / password123

### Stations par défaut
- Station Total Centre (ID: 1)
- Station Shell Nord (ID: 2)
- Station BP Sud (ID: 3)

## Documentation API

Accédez à la documentation Swagger UI sur :
http://localhost:8080/swagger-ui.html
