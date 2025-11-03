-- Script d'initialisation de la base de données MySQL pour Taadji Ko Lafia
-- Exécuter ce script après avoir créé la base de données

-- Créer la base de données si elle n'existe pas
CREATE DATABASE IF NOT EXISTS fuelticketdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Utiliser la base de données
USE fuelticketdb;

-- Vérifier que la base de données a été créée
SELECT 'Base de données fuelticketdb créée avec succès!' as message;
