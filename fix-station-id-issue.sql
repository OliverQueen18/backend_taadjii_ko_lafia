-- Script pour corriger le problème de station_id dans la table approvisionnement
-- Ce script rend la colonne station_id nullable ou la supprime si elle n'est plus nécessaire
-- 
-- INSTRUCTIONS:
-- 1. Connectez-vous à MySQL: mysql -u root -p
-- 2. Exécutez: USE fuelticketdb;
-- 3. Exécutez ce script

USE fuelticketdb;

-- Vérifier si la colonne station_id existe
SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_DEFAULT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fuelticketdb' 
AND TABLE_NAME = 'approvisionnement' 
AND COLUMN_NAME = 'station_id';

-- Étape 1: Trouver et supprimer toutes les contraintes de clé étrangère liées à station_id
SET @constraint_name = (
    SELECT CONSTRAINT_NAME 
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = 'fuelticketdb' 
    AND TABLE_NAME = 'approvisionnement' 
    AND COLUMN_NAME = 'station_id'
    AND CONSTRAINT_NAME != 'PRIMARY'
    LIMIT 1
);

-- Si une contrainte existe, la supprimer
SET @sql = IF(@constraint_name IS NOT NULL, 
    CONCAT('ALTER TABLE `approvisionnement` DROP FOREIGN KEY `', @constraint_name, '`'),
    'SELECT "Aucune contrainte trouvée pour station_id" AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Étape 2: Rendre la colonne nullable (solution temporaire si vous voulez garder la colonne)
-- ALTER TABLE `approvisionnement` MODIFY COLUMN `station_id` BIGINT NULL;

-- OU Étape 2 alternative: Supprimer complètement la colonne (solution recommandée)
ALTER TABLE `approvisionnement` DROP COLUMN IF EXISTS `station_id`;

-- Vérifier la structure finale de la table
DESCRIBE `approvisionnement`;

-- Vérifier que region_id existe bien
SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_DEFAULT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fuelticketdb' 
AND TABLE_NAME = 'approvisionnement' 
AND COLUMN_NAME IN ('station_id', 'region_id');

