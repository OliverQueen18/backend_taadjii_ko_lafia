-- Script SIMPLIFIÉ pour migrer la table 'approvisionnement' de station_id vers region_id
-- Exécuter ce script dans votre base de données MySQL
-- 
-- INSTRUCTIONS:
-- 1. Exécutez d'abord: SHOW CREATE TABLE approvisionnement;
-- 2. Notez le nom exact de la contrainte de clé étrangère pour station_id (ex: FK_approvisionnement_station)
-- 3. Remplacez 'FK_approvisionnement_station' dans ce script par le nom réel
-- 4. Exécutez ce script

USE fuelticketdb;

-- Supprimer l'ancienne contrainte de clé étrangère (remplacez par le nom réel)
-- ALTER TABLE `approvisionnement` DROP FOREIGN KEY `FK_approvisionnement_station`;

-- Supprimer l'ancienne colonne station_id
ALTER TABLE `approvisionnement` DROP COLUMN `station_id`;

-- La colonne region_id devrait déjà exister (ajoutée par Hibernate)
-- Si elle n'existe pas, décommentez la ligne suivante:
-- ALTER TABLE `approvisionnement` ADD COLUMN `region_id` BIGINT NOT NULL AFTER `societe_id`;

-- Ajouter la contrainte de clé étrangère pour region_id (si elle n'existe pas)
-- ALTER TABLE `approvisionnement` 
-- ADD CONSTRAINT `FK_approvisionnement_region` 
-- FOREIGN KEY (`region_id`) REFERENCES `region` (`id`);

-- Vérifier la structure finale
DESCRIBE `approvisionnement`;

