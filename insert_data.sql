-- 1. INSERT UTILISATEURS

-- 1. INSERT UTILISATEURS
-- Mot de passe pour tous : "password" -> $2a$10$aoeb8ZOo59113xGef2R8W.qbnyufwIlFJHOHOHSwpcL6pgHmMbEWm
INSERT INTO utilisateur (nom_utilisateur, mot_de_passe, email, role, date_inscription) VALUES 
('admin_main', '$2a$10$aoeb8ZOo59113xGef2R8W.qbnyufwIlFJHOHOHSwpcL6pgHmMbEWm', 'admin@amazon.com', 'ADMIN', NOW() - INTERVAL 1 YEAR),
('vendeur_tech', '$2a$10$aoeb8ZOo59113xGef2R8W.qbnyufwIlFJHOHOHSwpcL6pgHmMbEWm', 'tech@store.com', 'VENDEUR', NOW() - INTERVAL 6 MONTH),
('vendeur_mode', '$2a$10$aoeb8ZOo59113xGef2R8W.qbnyufwIlFJHOHOHSwpcL6pgHmMbEWm', 'mode@store.com', 'VENDEUR', NOW() - INTERVAL 5 MONTH),
('invest_alpha', '$2a$10$aoeb8ZOo59113xGef2R8W.qbnyufwIlFJHOHOHSwpcL6pgHmMbEWm', 'invest@capital.com', 'INVESTISSEUR', NOW() - INTERVAL 8 MONTH),
('client_alice', '$2a$10$aoeb8ZOo59113xGef2R8W.qbnyufwIlFJHOHOHSwpcL6pgHmMbEWm', 'alice@gmail.com', 'ACHETEUR', NOW() - INTERVAL 3 MONTH),
('client_bob', '$2a$10$aoeb8ZOo59113xGef2R8W.qbnyufwIlFJHOHOHSwpcL6pgHmMbEWm', 'bob@yahoo.com', 'ACHETEUR', NOW() - INTERVAL 2 MONTH),
('client_charlie', '$2a$10$aoeb8ZOo59113xGef2R8W.qbnyufwIlFJHOHOHSwpcL6pgHmMbEWm', 'charlie@hotmail.com', 'ACHETEUR', NOW() - INTERVAL 1 MONTH);

-- 2. INSERT ROLES SPÉCIFIQUES
INSERT INTO admin (id, permissions, derniere_connexion) VALUES 
((SELECT id FROM utilisateur WHERE nom_utilisateur='admin_main'), 'ALL_ACCESS', NOW());

INSERT INTO investisseur (id, montant_investissement, roi) VALUES 
((SELECT id FROM utilisateur WHERE nom_utilisateur='invest_alpha'), 50000.00, 12.5);

INSERT INTO acheteur (id, adresse_livraison, numero_telephone) VALUES 
((SELECT id FROM utilisateur WHERE nom_utilisateur='client_alice'), '123 Rue de Paris, 75001 Paris', '0601020304'),
((SELECT id FROM utilisateur WHERE nom_utilisateur='client_bob'), '456 Avenue de Lyon, 69002 Lyon', '0611223344'),
((SELECT id FROM utilisateur WHERE nom_utilisateur='client_charlie'), '789 Boulevard de Nice, 06000 Nice', '0699887766');

-- 3. INSERT MAGASINS (Liés aux Vendeurs)
INSERT INTO magasin (nom, adresse, numero_telephone, email, heures_ouverture, note, id_vendeur) VALUES 
('Tech World', '10 Rue du Numérique, Paris', '0123456789', 'contact@techworld.com', '09:00-18:00', 4.5, (SELECT id FROM utilisateur WHERE nom_utilisateur='vendeur_tech')),
('Mode & Style', '20 Rue de la Mode, Lyon', '0987654321', 'info@modestyle.com', '10:00-19:00', 4.2, (SELECT id FROM utilisateur WHERE nom_utilisateur='vendeur_mode'));

-- Mise à jour de la table Vendeur (car référence circulaire magasin <-> vendeur)
INSERT INTO vendeur (id, id_magasin, commission, objectif_ventes, ventes_totales) VALUES 
((SELECT id FROM utilisateur WHERE nom_utilisateur='vendeur_tech'), (SELECT id FROM magasin WHERE nom='Tech World'), 5.0, 100000.00, 45000.00),
((SELECT id FROM utilisateur WHERE nom_utilisateur='vendeur_mode'), (SELECT id FROM magasin WHERE nom='Mode & Style'), 4.5, 50000.00, 21000.00);

-- 4. INSERT PRODUITS (Tech World)
INSERT INTO produit (asin, nom, description, prix, note, nombre_avis, rang, categorie, url_image, url_produit, nombre_vendeurs, est_disponible, id_magasin) VALUES 
('B08N5N6V9T', 'Smartphone Galaxy S21', 'Samsung Galaxy S21 5G 128Go', 799.00, 4.8, 150, 1, 'Electronics', 'https://placehold.co/300?text=Smartphone', 'https://amazon.com/dp/B08N5N6V9T', 5, TRUE, (SELECT id FROM magasin WHERE nom='Tech World')),
('B09G3F1W3J', 'Casque Sony WH-1000XM4', 'Casque à réduction de bruit sans fil', 299.00, 4.9, 320, 2, 'Audio', 'https://placehold.co/300?text=Casque+Sony', 'https://amazon.com/dp/B09G3F1W3J', 10, TRUE, (SELECT id FROM magasin WHERE nom='Tech World')),
('B07W5JK7M1', 'Clavier Mécanique RGB', 'Clavier gamer switch bleu', 49.99, 4.3, 85, 15, 'Informatique', 'https://placehold.co/300?text=Clavier+RGB', 'https://amazon.com/dp/B07W5JK7M1', 2, TRUE, (SELECT id FROM magasin WHERE nom='Tech World'));

-- 4b. INSERT PRODUITS (Mode & Style)
INSERT INTO produit (asin, nom, description, prix, note, nombre_avis, rang, categorie, url_image, url_produit, nombre_vendeurs, est_disponible, id_magasin) VALUES 
('B08L5J4K3M', 'Jean Slim Homme', 'Jean bleu délavé coupe slim', 39.90, 4.1, 45, 5, 'Vêtements', 'https://placehold.co/300?text=Jean+Slim', 'https://amazon.com/dp/B08L5J4K3M', 1, TRUE, (SELECT id FROM magasin WHERE nom='Mode & Style')),
('B07T6H2N9Q', 'Robe d''été fleurie', 'Robe légère pour l''été', 29.50, 4.6, 110, 3, 'Vêtements', 'https://placehold.co/300?text=Robe+Ete', 'https://amazon.com/dp/B07T6H2N9Q', 1, TRUE, (SELECT id FROM magasin WHERE nom='Mode & Style'));

-- 5. INSERT STOCK
INSERT INTO stock (id_produit, quantite, seuil_min, seuil_max, emplacement) 
SELECT id, FLOOR(RAND() * 500), 10, 1000, CONCAT('Allee ', FLOOR(RAND()*10)) FROM produit;

-- 6. INSERT COMMANDES & LIGNE COMMANDE (pour générer de l'historique)
-- Commande 1 : Alice achète un Smartphone
INSERT INTO commande (id_acheteur, id_vendeur, date_commande, statut, sous_total, taxe, frais_livraison, montant_total, methode_paiement, adresse_livraison) VALUES 
((SELECT id FROM utilisateur WHERE nom_utilisateur='client_alice'), 
 (SELECT id FROM utilisateur WHERE nom_utilisateur='vendeur_tech'), 
 NOW() - INTERVAL 2 DAY, 'LIVREE', 799.00, 159.80, 0.00, 958.80, 'Carte Bancaire', '123 Rue de Paris');

SET @cmd1_id = LAST_INSERT_ID();
SET @prod1_id = (SELECT id FROM produit WHERE asin='B08N5N6V9T');

INSERT INTO ligne_commande (id_commande, id_produit, quantite, prix_unitaire, sous_total) VALUES 
(@cmd1_id, @prod1_id, 1, 799.00, 799.00);

-- Génération de la vente associée
INSERT INTO vente (id_commande, id_produit, id_vendeur, id_acheteur, quantite, prix_unitaire, montant_total, commission, montant_net, date_vente, statut, type_paiement) VALUES 
(@cmd1_id, @prod1_id, (SELECT id FROM utilisateur WHERE nom_utilisateur='vendeur_tech'), (SELECT id FROM utilisateur WHERE nom_utilisateur='client_alice'), 1, 799.00, 799.00, 39.95, 759.05, NOW() - INTERVAL 2 DAY, 'VALIDEE', 'Carte Bancaire');


-- Commande 2 : Bob achète un casque et un clavier
INSERT INTO commande (id_acheteur, id_vendeur, date_commande, statut, sous_total, taxe, frais_livraison, montant_total, methode_paiement, adresse_livraison) VALUES 
((SELECT id FROM utilisateur WHERE nom_utilisateur='client_bob'), 
 (SELECT id FROM utilisateur WHERE nom_utilisateur='vendeur_tech'), 
 NOW() - INTERVAL 5 HOUR, 'EXPEDIEE', 348.99, 69.80, 5.00, 423.79, 'PayPal', '456 Avenue de Lyon');

SET @cmd2_id = LAST_INSERT_ID();
SET @prod2_id = (SELECT id FROM produit WHERE asin='B09G3F1W3J');
SET @prod3_id = (SELECT id FROM produit WHERE asin='B07W5JK7M1');

INSERT INTO ligne_commande (id_commande, id_produit, quantite, prix_unitaire, sous_total) VALUES 
(@cmd2_id, @prod2_id, 1, 299.00, 299.00),
(@cmd2_id, @prod3_id, 1, 49.99, 49.99);

-- Ventes associées pour Bob
INSERT INTO vente (id_commande, id_produit, id_vendeur, id_acheteur, quantite, prix_unitaire, montant_total, commission, montant_net, date_vente, statut, type_paiement) VALUES 
(@cmd2_id, @prod2_id, (SELECT id FROM utilisateur WHERE nom_utilisateur='vendeur_tech'), (SELECT id FROM utilisateur WHERE nom_utilisateur='client_bob'), 1, 299.00, 299.00, 14.95, 284.05, NOW() - INTERVAL 5 HOUR, 'VALIDEE', 'PayPal'),
(@cmd2_id, @prod3_id, (SELECT id FROM utilisateur WHERE nom_utilisateur='vendeur_tech'), (SELECT id FROM utilisateur WHERE nom_utilisateur='client_bob'), 1, 49.99, 49.99, 2.50, 47.49, NOW() - INTERVAL 5 HOUR, 'VALIDEE', 'PayPal');

-- Commande 3 : Charlie - En cours
INSERT INTO commande (id_acheteur, id_vendeur, date_commande, statut, sous_total, taxe, frais_livraison, montant_total, methode_paiement, adresse_livraison) VALUES 
((SELECT id FROM utilisateur WHERE nom_utilisateur='client_charlie'), 
 (SELECT id FROM utilisateur WHERE nom_utilisateur='vendeur_mode'), 
 NOW() - INTERVAL 30 MINUTE, 'EN_ATTENTE', 29.50, 5.90, 4.50, 39.90, 'Carte Bancaire', '789 Boulevard de Nice');

SET @cmd3_id = LAST_INSERT_ID();
SET @prod4_id = (SELECT id FROM produit WHERE asin='B07T6H2N9Q');

INSERT INTO ligne_commande (id_commande, id_produit, quantite, prix_unitaire, sous_total) VALUES 
(@cmd3_id, @prod4_id, 1, 29.50, 29.50);

-- Vente (Statut Enregistrée, non validée)
INSERT INTO vente (id_commande, id_produit, id_vendeur, id_acheteur, quantite, prix_unitaire, montant_total, commission, montant_net, date_vente, statut, type_paiement) VALUES 
(@cmd3_id, @prod4_id, (SELECT id FROM utilisateur WHERE nom_utilisateur='vendeur_mode'), (SELECT id FROM utilisateur WHERE nom_utilisateur='client_charlie'), 1, 29.50, 29.50, 1.33, 28.17, NOW() - INTERVAL 30 MINUTE, 'ENREGISTREE', 'Carte Bancaire');


-- 7. INSERT AVIS
INSERT INTO avis (id_produit, id_acheteur, note, commentaire, est_verifie) VALUES 
(@prod1_id, (SELECT id FROM utilisateur WHERE nom_utilisateur='client_alice'), 5, 'Super téléphone, très rapide !', TRUE),
(@prod2_id, (SELECT id FROM utilisateur WHERE nom_utilisateur='client_bob'), 5, 'Le son est incroyable.', TRUE);

-- 8. INSERT ALERTES (Exemples pour le dashboard)
INSERT INTO alerte (id_utilisateur, type, message, priorite, est_lu) VALUES 
((SELECT id FROM utilisateur WHERE nom_utilisateur='vendeur_tech'), 'STOCK_BAS', 'Attention, le stock du clavier est faible.', 'Haute', FALSE),
((SELECT id FROM utilisateur WHERE nom_utilisateur='admin_main'), 'NOUVEL_UTILISATEUR', 'Nouvel investisseur inscrit : Invest Alpha', 'Faible', FALSE);

-- 9. INSERT STATISTIQUES (Données pré-calculées pour affichage direct)
-- Stats Journalières (Aujourd'hui)
INSERT INTO statistique (type_statistique, periode, date_debut, date_fin, produits_vendus, revenu_total, commandes_total, commandes_completees, panier_moyen, revenu_net) VALUES 
('GLOBAL', 'journalier', CURDATE(), CURDATE(), 3, 1147.99, 2, 1, 573.99, 1090.59);

-- Stats Hebdomadaires
INSERT INTO statistique (type_statistique, periode, date_debut, date_fin, produits_vendus, revenu_total, commandes_total, commandes_completees, panier_moyen, revenu_net) VALUES 
('GLOBAL', 'hebdomadaire', SUBDATE(CURDATE(), WEEKDAY(CURDATE())), ADDDATE(CURDATE(), 6 - WEEKDAY(CURDATE())), 15, 5400.00, 8, 7, 675.00, 5100.00);

-- Stats Mensuelles
INSERT INTO statistique (type_statistique, periode, date_debut, date_fin, produits_vendus, revenu_total, commandes_total, commandes_completees, panier_moyen, revenu_net) VALUES 
('GLOBAL', 'mensuel', DATE_FORMAT(NOW() ,'%Y-%m-01'), LAST_DAY(NOW()), 45, 18500.00, 25, 24, 740.00, 17200.00);
