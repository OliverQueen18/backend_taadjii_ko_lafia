-- Script pour migrer la table 'approvisionnement' de station_id vers region_id
-- Exécuter ce script dans votre base de données MySQL
-- 
-- INSTRUCTIONS:
-- 1. Connectez-vous à MySQL: mysql -u root -p
-- 2. Exécutez: USE fuelticketdb;
-- 3. Exécutez ce script ligne par ligne ou en entier

USE fuelticketdb;

-- Étape 1: Vérifier la structure actuelle de la table
SHOW CREATE TABLE approvisionnement;

-- Étape 2: Trouver et supprimer l'ancienne contrainte de clé étrangère pour station_id
-- Exécutez d'abord la commande ci-dessus pour voir le nom exact de la contrainte
-- Puis exécutez (remplacez 'FK_approvisionnement_station' par le nom réel):
-- ALTER TABLE `approvisionnement` DROP FOREIGN KEY `FK_approvisionnement_station`;

-- OU si vous ne connaissez pas le nom exact, utilisez cette méthode:
SET @constraint_name = (
    SELECT CONSTRAINT_NAME 
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
    WHERE TABLE_SCHEMA = 'fuelticketdb' 
    AND TABLE_NAME = 'approvisionnement' 
    AND COLUMN_NAME = 'station_id'
    AND CONSTRAINT_NAME != 'PRIMARY'
    LIMIT 1
);

SET @sql = CONCAT('ALTER TABLE `approvisionnement` DROP FOREIGN KEY `', @constraint_name, '`');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Étape 3: Supprimer l'ancienne colonne station_id
ALTER TABLE `approvisionnement` DROP COLUMN `station_id`;

-- Étape 4: Vérifier que region_id existe (elle devrait déjà exister grâce à Hibernate)
-- Si elle n'existe pas, décommentez la ligne suivante:
-- ALTER TABLE `approvisionnement` ADD COLUMN `region_id` BIGINT NOT NULL AFTER `societe_id`;

-- Étape 5: Ajouter la contrainte de clé étrangère pour region_id (si elle n'existe pas déjà)
-- Ignorez l'erreur si la contrainte existe déjà
ALTER TABLE `approvisionnement` 
ADD CONSTRAINT `FK_approvisionnement_region` 
FOREIGN KEY (`region_id`) REFERENCES `region` (`id`);

-- Étape 6: Vérifier la structure finale
DESCRIBE `approvisionnement`;
