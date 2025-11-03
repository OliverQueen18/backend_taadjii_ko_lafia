Write-Host "Starting Fuel Ticket Backend..." -ForegroundColor Green
Write-Host ""
Write-Host "Make sure PostgreSQL is running on localhost:5432" -ForegroundColor Yellow
Write-Host "Database: fuelticketdb" -ForegroundColor Yellow
Write-Host "Username: fuelticket" -ForegroundColor Yellow
Write-Host "Password: changeit" -ForegroundColor Yellow
Write-Host ""
Write-Host "Starting application..." -ForegroundColor Green

java -jar target/fuel-ticket-backend-0.0.1-SNAPSHOT.jar
