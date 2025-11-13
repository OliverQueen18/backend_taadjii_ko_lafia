-- Script pour créer la table pending_registration
-- Cette table stocke temporairement les inscriptions en attente de vérification
-- L'utilisateur ne sera créé dans la table user qu'après vérification du code

CREATE TABLE IF NOT EXISTS pending_registration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    telephone VARCHAR(20) UNIQUE,
    verification_code VARCHAR(6) NOT NULL,
    verification_code_expiry DATETIME NOT NULL,
    telephone_verification_code VARCHAR(6),
    telephone_verification_code_expiry DATETIME,
    telephone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    user_data_json TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at DATETIME,
    INDEX idx_email (email),
    INDEX idx_telephone (telephone),
    INDEX idx_verification_code_expiry (verification_code_expiry)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

