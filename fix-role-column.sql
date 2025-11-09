-- Script pour corriger la taille de la colonne 'role' dans la table 'user'
-- Exécuter ce script dans votre base de données MySQL

USE fuelticketdb;

-- Modifier la colonne role pour accepter jusqu'à 20 caractères
-- Cela permet de stocker "GESTIONNAIRE" (12 caractères) et d'autres valeurs futures
ALTER TABLE `user` MODIFY COLUMN `role` VARCHAR(20) NOT NULL;

-- Vérifier que la modification a été appliquée
DESCRIBE `user`;

