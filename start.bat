@echo off
echo Starting Fuel Ticket Backend...
echo.
echo Make sure PostgreSQL is running on localhost:5432
echo Database: fuelticketdb
echo Username: fuelticket
echo Password: changeit
echo.
echo Starting application...
java -jar target/fuel-ticket-backend-0.0.1-SNAPSHOT.jar
pause
