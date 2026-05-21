
DROP DATABASE IF EXISTS amazon_bestsellers;
CREATE DATABASE amazon_bestsellers CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE amazon_bestsellers;

-- ========================================
-- TABLE: Utilisateur
-- ========================================
CREATE TABLE utilisateur (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nom_utilisateur VARCHAR(100) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    date_inscription DATETIME DEFAULT CURRENT_TIMESTAMP,
    role ENUM('ADMIN', 'VENDEUR', 'INVESTISSEUR', 'ACHETEUR') NOT NULL,
    est_actif BOOLEAN DEFAULT TRUE,
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_nom_utilisateur (nom_utilisateur)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLE: Admin
-- ========================================
CREATE TABLE admin (
    id BIGINT PRIMARY KEY,
    permissions TEXT,
    derniere_connexion DATETIME,
    FOREIGN KEY (id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    INDEX idx_derniere_connexion (derniere_connexion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLE: Investisseur
-- ========================================
CREATE TABLE investisseur (
    id BIGINT PRIMARY KEY,
    montant_investissement DECIMAL(15,2) DEFAULT 0.00,
    roi DECIMAL(5,2) DEFAULT 0.00,
    FOREIGN KEY (id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    INDEX idx_montant_investissement (montant_investissement),
    INDEX idx_roi (roi)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLE: Magasin
-- ========================================
CREATE TABLE magasin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(200) NOT NULL,
    adresse VARCHAR(255),
    numero_telephone VARCHAR(20),
    email VARCHAR(150),
    heures_ouverture VARCHAR(100),
    note DECIMAL(3,2) DEFAULT 0.00,
    id_vendeur BIGINT NULL,
    INDEX idx_nom (nom),
    INDEX idx_note (note)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLE: Vendeur
-- ========================================
CREATE TABLE vendeur (
    id BIGINT PRIMARY KEY,
    id_magasin BIGINT,
    commission DECIMAL(5,2) DEFAULT 0.00,
    objectif_ventes DECIMAL(12,2) DEFAULT 0.00,
    ventes_totales DECIMAL(12,2) DEFAULT 0.00,
    FOREIGN KEY (id) REFERENCES utilisateur(id) ON DELETE CASCADE,
    FOREIGN KEY (id_magasin) REFERENCES magasin(id) ON DELETE SET NULL,
    INDEX idx_magasin (id_magasin),
    INDEX idx_ventes_totales (ventes_totales)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLE: Acheteur
-- ========================================
CREATE TABLE acheteur (
    id BIGINT PRIMARY KEY,
    adresse_livraison VARCHAR(255),
    numero_telephone VARCHAR(20),
    FOREIGN KEY (id) REFERENCES utilisateur(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLE: Produit
-- ========================================
CREATE TABLE produit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    asin VARCHAR(20) NOT NULL UNIQUE,
    nom VARCHAR(500),
    description TEXT,
    prix DECIMAL(10,2) NOT NULL,
    note DECIMAL(3,2) DEFAULT 0.00,
    nombre_avis INT DEFAULT 0,
    rang INT DEFAULT 0,
    categorie VARCHAR(100),
    url_image VARCHAR(500),
    url_produit VARCHAR(500) NOT NULL,
    nombre_vendeurs INT DEFAULT 1,
    est_disponible BOOLEAN DEFAULT TRUE,
    date_ajout DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_magasin BIGINT,
    FOREIGN KEY (id_magasin) REFERENCES magasin(id) ON DELETE SET NULL,
    INDEX idx_asin (asin),
    INDEX idx_categorie (categorie),
    INDEX idx_rang (rang),
    INDEX idx_prix (prix),
    INDEX idx_note (note),
    INDEX idx_est_disponible (est_disponible),
    INDEX idx_magasin (id_magasin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLE: Stock
-- ========================================
CREATE TABLE stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_produit BIGINT NOT NULL,
    quantite INT DEFAULT 0,
    seuil_min INT DEFAULT 10,
    seuil_max INT DEFAULT 1000,
    derniere_maj DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    emplacement VARCHAR(100),
    FOREIGN KEY (id_produit) REFERENCES produit(id) ON DELETE CASCADE,
    UNIQUE KEY unique_produit_stock (id_produit),
    INDEX idx_quantite (quantite),
    INDEX idx_seuil_min (seuil_min)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLE: Commande
-- ========================================
CREATE TABLE commande (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_acheteur BIGINT NOT NULL,
    id_vendeur BIGINT,
    date_commande DATETIME DEFAULT CURRENT_TIMESTAMP,
    -- Change this line in the commande table creation:
statut ENUM('EN_ATTENTE', 'CONFIRMEE', 'EXPEDIEE', 'LIVREE', 'ANNULEE') DEFAULT 'EN_ATTENTE',
    sous_total DECIMAL(10,2) DEFAULT 0.00,
    taxe DECIMAL(10,2) DEFAULT 0.00,
    frais_livraison DECIMAL(10,2) DEFAULT 0.00,
    montant_total DECIMAL(10,2) DEFAULT 0.00,
    methode_paiement VARCHAR(50),
    adresse_livraison VARCHAR(255),
    FOREIGN KEY (id_acheteur) REFERENCES acheteur(id) ON DELETE CASCADE,
    FOREIGN KEY (id_vendeur) REFERENCES vendeur(id) ON DELETE SET NULL,
    INDEX idx_acheteur (id_acheteur),
    INDEX idx_vendeur (id_vendeur),
    INDEX idx_date (date_commande),
    INDEX idx_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLE: Ligne_commande
-- ========================================
CREATE TABLE ligne_commande (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_commande BIGINT NOT NULL,
    id_produit BIGINT NOT NULL,
    quantite INT NOT NULL DEFAULT 1 CHECK (quantite > 0),
    prix_unitaire DECIMAL(10,2) NOT NULL,
    sous_total DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_commande) REFERENCES commande(id) ON DELETE CASCADE,
    FOREIGN KEY (id_produit) REFERENCES produit(id) ON DELETE CASCADE,
    INDEX idx_commande (id_commande),
    INDEX idx_produit (id_produit)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLE: Vente
-- ========================================
CREATE TABLE vente (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_commande BIGINT NOT NULL,
    id_produit BIGINT NOT NULL,
    id_vendeur BIGINT,
    id_acheteur BIGINT NOT NULL,
    quantite INT NOT NULL DEFAULT 1,
    prix_unitaire DECIMAL(10,2) NOT NULL,
    montant_total DECIMAL(10,2) NOT NULL,
    commission DECIMAL(10,2) DEFAULT 0.00,
    montant_net DECIMAL(10,2) NOT NULL,
    date_vente DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('ENREGISTREE', 'VALIDEE', 'ANNULEE', 'REMBOURSEE') DEFAULT 'ENREGISTREE',
    type_paiement VARCHAR(50),
    frais_transaction DECIMAL(10,2) DEFAULT 0.00,
    FOREIGN KEY (id_commande) REFERENCES commande(id) ON DELETE CASCADE,
    FOREIGN KEY (id_produit) REFERENCES produit(id) ON DELETE CASCADE,
    FOREIGN KEY (id_vendeur) REFERENCES vendeur(id) ON DELETE SET NULL,
    FOREIGN KEY (id_acheteur) REFERENCES acheteur(id) ON DELETE CASCADE,
    INDEX idx_commande (id_commande),
    INDEX idx_produit (id_produit),
    INDEX idx_vendeur (id_vendeur),
    INDEX idx_acheteur (id_acheteur),
    INDEX idx_date_vente (date_vente),
    INDEX idx_statut (statut),
    INDEX idx_montant_total (montant_total)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLE: Avis
-- ========================================
CREATE TABLE avis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_produit BIGINT NOT NULL,
    id_acheteur BIGINT NOT NULL,
    note DECIMAL(3,2) NOT NULL CHECK (note >= 0 AND note <= 5),
    commentaire TEXT,
    date_avis DATETIME DEFAULT CURRENT_TIMESTAMP,
    est_verifie BOOLEAN DEFAULT FALSE,
    nombre_utiles INT DEFAULT 0,
    FOREIGN KEY (id_produit) REFERENCES produit(id) ON DELETE CASCADE,
    FOREIGN KEY (id_acheteur) REFERENCES acheteur(id) ON DELETE CASCADE,
    INDEX idx_produit (id_produit),
    INDEX idx_acheteur (id_acheteur),
    INDEX idx_date (date_avis),
    INDEX idx_note (note),
    INDEX idx_est_verifie (est_verifie)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABLE: Alerte
-- ========================================
CREATE TABLE alerte (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_utilisateur BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    est_lu BOOLEAN DEFAULT FALSE,
    priorite ENUM('Faible', 'Moyenne', 'Haute', 'Critique') DEFAULT 'Moyenne',
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id) ON DELETE CASCADE,
    INDEX idx_utilisateur (id_utilisateur),
    INDEX idx_est_lu (est_lu),
    INDEX idx_priorite (priorite),
    INDEX idx_date_creation (date_creation)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- ========================================
-- TABLE: Statistique
-- ========================================
CREATE TABLE statistique (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_statistique VARCHAR(50) NOT NULL,
    periode VARCHAR(20) NOT NULL, -- 'journalier', 'hebdomadaire', 'mensuel', 'annuel'
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    
    -- Statistiques produits
    produits_vendus INT DEFAULT 0,
    revenu_total DECIMAL(15,2) DEFAULT 0.00,
    produit_plus_vendu_id BIGINT,
    categorie_plus_vendue VARCHAR(100),
    
    -- Statistiques utilisateurs
    nouveaux_utilisateurs INT DEFAULT 0,
    utilisateurs_actifs INT DEFAULT 0,
    acheteurs_actifs INT DEFAULT 0,
    vendeurs_actifs INT DEFAULT 0,
    
    -- Statistiques ventes
    commandes_total INT DEFAULT 0,
    commandes_completees INT DEFAULT 0,
    commandes_annulees INT DEFAULT 0,
    panier_moyen DECIMAL(10,2) DEFAULT 0.00,
    
    -- Statistiques financières
    commission_totale DECIMAL(15,2) DEFAULT 0.00,
    frais_transaction_total DECIMAL(15,2) DEFAULT 0.00,
    revenu_net DECIMAL(15,2) DEFAULT 0.00,
    
    -- Performances
    taux_conversion DECIMAL(5,2) DEFAULT 0.00, -- Taux de conversion des visiteurs en acheteurs
    satisfaction_moyenne DECIMAL(3,2) DEFAULT 0.00, -- Note moyenne des avis
    
    -- Stock
    produits_en_rupture INT DEFAULT 0,
    produits_reapprovisionnes INT DEFAULT 0,
    
    -- Métadonnées
    date_calcul DATETIME DEFAULT CURRENT_TIMESTAMP,
    est_valide BOOLEAN DEFAULT TRUE,
    
    -- Clés étrangères
    FOREIGN KEY (produit_plus_vendu_id) REFERENCES produit(id) ON DELETE SET NULL,
    
    -- Index pour optimisation
    INDEX idx_type_periode (type_statistique, periode),
    INDEX idx_dates (date_debut, date_fin),
    INDEX idx_date_calcul (date_calcul),
    UNIQUE KEY unique_stat_periode (type_statistique, periode, date_debut, date_fin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
