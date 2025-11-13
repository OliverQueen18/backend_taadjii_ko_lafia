-- Script SIMPLE pour corriger le problème station_id dans approvisionnement
-- Exécutez ce script dans MySQL

USE fuelticketdb;

-- Option 1: Rendre station_id nullable (si vous voulez garder la colonne pour compatibilité)
-- ALTER TABLE `approvisionnement` MODIFY COLUMN `station_id` BIGINT NULL;

-- Option 2: Supprimer complètement station_id (RECOMMANDÉ)
-- D'abord, supprimer la contrainte de clé étrangère si elle existe
SET @fk_name = (
    SELECT CONSTRAINT_NAME 
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = 'fuelticketdb' 
    AND TABLE_NAME = 'approvisionnement' 
    AND COLUMN_NAME = 'station_id'
    AND CONSTRAINT_NAME != 'PRIMARY'
    LIMIT 1
);

SET @drop_fk = IF(@fk_name IS NOT NULL, 
    CONCAT('ALTER TABLE `approvisionnement` DROP FOREIGN KEY `', @fk_name, '`'),
    'SELECT "Pas de contrainte FK à supprimer" AS info'
);
PREPARE stmt1 FROM @drop_fk;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

-- Ensuite, supprimer la colonne
ALTER TABLE `approvisionnement` DROP COLUMN `station_id`;

-- Vérifier le résultat
DESCRIBE `approvisionnement`;

