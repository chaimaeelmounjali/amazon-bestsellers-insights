package com.example.amazonbestseller.config;

import com.example.amazonbestseller.entity.*;
import com.example.amazonbestseller.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
@org.springframework.core.annotation.Order(10)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProduitRepository produitRepository;
    private final com.example.amazonbestseller.repository.MagasinRepository magasinRepository;
    private final VendeurRepository vendeurRepository;
    private final PasswordEncoder passwordEncoder;
    private final StockRepository stockRepository;
    private final CommandeRepository commandeRepository;
    private final AcheteurRepository acheteurRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AdminRepository adminRepository;
    private final InvestisseurRepository investisseurRepository;
    private final VenteRepository venteRepository;
    private final AvisRepository avisRepository;
    private final jakarta.persistence.EntityManager entityManager; // Inject EntityManager

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Vérification des données de test...");

        // 0. SELF-HEALING: Réparer les incohérences de données (Utilisateurs orphelins)
        reparerIncoherencesUtilisateurs();

        // ==================== VENDEUR 1 (High-Tech) ====================
        Vendeur vendeur1 = vendeurRepository.findByEmail("tech@store.com").orElseGet(() -> {
            Vendeur v = new Vendeur();
            v.setNomUtilisateur("Vendeur Tech");
            v.setEmail("tech@store.com");
            v.setMotDePasse(passwordEncoder.encode("password"));
            v.setRole(Utilisateur.Role.VENDEUR);
            v.setEstActif(true);
            return vendeurRepository.save(v);
        });

        // ==================== AUTRES UTILISATEURS (Admin, Invest, Buyer)
        // ====================
        if (utilisateurRepository.findByEmail("admin@amazon.com").isEmpty()) {
            Admin admin = new Admin();
            admin.setNomUtilisateur("Admin Admin");
            admin.setEmail("admin@amazon.com");
            admin.setMotDePasse(passwordEncoder.encode("password"));
            admin.setRole(Utilisateur.Role.ADMIN);
            admin.setPermissions("ALL_ACCESS");
            admin.setEstActif(true);
            adminRepository.save(admin);
        }

        if (utilisateurRepository.findByEmail("invest@capital.com").isEmpty()) {
            Investisseur invest = new Investisseur();
            invest.setNomUtilisateur("Investisseur Alpha");
            invest.setEmail("invest@capital.com");
            invest.setMotDePasse(passwordEncoder.encode("password"));
            invest.setRole(Utilisateur.Role.INVESTISSEUR);
            invest.setMontantInvestissement(BigDecimal.valueOf(100000));
            invest.setRoi(BigDecimal.valueOf(15.5));
            invest.setEstActif(true);
            investisseurRepository.save(invest);
        }

        if (utilisateurRepository.findByEmail("alice@gmail.com").isEmpty()) {
            Acheteur alice = new Acheteur();
            alice.setNomUtilisateur("client_alice");
            alice.setEmail("alice@gmail.com");
            alice.setMotDePasse(passwordEncoder.encode("password"));
            alice.setRole(Utilisateur.Role.ACHETEUR);
            alice.setAdresseLivraison("123 Rue de Paris, 75001 Paris");
            alice.setNumeroTelephone("0601020304");
            alice.setEstActif(true);
            acheteurRepository.save(alice);
        }

        if (utilisateurRepository.findByEmail("charlie@hotmail.com").isEmpty()) {
            Acheteur charlie = new Acheteur();
            charlie.setNomUtilisateur("client_charlie");
            charlie.setEmail("charlie@hotmail.com");
            charlie.setMotDePasse(passwordEncoder.encode("password"));
            charlie.setRole(Utilisateur.Role.ACHETEUR);
            charlie.setAdresseLivraison("789 Boulevard de Nice, 06000 Nice");
            charlie.setNumeroTelephone("0699887766");
            charlie.setEstActif(true);
            acheteurRepository.save(charlie);
        }

        log.info("=".repeat(50));
        log.info("LISTE DES UTILISATEURS PRÊTS (Mot de passe: password)");
        utilisateurRepository.findAll()
                .forEach(u -> log.info("- {} ({})", u.getEmail(), u.getRole()));
        log.info("=".repeat(50));

        com.example.amazonbestseller.entity.Magasin magasin1 = magasinRepository.findByIdVendeur(vendeur1.getId())
                .stream().findFirst().orElseGet(() -> {
                    com.example.amazonbestseller.entity.Magasin m = new com.example.amazonbestseller.entity.Magasin();
                    m.setNom("Boutique High-Tech");
                    m.setIdVendeur(vendeur1.getId());
                    m.setAdresse("123 Rue de la Tech, Paris");
                    return magasinRepository.save(m);
                });

        // Assurer le lien bidirectionnel si possible
        if (vendeur1.getMagasin() == null) {
            vendeur1.setMagasin(magasin1);
            vendeurRepository.save(vendeur1);
        }

        // Produits Vendeur 1 (Electronics & Computers)
        long totalProduitsCsv = produitRepository.count();
        if (produitRepository.findByMagasinIdVendeur(vendeur1.getId()).isEmpty()
                && totalProduitsCsv < 20) {
            log.info("Création du catalogue de secours Vendeur 1...");
            String[] catV1 = { "Électronique", "Informatique", "Jeux Vidéo" };
            Random rand = new Random();

            for (int i = 1; i <= 50; i++) {
                Produit p = new Produit();
                String cat = catV1[rand.nextInt(catV1.length)];
                p.setNom("Tech " + cat + " " + i);
                p.setDescription("Le meilleur de la " + cat + ". Version pro " + i);
                p.setCategorie(cat);
                p.setPrix(BigDecimal.valueOf(50 + rand.nextInt(1000)));
                p.setNote(
                        BigDecimal.valueOf(3.5 + rand.nextDouble() * 1.5).setScale(1, RoundingMode.HALF_UP));
                p.setNombreAvis(rand.nextInt(500));
                p.setRang(i);
                p.setAsin("TECH" + (1000 + i));
                p.setEstDisponible(true);
                p.setDateAjout(LocalDateTime.now().minusDays(rand.nextInt(30)));
                p.setMagasin(magasin1);
                p.setUrlImage("https://placehold.co/150?text=Tech+" + i);
                p.setUrlProduit("https://amazon.com/dp/" + p.getAsin());
                Produit savedProduit = produitRepository.save(p);

                // Initialisation du stock
                creerStockParDefaut(savedProduit);
            }
        }

        // ==================== VENDEUR 2 (Mode & Style) ====================
        Vendeur vendeur2 = vendeurRepository.findByEmail("mode@store.com").orElseGet(() -> {
            Vendeur v = new Vendeur();
            v.setNomUtilisateur("Vendeur Mode");
            v.setEmail("mode@store.com");
            v.setMotDePasse(passwordEncoder.encode("password"));
            v.setRole(Utilisateur.Role.VENDEUR);
            return vendeurRepository.save(v);
        });

        com.example.amazonbestseller.entity.Magasin magasin2 = magasinRepository.findByIdVendeur(vendeur2.getId())
                .stream().findFirst().orElseGet(() -> {
                    com.example.amazonbestseller.entity.Magasin m = new com.example.amazonbestseller.entity.Magasin();
                    m.setNom("Mode & Style");
                    m.setIdVendeur(vendeur2.getId());
                    m.setAdresse("45 Rue de la Mode, Lyon");
                    return magasinRepository.save(m);
                });

        if (vendeur2.getMagasin() == null) {
            vendeur2.setMagasin(magasin2);
            vendeurRepository.save(vendeur2);
        }

        // Produits Vendeur 2 (Sports & Fashion)
        if (produitRepository.findByMagasinIdVendeur(vendeur2.getId()).isEmpty()
                && totalProduitsCsv < 25) {
            log.info("Création du catalogue de secours Vendeur 2...");
            String[] catV2 = { "Sports", "Mode", "Loisirs" };
            Random rand = new Random();

            for (int i = 1; i <= 50; i++) {
                Produit p = new Produit();
                String cat = catV2[rand.nextInt(catV2.length)];
                p.setNom("Article " + cat + " " + i);
                p.setDescription("Accessoire indispensable pour " + cat + ". Modèle " + i);
                p.setCategorie(cat);
                p.setPrix(BigDecimal.valueOf(10 + rand.nextInt(200)));
                p.setNote(BigDecimal.valueOf(3 + rand.nextDouble() * 2).setScale(1, RoundingMode.HALF_UP));
                p.setNombreAvis(rand.nextInt(300));
                p.setRang(i + 100);
                p.setAsin("SPORT" + (2000 + i));
                p.setEstDisponible(true);
                p.setDateAjout(LocalDateTime.now().minusDays(rand.nextInt(30)));
                p.setMagasin(magasin2);
                p.setUrlImage("https://placehold.co/150?text=Sport+" + i);
                p.setUrlProduit("https://amazon.com/dp/" + p.getAsin());
                Produit savedProduit = produitRepository.save(p);

                // Initialisation du stock
                creerStockParDefaut(savedProduit);
            }
        }

        // ==================== VENDEUR 3 (Maison & Cuisine) ====================
        Vendeur vendeur3 = vendeurRepository.findByEmail("vendeur3@test.com").orElseGet(() -> {
            Vendeur v = new Vendeur();
            v.setNomUtilisateur("Vendeur Maison");
            v.setEmail("vendeur3@test.com");
            v.setMotDePasse(passwordEncoder.encode("password"));
            v.setRole(Utilisateur.Role.VENDEUR);
            return vendeurRepository.save(v);
        });

        com.example.amazonbestseller.entity.Magasin magasin3 = magasinRepository.findByIdVendeur(vendeur3.getId())
                .stream().findFirst().orElseGet(() -> {
                    com.example.amazonbestseller.entity.Magasin m = new com.example.amazonbestseller.entity.Magasin();
                    m.setNom("Maison & Confort");
                    m.setIdVendeur(vendeur3.getId());
                    m.setAdresse("88 Boulevard de la Maison, Lyon");
                    return magasinRepository.save(m);
                });

        if (vendeur3.getMagasin() == null) {
            vendeur3.setMagasin(magasin3);
            vendeurRepository.save(vendeur3);
        }

        // Produits Vendeur 3 (Home & Kitchen)
        if (produitRepository.findByMagasinIdVendeur(vendeur3.getId()).isEmpty()
                && totalProduitsCsv < 30) {
            log.info("Création du catalogue de secours Vendeur 3...");
            String[] catV3 = { "Cuisine", "Décoration", "Meubles" };
            Random rand = new Random();

            for (int i = 1; i <= 50; i++) {
                Produit p = new Produit();
                String cat = catV3[rand.nextInt(catV3.length)];
                p.setNom("Produit " + cat + " " + i);
                p.setDescription("Tout pour la " + cat + ". Élégance brute " + i);
                p.setCategorie(cat);
                p.setPrix(BigDecimal.valueOf(20 + rand.nextInt(500)));
                p.setNote(
                        BigDecimal.valueOf(3.8 + rand.nextDouble() * 1.2).setScale(1, RoundingMode.HALF_UP));
                p.setNombreAvis(rand.nextInt(400));
                p.setRang(i + 200);
                p.setAsin("HOME" + (3000 + i));
                p.setEstDisponible(true);
                p.setDateAjout(LocalDateTime.now().minusDays(rand.nextInt(30)));
                p.setMagasin(magasin3);
                p.setUrlImage("https://placehold.co/150?text=Home+" + i);
                p.setUrlProduit("https://amazon.com/dp/" + p.getAsin());
                Produit savedProduit = produitRepository.save(p);

                // Initialisation du stock
                creerStockParDefaut(savedProduit);
            }
        }

        // ==================== ASSIGNATION PRODUITS ORPHELINS ====================
        assignerProduitsOrphelins(magasin1, magasin2, magasin3);

        // ==================== REPARATION STOCK MANQUANT ====================
        reparerStocksManquants();

        // ==================== GENERATION VENTES HISTORIQUES ====================
        try {
            genererVentesHistoriques(magasin1, magasin2, magasin3);
        } catch (Exception e) {
            log.error("Erreur lors de la génération des ventes historiques", e);
        }

        // ==================== GENERATION AVIS HISTORIQUES ====================
        try {
            genererAvisHistoriques();
        } catch (Exception e) {
            log.error("Erreur lors de la génération des avis historiques", e);
        }

        // ==================== GENERATION DONNEES ACHETEURS (Alice & Charlie)
        // ====================
        try {
            Acheteur alice = acheteurRepository.findByEmail("alice@gmail.com").orElse(null);
            if (alice != null) {
                genererDonneesPourAcheteur(alice, magasin1, magasin2, magasin3, "Alice");
            } else {
                log.warn("Alice introuvable pour la génération de données !");
            }
        } catch (Exception e) {
            log.error("Erreur critique lors de la génération des données pour Alice", e);
        }

        try {
            Acheteur charlie = acheteurRepository.findByEmail("charlie@hotmail.com").orElse(null);
            if (charlie != null) {
                genererDonneesPourAcheteur(charlie, magasin1, magasin2, magasin3, "Charlie");
            } else {
                log.warn("Charlie introuvable pour la génération de données !");
            }
        } catch (Exception e) {
            log.error("Erreur critique lors de la génération des données pour Charlie", e);
        }

        log.info(
                "Initialisation terminée ! (3 Vendeurs + Produits + Ventes + Avis + Données Alice & Charlie vérifiées)");
    }

    private void genererDonneesPourAcheteur(Acheteur acheteur, com.example.amazonbestseller.entity.Magasin m1,
            com.example.amazonbestseller.entity.Magasin m2, com.example.amazonbestseller.entity.Magasin m3,
            String buyerName) {
        log.info(">>> DEBUT Génération de données pour {} (email: {})...", buyerName, acheteur.getEmail());

        // 1. Ensure Username is properly set (no correction needed if already correct)
        log.info("{} username: {}", buyerName, acheteur.getNomUtilisateur());

        // 2. Refresh active commands count
        List<Commande> existingCommandes = commandeRepository.findByAcheteurId(acheteur.getId());
        int currentOrderCount = existingCommandes.size();
        log.info("Nombre de commandes actuelles pour {} : {}", buyerName, currentOrderCount);

        if (currentOrderCount >= 20) {
            log.info("{} a déjà suffisamment de données ({} commandes). Fin.", buyerName, currentOrderCount);
            return;
        }

        // 3. Robust Product Fetching
        List<Produit> allProducts = produitRepository.findAll();
        log.info("Nombre total de produits disponibles dans la base : {}", allProducts.size());

        if (allProducts.isEmpty()) {
            log.warn("ATTENTION : Aucun produit trouvé ! Création d'un produit de secours pour {}...", buyerName);
            Produit p = new Produit();
            p.setNom("Produit de Secours " + buyerName);
            p.setDescription("Généré automatiquement car aucun produit trouvé");
            p.setPrix(BigDecimal.valueOf(19.99));
            p.setMagasin(m1); // Fallback to m1
            p.setEstDisponible(true);
            p.setAsin("RESCUE001");
            allProducts.add(produitRepository.save(p));
        }

        // 4. Generate missing orders
        Random rand = new Random();
        int ordersNeeded = 5 - currentOrderCount + rand.nextInt(3); // Aim for 5-7 total
        log.info("Planification de la génération de {} nouvelles commandes...", ordersNeeded);

        for (int i = 0; i < ordersNeeded; i++) {
            try {
                // Pick a random product to start the order
                Produit mainProduct = allProducts.get(rand.nextInt(allProducts.size()));

                // Determine Vendeur (Fallback to m1's vendor if product has no store)
                com.example.amazonbestseller.entity.Magasin productStore = mainProduct.getMagasin();
                Long vendeurId = (productStore != null) ? productStore.getIdVendeur() : m1.getIdVendeur();
                Vendeur vendeur = vendeurRepository.findById(vendeurId).orElse(null);

                Commande cmd = new Commande();
                cmd.setAcheteur(acheteur);
                cmd.setVendeur(vendeur);
                cmd.setDateCommande(LocalDateTime.now().minusDays(1 + rand.nextInt(60)));
                cmd.setStatut(Commande.Statut.LIVREE);
                cmd.setMethodePaiement("Carte Visa");
                cmd.setAdresseLivraison(acheteur.getAdresseLivraison());

                // Save Order first
                Commande savedCmd = commandeRepository.save(cmd);

                BigDecimal totalOrderRef = BigDecimal.ZERO;

                // Add 1-3 items
                int nbItems = 1 + rand.nextInt(3);
                for (int j = 0; j < nbItems; j++) {
                    Produit itemProduct = allProducts.get(rand.nextInt(allProducts.size()));

                    LigneCommande lc = new LigneCommande();
                    lc.setCommande(savedCmd);
                    lc.setProduit(itemProduct);
                    lc.setQuantite(1 + rand.nextInt(2));
                    lc.setPrixUnitaire(itemProduct.getPrix() != null ? itemProduct.getPrix() : BigDecimal.TEN);
                    lc.calculerSousTotal();
                    ligneCommandeRepository.save(lc);

                    totalOrderRef = totalOrderRef.add(lc.getSousTotal());

                    // Generate 'Vente' record for stats
                    Vente v = new Vente();
                    v.setCommande(savedCmd);
                    v.setProduit(itemProduct);
                    // Use the product's actual vendor for the sale record if possible, else order's
                    // vendor
                    Long itemVendeurId = (itemProduct.getMagasin() != null) ? itemProduct.getMagasin().getIdVendeur()
                            : vendeurId;
                    v.setVendeur(vendeurRepository.findById(itemVendeurId).orElse(vendeur));

                    v.setAcheteur(acheteur);
                    v.setQuantite(lc.getQuantite());
                    v.setPrixUnitaire(lc.getPrixUnitaire());
                    v.setMontantTotal(lc.getSousTotal());
                    v.setCommission(lc.getSousTotal().multiply(BigDecimal.valueOf(0.05)));
                    v.setMontantNet(v.getMontantTotal().subtract(v.getCommission()));
                    v.setDateVente(savedCmd.getDateCommande());
                    v.setStatut(Vente.Statut.VALIDEE);
                    venteRepository.save(v);
                }

                // Update Order Totals
                savedCmd.setSousTotal(totalOrderRef);
                savedCmd.setTaxe(totalOrderRef.multiply(BigDecimal.valueOf(0.20)));
                savedCmd.setFraisLivraison(BigDecimal.ZERO);
                savedCmd.setMontantTotal(savedCmd.getSousTotal().add(savedCmd.getTaxe()));
                commandeRepository.save(savedCmd);

                log.info("  -> Commande créée pour {} : ID={}, Montant={}", buyerName, savedCmd.getId(),
                        savedCmd.getMontantTotal());

            } catch (Exception e) {
                log.error("Erreur lors de la création d'une commande pour {}", buyerName, e);
            }
        }

        // 5. Generate Reviews if needed
        long avisCount = avisRepository.findByAcheteurId(acheteur.getId()).size();
        if (avisCount < 3 && !allProducts.isEmpty()) {
            log.info("Génération d'avis pour {}...", buyerName);
            for (int k = 0; k < 3; k++) {
                Produit p = allProducts.get(rand.nextInt(allProducts.size()));
                if (avisRepository.findByProduitIdAndAcheteurId(p.getId(), acheteur.getId()).isEmpty()) {
                    Avis avis = new Avis();
                    avis.setAcheteur(acheteur);
                    avis.setProduit(p);
                    avis.setNote(BigDecimal.valueOf(4 + rand.nextInt(2)));
                    avis.setCommentaire("Top achat !");
                    avis.setDateAvis(LocalDateTime.now().minusDays(rand.nextInt(30)));
                    avis.setEstVerifie(true);
                    avisRepository.save(avis);
                }
            }
        }

        log.info(">>> FIN Génération données {}.", buyerName);
    }

    private void creerStockParDefaut(Produit produit) {
        Stock stock = new Stock();
        stock.setProduit(produit);
        stock.setQuantite(new Random().nextInt(501)); // Variable aléatoire entre 0 et 500
        stock.setSeuilMin(10);
        stock.setEmplacement("Entrepôt A");
        stock.setDerniereMaj(LocalDateTime.now());
        stockRepository.save(stock);
    }

    private void reparerStocksManquants() {
        log.info("Vérification des stocks manquants...");
        long count = 0;
        List<Produit> tousLesProduits = produitRepository.findAll();
        for (Produit p : tousLesProduits) {
            if (stockRepository.findByProduitId(p.getId()).isEmpty()) {
                creerStockParDefaut(p);
                count++;
            }
        }
        if (count > 0) {
            log.info("Réparation effectuée : {} produits ont reçu un stock par défaut.", count);
        }
    }

    private void assignerProduitsOrphelins(com.example.amazonbestseller.entity.Magasin magasin1,
            com.example.amazonbestseller.entity.Magasin magasin2,
            com.example.amazonbestseller.entity.Magasin magasin3) {
        log.info("Assignation des produits orphelins...");

        // Récupérer les produits sans magasin OU ceux du magasin Global Store
        List<Produit> orphelins = produitRepository.findAll().stream()
                .filter(p -> p.getMagasin() == null
                        || "Amazon Global Store".equals(p.getMagasin().getNom()))
                .limit(3000) // Augmenter la limite pour traiter plus de produits CSV
                .toList();

        if (orphelins.isEmpty()) {
            log.info("Aucun produit orphelin trouvé.");
            return;
        }

        log.info("Traitement de {} produits orphelins...", orphelins.size());

        int countV1 = 0, countV2 = 0, countV3 = 0, countAutre = 0;
        List<Produit> toSave = new java.util.ArrayList<>();

        for (Produit p : orphelins) {
            String cat = (p.getCategorie() != null ? p.getCategorie().toLowerCase() : "");
            String nom = (p.getNom() != null ? p.getNom().toLowerCase() : "");

            if (cat.contains("electron") || cat.contains("tech") || cat.contains("computer") ||
                    cat.contains("phone") || cat.contains("laptop") || cat.contains("gaming") ||
                    nom.contains("phone") || nom.contains("laptop") || nom.contains("computer")) {
                p.setMagasin(magasin1);
                countV1++;
            } else if (cat.contains("sport") || cat.contains("fashion") || cat.contains("cloth") ||
                    cat.contains("wear") || cat.contains("shoe") || cat.contains("mode") ||
                    nom.contains("sport") || nom.contains("shoe") || nom.contains("cloth")) {
                p.setMagasin(magasin2);
                countV2++;
            } else if (cat.contains("home") || cat.contains("kitchen") || cat.contains("garden") ||
                    cat.contains("decor") || cat.contains("house") || cat.contains("cuisine") ||
                    nom.contains("home") || nom.contains("kitchen") || nom.contains("garden")) {
                p.setMagasin(magasin3);
                countV3++;
            } else {
                int idx = countAutre % 3;
                p.setMagasin(idx == 0 ? magasin1 : (idx == 1 ? magasin2 : magasin3));
                countAutre++;
            }
            toSave.add(p);

            // Sauvegarde par lots de 500
            if (toSave.size() >= 500) {
                produitRepository.saveAllAndFlush(toSave);
                toSave.clear();
            }
        }

        if (!toSave.isEmpty()) {
            produitRepository.saveAllAndFlush(toSave);
        }

        log.info("Assignation par lot terminée : V1={}, V2={}, V3={}, Autres={}", countV1, countV2, countV3,
                countAutre);
    }

    private void genererVentesHistoriques(com.example.amazonbestseller.entity.Magasin magasin1,
            com.example.amazonbestseller.entity.Magasin magasin2,
            com.example.amazonbestseller.entity.Magasin magasin3) {
        log.info("Génération des ventes historiques...");

        // Récupérer les acheteurs existants
        List<Acheteur> acheteurs = acheteurRepository.findAll();
        if (acheteurs.isEmpty()) {
            // Créer un acheteur de test si nécessaire
            Acheteur a = new Acheteur();
            a.setNomUtilisateur("Acheteur Test");
            a.setEmail("acheteur@test.com");
            a.setMotDePasse(passwordEncoder.encode("password"));
            a.setRole(Utilisateur.Role.ACHETEUR);
            a.setAdresseLivraison("123 Rue Test, Paris");
            a.setNumeroTelephone("0123456789");
            acheteurs = List.of(acheteurRepository.save(a));
        }

        Random rand = new Random();

        // Générer 50 ventes sur les 3 derniers mois pour chaque vendeur
        for (com.example.amazonbestseller.entity.Magasin magasin : Arrays.asList(magasin1, magasin2, magasin3)) {
            List<Produit> produitsMagasin = produitRepository.findByMagasinId(magasin.getId());

            if (produitsMagasin.isEmpty())
                continue;

            for (int i = 0; i < 50; i++) {
                Acheteur acheteur = acheteurs.get(rand.nextInt(acheteurs.size()));
                Commande commande = new Commande();
                commande.setAcheteur(acheteur);

                // Lier le vendeur à la commande
                Vendeur vendeur = vendeurRepository.findById(magasin.getIdVendeur())
                        .orElse(null);
                commande.setVendeur(vendeur);

                commande.setDateCommande(LocalDateTime.now().minusDays(rand.nextInt(90)));
                commande.setStatut(Commande.Statut.values()[rand.nextInt(Commande.Statut.values().length)]);
                commande.setMethodePaiement("Carte Bancaire");
                commande.setAdresseLivraison(acheteur.getAdresseLivraison());

                // Sélectionner 1-3 produits aléatoires
                int nbProduits = 1 + rand.nextInt(3);
                BigDecimal total = BigDecimal.ZERO;

                Commande savedCommande = commandeRepository.save(commande);

                for (int j = 0; j < nbProduits && j < produitsMagasin.size(); j++) {
                    Produit produit = produitsMagasin.get(rand.nextInt(produitsMagasin.size()));

                    LigneCommande ligne = new LigneCommande();
                    ligne.setCommande(savedCommande);
                    ligne.setProduit(produit);
                    ligne.setQuantite(1 + rand.nextInt(3));
                    ligne.setPrixUnitaire(produit.getPrix());
                    ligne.calculerSousTotal();

                    ligneCommandeRepository.save(ligne);
                    total = total.add(ligne.getSousTotal());

                    // Sauvegarder également dans la table Vente pour les prédictions
                    Vente vente = new Vente();
                    vente.setCommande(savedCommande);
                    vente.setProduit(produit);
                    vente.setVendeur(vendeur);
                    vente.setAcheteur(acheteur);
                    vente.setQuantite(ligne.getQuantite());
                    vente.setPrixUnitaire(ligne.getPrixUnitaire());
                    vente.setMontantTotal(ligne.getSousTotal());
                    vente.setCommission(ligne.getSousTotal().multiply(BigDecimal.valueOf(0.05)));
                    vente.setMontantNet(vente.getMontantTotal().subtract(vente.getCommission()));
                    vente.setDateVente(savedCommande.getDateCommande());
                    vente.setStatut(Vente.Statut.VALIDEE);
                    vente.setTypePaiement(commande.getMethodePaiement());
                    venteRepository.save(vente);
                }

                savedCommande.setSousTotal(total);
                savedCommande.setTaxe(total.multiply(BigDecimal.valueOf(0.20)));
                savedCommande.setFraisLivraison(BigDecimal.valueOf(5.99));
                savedCommande
                        .setMontantTotal(total.add(savedCommande.getTaxe()).add(savedCommande.getFraisLivraison()));
                commandeRepository.save(savedCommande);
            }
        }

        log.info("Ventes historiques générées avec succès.");
    }

    private void genererAvisHistoriques() {
        log.info("Génération des avis historiques...");
        List<Acheteur> acheteurs = acheteurRepository.findAll();
        List<Produit> produits = produitRepository.findAll();

        if (acheteurs.isEmpty() || produits.isEmpty())
            return;

        Random rand = new Random();
        String[] commentaires = {
                "Excellent produit, je recommande !",
                "Très satisfait de mon achat.",
                "Conforme à la description.",
                "Livraison rapide et soignée.",
                "Un peu déçu par la qualité, mais ça passe.",
                "Super rapport qualité-prix.",
                "Je ne peux plus m'en passer !",
                "Parfait pour mon usage quotidien.",
                "Le design est top.",
                "Fonctionne très bien."
        };

        int avisCrees = 0;
        for (Acheteur acheteur : acheteurs) {
            // Chaque acheteur laisse entre 3 et 8 avis
            int nbAvis = 3 + rand.nextInt(6);
            for (int i = 0; i < nbAvis; i++) {
                Produit p = produits.get(rand.nextInt(produits.size()));

                // Vérifier si l'acheteur a déjà laissé un avis sur ce produit
                if (avisRepository.findByProduitIdAndAcheteurId(p.getId(), acheteur.getId()).isPresent()) {
                    continue;
                }

                Avis avis = new Avis();
                avis.setAcheteur(acheteur);
                avis.setProduit(p);
                avis.setNote(BigDecimal.valueOf(3.0 + rand.nextDouble() * 2.0).setScale(1, RoundingMode.HALF_UP));
                avis.setCommentaire(commentaires[rand.nextInt(commentaires.length)]);
                avis.setDateAvis(LocalDateTime.now().minusDays(rand.nextInt(30)));
                avis.setEstVerifie(rand.nextBoolean());
                avisRepository.save(avis);
                avisCrees++;
            }
        }
        log.info("{} avis historiques générés avec succès.", avisCrees);
    }

    private void reparerIncoherencesUtilisateurs() {
        log.info(">>> AUTO-REPAIR: Vérification des incohérences utilisateurs...");

        // 1. Réparer les ACHETEURS
        List<Object[]> acheteursOrphelins = entityManager.createNativeQuery(
                "SELECT id, email FROM utilisateur WHERE role = 'ACHETEUR' AND id NOT IN (SELECT id FROM acheteur)")
                .getResultList();

        if (!acheteursOrphelins.isEmpty()) {
            log.warn("Trouvé {} acheteurs orphelins (sans entrée dans la table 'acheteur'). Réparation...",
                    acheteursOrphelins.size());
            for (Object[] row : acheteursOrphelins) {
                Long id = ((Number) row[0]).longValue();
                String email = (String) row[1];
                log.info("  -> RÉPARATION CRITIQUE: Création de l'entrée Acheteur pour Utilisateur ID={} ({})", id,
                        email);

                // Insertion forcée SQL Natif
                entityManager.createNativeQuery(
                        "INSERT INTO acheteur (id, adresse_livraison, numero_telephone) VALUES (?, ?, ?)")
                        .setParameter(1, id)
                        .setParameter(2, "Adresse Inconnue (Récupérée)")
                        .setParameter(3, "0000000000")
                        .executeUpdate();
            }
        }

        // 2. Réparer les VENDEURS
        List<Object[]> vendeursOrphelins = entityManager.createNativeQuery(
                "SELECT id, email FROM utilisateur WHERE role = 'VENDEUR' AND id NOT IN (SELECT id FROM vendeur)")
                .getResultList();

        if (!vendeursOrphelins.isEmpty()) {
            log.warn("Trouvé {} vendeurs orphelins. Réparation...", vendeursOrphelins.size());
            for (Object[] row : vendeursOrphelins) {
                Long id = ((Number) row[0]).longValue();
                log.info("  -> Réparation Vendeur ID={}", id);

                entityManager.createNativeQuery(
                        "INSERT INTO vendeur (id, commission, objectif_ventes, ventes_totales) VALUES (?, 5.0, 1000.0, 0.0)")
                        .setParameter(1, id)
                        .executeUpdate();
            }
        }

        // 3. Réparer les ADMINS
        List<Object[]> adminsOrphelins = entityManager.createNativeQuery(
                "SELECT id, email FROM utilisateur WHERE role = 'ADMIN' AND id NOT IN (SELECT id FROM admin)")
                .getResultList();

        if (!adminsOrphelins.isEmpty()) {
            log.warn("Trouvé {} admins orphelins. Réparation...", adminsOrphelins.size());
            for (Object[] row : adminsOrphelins) {
                Long id = ((Number) row[0]).longValue();
                entityManager.createNativeQuery(
                        "INSERT INTO admin (id, permissions) VALUES (?, 'ALL')")
                        .setParameter(1, id)
                        .executeUpdate();
            }
        }

        // 4. Réparer les INVESTISSEURS
        List<Object[]> investsOrphelins = entityManager.createNativeQuery(
                "SELECT id, email FROM utilisateur WHERE role = 'INVESTISSEUR' AND id NOT IN (SELECT id FROM investisseur)")
                .getResultList();

        if (!investsOrphelins.isEmpty()) {
            log.warn("Trouvé {} investisseurs orphelins. Réparation...", investsOrphelins.size());
            for (Object[] row : investsOrphelins) {
                Long id = ((Number) row[0]).longValue();
                entityManager.createNativeQuery(
                        "INSERT INTO investisseur (id, montant_investissement, roi) VALUES (?, 0.0, 0.0)")
                        .setParameter(1, id)
                        .executeUpdate();
            }
        }

        log.info(">>> AUTO-REPAIR: Terminé.");
    }
}
