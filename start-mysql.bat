@echo off
echo ========================================
echo   Démarrage de Taadji Ko Lafia avec MySQL
echo ========================================
echo.

echo 1. Vérification de la base de données MySQL...
"C:\wamp64\bin\mysql\mysql8.3.0\bin\mysql.exe" -u root -e "CREATE DATABASE IF NOT EXISTS fuelticketdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

echo.
echo 2. Démarrage de l'application Spring Boot...
echo.

mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=default

pause
