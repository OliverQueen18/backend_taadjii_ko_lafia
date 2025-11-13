-- Script RAPIDE pour corriger la table approvisionnement
-- Exécutez ce script dans MySQL
-- 
-- INSTRUCTIONS:
-- 1. mysql -u root -p
-- 2. USE fuelticketdb;
-- 3. Copiez-collez les commandes ci-dessous une par une

USE fuelticketdb;

-- D'abord, trouvons le nom de la contrainte
SELECT CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'fuelticketdb' 
AND TABLE_NAME = 'approvisionnement' 
AND COLUMN_NAME = 'station_id'
AND CONSTRAINT_NAME != 'PRIMARY';

-- Copiez le nom de la contrainte affiché ci-dessus et utilisez-le dans la commande suivante:
-- ALTER TABLE `approvisionnement` DROP FOREIGN KEY `NOM_DE_LA_CONTRAINTE_ICI`;

-- OU si vous préférez, exécutez cette commande qui devrait fonctionner dans la plupart des cas:
ALTER TABLE `approvisionnement` DROP FOREIGN KEY `FK_approvisionnement_station`;

-- Si la commande ci-dessus échoue avec "Unknown constraint", essayez:
-- ALTER TABLE `approvisionnement` DROP FOREIGN KEY `approvisionnement_ibfk_1`;
-- (ou approvisionnement_ibfk_2, etc. selon votre base de données)

-- Ensuite, supprimez la colonne
ALTER TABLE `approvisionnement` DROP COLUMN `station_id`;

-- Vérifiez le résultat
DESCRIBE `approvisionnement`;
