Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Démarrage de Taadji Ko Lafia avec MySQL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "1. Vérification de la base de données MySQL..." -ForegroundColor Yellow
& "C:\wamp64\bin\mysql\mysql8.3.0\bin\mysql.exe" -u root -e "CREATE DATABASE IF NOT EXISTS fuelticketdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

Write-Host ""
Write-Host "2. Démarrage de l'application Spring Boot..." -ForegroundColor Yellow
Write-Host ""

.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=default

Read-Host "Appuyez sur Entrée pour continuer"
