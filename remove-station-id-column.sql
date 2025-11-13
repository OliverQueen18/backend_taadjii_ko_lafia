-- Script pour supprimer la colonne station_id de la table approvisionnement
-- Exécutez ce script dans MySQL pour corriger l'erreur

USE fuelticketdb;

-- Étape 1: Vérifier si la colonne existe
SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_DEFAULT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fuelticketdb' 
AND TABLE_NAME = 'approvisionnement' 
AND COLUMN_NAME = 'station_id';

-- Étape 2: Trouver et supprimer la contrainte de clé étrangère (si elle existe)
SET @fk_constraint = (
    SELECT CONSTRAINT_NAME 
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = 'fuelticketdb' 
    AND TABLE_NAME = 'approvisionnement' 
    AND COLUMN_NAME = 'station_id'
    AND CONSTRAINT_NAME != 'PRIMARY'
    LIMIT 1
);

-- Supprimer la contrainte si elle existe
SET @sql_drop_fk = IF(@fk_constraint IS NOT NULL, 
    CONCAT('ALTER TABLE `approvisionnement` DROP FOREIGN KEY `', @fk_constraint, '`'),
    'SELECT "Aucune contrainte FK trouvée" AS message'
);
PREPARE stmt FROM @sql_drop_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Étape 3: Supprimer la colonne station_id
ALTER TABLE `approvisionnement` DROP COLUMN IF EXISTS `station_id`;

-- Étape 4: Vérifier la structure finale
DESCRIBE `approvisionnement`;

-- Étape 5: Vérifier que region_id existe bien
SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_DEFAULT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'fuelticketdb' 
AND TABLE_NAME = 'approvisionnement' 
AND COLUMN_NAME IN ('station_id', 'region_id');

