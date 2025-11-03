-- Insert sample data for development
-- Compatible with the new database architecture
-- This file is automatically loaded by Spring Boot on startup
-- Make sure spring.jpa.defer-datasource-initialization=true is set in application.properties

-- Insert sample users with all required fields
-- Password: "password123" (hashed with BCrypt)
INSERT INTO `user` (id, nom, prenom, email, password, telephone, role, email_verified, created_at, updated_at) VALUES 
(1, 'Admin', 'System', 'admin@taadjikolafia.ml', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '+22370123456', 'ADMIN', true, NOW(), NOW()),
(2, 'Dupont', 'Jean', 'jean.dupont@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '+22370111111', 'CITOYEN', true, NOW(), NOW()),
(3, 'Traoré', 'Amadou', 'amadou.traore@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '+22370222222', 'CITOYEN', true, NOW(), NOW()),
(4, 'Diallo', 'Fatou', 'fatou.diallo@email.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '+22370333333', 'CITOYEN', false, NOW(), NOW()),
-- Station managers
(5, 'Total', 'Manager', 'manager.total@total.ml', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '+22320112233', 'STATION', true, NOW(), NOW()),
(6, 'Shell', 'Manager', 'manager.shell@shell.ml', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '+22320114455', 'STATION', true, NOW(), NOW()),
(7, 'BP', 'Manager', 'manager.bp@bp.ml', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '+22320116677', 'STATION', true, NOW(), NOW());

-- Insert sample stations with coordinates and managers
INSERT INTO station (id, nom, localisation, adresse_complete, capacite_journaliere, latitude, longitude, telephone, email, site_web, horaires_ouverture, is_ouverte, manager_id) VALUES 
(1, 'STATION TOTAL CENTRE', 'CENTRE-VILLE', 'Avenue de la République, Bamako, Mali', 5000.0, 12.6500, -8.0000, '+22320112233', 'total.centre@total.ml', 'www.total.ml', '06:00-22:00', true, 5),
(2, 'STATION SHELL NORD', 'QUARTIER NORD', 'Route Nationale 1, Bamako, Mali', 4000.0, 12.6800, -7.9800, '+22320114455', 'shell.nord@shell.ml', 'www.shell.ml', '06:00-22:00', true, 6),
(3, 'STATION BP SUD', 'QUARTIER SUD', 'Boulevard de la Paix, Bamako, Mali', 6000.0, 12.6200, -8.0200, '+22320116677', 'bp.sud@bp.ml', 'www.bp.ml', '06:00-22:00', true, 7);

-- Insert fuel stocks for each station
INSERT INTO fuel_stock (id, station_id, fuel_type, stock_disponible, capacite_maximale, prix_par_litre, is_disponible) VALUES 
-- Station Total Centre
(1, 1, 'ESSENCE', 1500.0, 2000.0, 750.0, true),
(2, 1, 'DIESEL', 1200.0, 2000.0, 720.0, true),
(3, 1, 'GPL', 300.0, 500.0, 450.0, true),
-- Station Shell Nord
(4, 2, 'ESSENCE', 1000.0, 1500.0, 755.0, true),
(5, 2, 'DIESEL', 1500.0, 2000.0, 725.0, true),
(6, 2, 'KEROSENE', 200.0, 300.0, 650.0, true),
-- Station BP Sud
(7, 3, 'ESSENCE', 2000.0, 2500.0, 745.0, true),
(8, 3, 'DIESEL', 1800.0, 2500.0, 715.0, true),
(9, 3, 'GPL', 500.0, 800.0, 440.0, true),
(10, 3, 'KEROSENE', 300.0, 500.0, 640.0, true);

-- Insert sale schedules for fuel sales
INSERT INTO sale_schedule (id, station_id, fuel_type, sale_date, start_time, end_time, available_quantity, max_quantity_per_ticket, max_tickets_per_day, is_active) VALUES 
-- Station Total Centre - Today's schedules
(1, 1, 'ESSENCE', CURDATE(), '08:00:00', '18:00:00', 500.0, 20.0, 50, true),
(2, 1, 'DIESEL', CURDATE(), '08:00:00', '18:00:00', 400.0, 25.0, 40, true),
-- Station Shell Nord - Today's schedules
(3, 2, 'ESSENCE', CURDATE(), '07:00:00', '20:00:00', 300.0, 15.0, 60, true),
(4, 2, 'DIESEL', CURDATE(), '07:00:00', '20:00:00', 500.0, 20.0, 50, true),
-- Station BP Sud - Today's schedules
(5, 3, 'ESSENCE', CURDATE(), '06:00:00', '22:00:00', 800.0, 30.0, 80, true),
(6, 3, 'DIESEL', CURDATE(), '06:00:00', '22:00:00', 600.0, 25.0, 70, true),
(7, 3, 'GPL', CURDATE(), '06:00:00', '22:00:00', 200.0, 50.0, 30, true);

-- Insert sample tickets
INSERT INTO ticket (id, numero_ticket, numero_ordre, date_approvisionnement, type_carburant, quantite, statut, citoyen_id, station_id, sale_schedule_id, nom_citoyen, prenom_citoyen, email_citoyen, telephone_citoyen, date_creation, date_expiration, date_derniere_miseajour, is_expired) VALUES 
-- Valid tickets
(1, 'TKT-001-2025', '001', DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'ESSENCE', 20.0, 'VALIDE', 2, 1, 1, 'Dupont', 'Jean', 'jean.dupont@email.com', '+22370111111', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), false),
(2, 'TKT-002-2025', '002', DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'DIESEL', 25.0, 'VALIDE', 3, 1, 2, 'Traoré', 'Amadou', 'amadou.traore@email.com', '+22370222222', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), false),
(3, 'TKT-003-2025', '003', DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'ESSENCE', 15.0, 'EN_ATTENTE', 2, 2, 3, 'Dupont', 'Jean', 'jean.dupont@email.com', '+22370111111', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), false),
-- Served ticket
(4, 'TKT-004-2025', '004', CURDATE(), 'ESSENCE', 30.0, 'SERVI', 3, 3, 5, 'Traoré', 'Amadou', 'amadou.traore@email.com', '+22370222222', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 25 DAY), NOW(), false),
-- Expired ticket
(5, 'TKT-005-2025', '005', DATE_SUB(CURDATE(), INTERVAL 35 DAY), 'DIESEL', 20.0, 'EXPIRE', 2, 2, 4, 'Dupont', 'Jean', 'jean.dupont@email.com', '+22370111111', DATE_SUB(NOW(), INTERVAL 35 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), true);

-- Insert payments for tickets
-- Note: Hibernate generates column names in snake_case automatically
INSERT INTO payment (id, montant, date_paiement, mode_paiement, ticket_id) VALUES 
(1, 15000.0, DATE_SUB(NOW(), INTERVAL 5 DAY), 'CARTE_BANCAIRE', 4),
(2, 18000.0, DATE_SUB(NOW(), INTERVAL 4 DAY), 'MOBILE_MONEY', 1),
(3, 14400.0, DATE_SUB(NOW(), INTERVAL 3 DAY), 'ESPECES', 2);

