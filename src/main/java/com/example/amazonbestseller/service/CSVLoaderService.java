package com.example.amazonbestseller.service;

import com.example.amazonbestseller.entity.Produit;
import com.example.amazonbestseller.entity.Stock;
import com.example.amazonbestseller.repository.ProduitRepository;
import com.example.amazonbestseller.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class CSVLoaderService {

    private final ProduitRepository produitRepository;
    private final StockRepository stockRepository;
    private final com.example.amazonbestseller.repository.MagasinRepository magasinRepository;
    private final com.example.amazonbestseller.repository.VendeurRepository vendeurRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    private final org.springframework.core.io.ResourceLoader resourceLoader;
    private CSVLoaderService self;

    @org.springframework.beans.factory.annotation.Autowired
    public void setSelf(@org.springframework.context.annotation.Lazy CSVLoaderService self) {
        this.self = self;
    }

    @Value("${csv.file.path}")
    private String csvFilePath;

    @Value("${csv.encoding:UTF-8}")
    private String encoding;

    @Value("${csv.separator:,}")
    private String separator;

    @Value("${app.clean-before-load:false}")
    private boolean cleanBeforeLoad;

    /**
     * Point d'entrée principal - Charge les données du CSV
     */
    public void chargerDonneesCSV() {
        log.info("=".repeat(80));
        log.info("🚀 DÉBUT DU CHARGEMENT DES DONNÉES CSV");
        log.info("=".repeat(80));
        log.info("📁 Fichier: {}", csvFilePath);

        try {
            // Nettoyer la base si configuré
            if (cleanBeforeLoad) {
                log.info("🧹 Nettoyage de la base de données activé...");
                nettoyerBaseDeDonnees();
            } else {
                // Vérifier si déjà chargé mais on continue pour la mise à jour (Enrichissement)
                long count = produitRepository.count();
                if (count > 0) {
                    log.info("ℹ️  Base contient déjà {} produits. Mode mise à jour/enrichissement activé.", count);
                }
            }

            // Lire et nettoyer
            List<Produit> produits = lireEtNettoyerCSV();

            // Créer un magasin par défaut pour les produits CSV
            if (!produits.isEmpty()) {
                try {
                    com.example.amazonbestseller.entity.Magasin defaultStore = creerMagasinParDefaut();
                    produits.forEach(p -> p.setMagasin(defaultStore));
                    log.info("✅ Magasin par défaut assigné aux produits CSV: {}", defaultStore.getNom());
                } catch (Exception e) {
                    log.error("⚠️ Impossible de créer le magasin par défaut, les produits seront orphelins : {}",
                            e.getMessage());
                }
            }

            // Sauvegarder
            if (!produits.isEmpty()) {
                sauvegarderProduits(produits);
            } else {
                log.warn("⚠️ Aucun produit chargé depuis le CSV, mais on continue avec le script SQL.");
            }

            // Exécuter le script SQL additionnel (Important pour Alice et ses commandes)
            executerScriptSql();

            log.info("=".repeat(80));
            log.info("✅ CHARGEMENT TERMINÉ AVEC SUCCÈS!");
            log.info("=".repeat(80));

        } catch (Exception e) {
            log.error("❌ ERREUR FATALE lors du chargement", e);
            throw new RuntimeException("Impossible de charger le CSV", e);
        }
    }

    /**
     * Nettoie toutes les données de la base de données
     */
    private void nettoyerBaseDeDonnees() {
        log.info("🗑️  Suppression de toutes les données existantes...");

        try {
            // Désactiver temporairement les contraintes de clés étrangères
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Supprimer dans l'ordre inverse des dépendances
            log.debug("   Suppression des statistiques...");
            jdbcTemplate.execute("DELETE FROM statistique");

            log.debug("   Suppression des alertes...");
            jdbcTemplate.execute("DELETE FROM alerte");

            log.debug("   Suppression des avis...");
            jdbcTemplate.execute("DELETE FROM avis");

            log.debug("   Suppression des ventes...");
            jdbcTemplate.execute("DELETE FROM vente");

            log.debug("   Suppression des lignes de commande...");
            jdbcTemplate.execute("DELETE FROM ligne_commande");

            log.debug("   Suppression des commandes...");
            jdbcTemplate.execute("DELETE FROM commande");

            log.debug("   Suppression des stocks...");
            jdbcTemplate.execute("DELETE FROM stock");

            log.debug("   Suppression des produits...");
            jdbcTemplate.execute("DELETE FROM produit");

            log.debug("   Suppression des magasins...");
            jdbcTemplate.execute("DELETE FROM magasin");

            log.debug("   Suppression des vendeurs...");
            jdbcTemplate.execute("DELETE FROM vendeur");

            log.debug("   Suppression des acheteurs...");
            jdbcTemplate.execute("DELETE FROM acheteur");

            log.debug("   Suppression des investisseurs...");
            jdbcTemplate.execute("DELETE FROM investisseur");

            log.debug("   Suppression des admins...");
            jdbcTemplate.execute("DELETE FROM admin");

            log.debug("   Suppression des utilisateurs...");
            jdbcTemplate.execute("DELETE FROM utilisateur");

            // Réinitialiser les AUTO_INCREMENT
            log.debug("   Réinitialisation des compteurs AUTO_INCREMENT...");
            jdbcTemplate.execute("ALTER TABLE utilisateur AUTO_INCREMENT = 1");
            jdbcTemplate.execute("ALTER TABLE produit AUTO_INCREMENT = 1");
            jdbcTemplate.execute("ALTER TABLE magasin AUTO_INCREMENT = 1");
            jdbcTemplate.execute("ALTER TABLE stock AUTO_INCREMENT = 1");
            jdbcTemplate.execute("ALTER TABLE commande AUTO_INCREMENT = 1");
            jdbcTemplate.execute("ALTER TABLE ligne_commande AUTO_INCREMENT = 1");
            jdbcTemplate.execute("ALTER TABLE vente AUTO_INCREMENT = 1");
            jdbcTemplate.execute("ALTER TABLE avis AUTO_INCREMENT = 1");
            jdbcTemplate.execute("ALTER TABLE alerte AUTO_INCREMENT = 1");
            jdbcTemplate.execute("ALTER TABLE statistique AUTO_INCREMENT = 1");

            // Réactiver les contraintes de clés étrangères
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

            log.info("✅ Base de données nettoyée avec succès!");

        } catch (Exception e) {
            log.error("❌ Erreur lors du nettoyage de la base de données", e);
            // Réactiver les contraintes même en cas d'erreur
            try {
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            } catch (Exception ex) {
                log.error("Impossible de réactiver les contraintes FK", ex);
            }
            throw new RuntimeException("Échec du nettoyage de la base de données", e);
        }
    }

    /**
     * Lit et nettoie le fichier CSV
     */
    private List<Produit> lireEtNettoyerCSV() throws Exception {
        List<Produit> produits = new ArrayList<>();
        int lineNumber = 0;
        int skipped = 0;
        int valid = 0;

        log.info("📖 Lecture du fichier CSV...");

        // Priorité au fichier système pour le développement (évite le cache du
        // classpath)
        org.springframework.core.io.Resource resource = resourceLoader.getResource("file:./" + csvFilePath);
        if (!resource.exists()) {
            resource = resourceLoader.getResource("file:" + csvFilePath);
        }
        if (!resource.exists()) {
            resource = resourceLoader.getResource("classpath:" + csvFilePath);
        }

        if (!resource.exists()) {
            log.error("❌ Fichier CSV introuvable : {}", csvFilePath);
            return produits;
        }

        log.info("   -> Source utilisée : {}", resource.getURI());

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Ignorer l'en-tête
                if (isFirstLine) {
                    isFirstLine = false;
                    log.info("📋 En-tête: {}", line);
                    continue;
                }

                try {
                    Produit produit = parseLigne(line, lineNumber);
                    if (produit != null) {
                        if (valid <= 5) {
                            log.info("DEBUG CSV - ASIN: {}, Nom: {}, Image: {}", produit.getAsin(), produit.getNom(),
                                    produit.getUrlImage());
                        }
                        produits.add(produit);
                        valid++;

                        if (valid % 50 == 0) {
                            log.debug("✓ {} produits valides lus...", valid);
                        }
                    } else {
                        skipped++;
                    }
                } catch (Exception e) {
                    skipped++;
                    log.warn("⚠️  Ligne {} ignorée: {}", lineNumber, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors de la lecture du CSV: {}", e.getMessage());
            throw e;
        }

        log.info("📊 Résultat du parsing:");
        log.info("   • Lignes totales: {}", lineNumber - 1);
        log.info("   • Produits valides: {}", valid);
        log.info("   • Lignes ignorées: {}", skipped);

        return produits;
    }

    /**
     * Parse une ligne du CSV et crée un objet Produit
     */
    private Produit parseLigne(String line, int lineNumber) {
        try {
            // Séparer les colonnes en tenant compte des virgules dans les guillemets
            List<String> colonnes = parseCSVLine(line);

            if (colonnes.size() < 8) {
                log.warn("Ligne {} : nombre de colonnes insuffisant ({})", lineNumber, colonnes.size());
                return null;
            }

            // Extraire et nettoyer les données (colonnes 0-7 sont obligatoires)
            String asin = nettoyerTexte(colonnes.get(0));
            String categorie = nettoyerTexte(colonnes.get(1));
            String urlProduit = nettoyerTexte(colonnes.get(2));
            String nombreVendeursStr = nettoyerTexte(colonnes.get(3));
            String rangStr = nettoyerTexte(colonnes.get(4));
            String noteStr = nettoyerTexte(colonnes.get(5));
            String nombreAvisStr = nettoyerTexte(colonnes.get(6));
            String prixStr = nettoyerTexte(colonnes.get(7));

            // Colonnes optionnelles (8 et 9) pour CSV enrichi
            String nomProduitCSV = colonnes.size() > 8 ? nettoyerTexte(colonnes.get(8)) : null;
            String urlImageCSV = colonnes.size() > 9 ? nettoyerTexte(colonnes.get(9)) : null;

            // Validation ASIN
            if (asin == null || asin.isEmpty() || asin.equalsIgnoreCase("ASIN")) {
                return null;
            }

            // Utiliser le nom du CSV si disponible, sinon générer
            String nomProduit = (nomProduitCSV != null && !nomProduitCSV.isEmpty())
                    ? nomProduitCSV
                    : genererNomProduit(categorie, rangStr, asin);

            // Utiliser l'image du CSV si disponible, sinon générer
            String urlImageProduit = (urlImageCSV != null && !urlImageCSV.isEmpty())
                    ? urlImageCSV
                    : genererUrlImage(asin);

            if (nomProduitCSV != null && !nomProduitCSV.isEmpty()) {
                // Too noisy to log every enriched product, relying on first 5 debug log in
                // caller
            }

            return Produit.builder()
                    .asin(asin)
                    .nom(nomProduit)
                    .description("Description du produit Amazon ASIN: " + asin)
                    .categorie(categorie != null && !categorie.isEmpty() ? categorie : "Non Catégorisé")
                    .urlProduit(urlProduit != null && !urlProduit.isEmpty() ? urlProduit
                            : "https://www.amazon.com/dp/" + asin)
                    .urlImage(urlImageProduit)
                    .nombreVendeurs(parseNombreVendeurs(nombreVendeursStr))
                    .rang(parseRang(rangStr))
                    .note(parseNote(noteStr))
                    .nombreAvis(parseNombreAvis(nombreAvisStr))
                    .prix(parsePrix(prixStr))
                    .estDisponible(true)
                    .dateAjout(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.debug("Erreur ligne {}: {}", lineNumber, e.getMessage());
            return null;
        }
    }

    /**
     * Parse une ligne CSV en tenant compte des guillemets et virgules
     */
    private List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());

        return result;
    }

    /**
     * Nettoie une chaîne de caractères
     */
    private String nettoyerTexte(String texte) {
        if (texte == null)
            return null;
        return texte.trim()
                .replace("\"", "")
                .replace("&amp;", "&")
                .trim();
    }

    /**
     * Parse le nombre de vendeurs (ex: "9 Sellers" -> 9)
     */
    private Integer parseNombreVendeurs(String str) {
        if (str == null || str.isEmpty())
            return 1;
        try {
            String numberPart = str.replaceAll("[^0-9]", "");
            return numberPart.isEmpty() ? 1 : Integer.parseInt(numberPart);
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * Parse le rang (ex: "#25" -> 25)
     */
    private Integer parseRang(String str) {
        if (str == null || str.isEmpty())
            return 0;
        try {
            String numberPart = str.replace("#", "").trim();
            return Integer.parseInt(numberPart);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Parse la note (ex: "4.6" -> 4.6)
     */
    private BigDecimal parseNote(String str) {
        if (str == null || str.isEmpty())
            return BigDecimal.ZERO;
        try {
            String cleaned = str.replace(",", ".").trim();
            BigDecimal note = new BigDecimal(cleaned);
            // Valider la note entre 0 et 5
            if (note.compareTo(BigDecimal.ZERO) < 0)
                return BigDecimal.ZERO;
            if (note.compareTo(new BigDecimal("5")) > 0)
                return new BigDecimal("5");
            return note;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Parse le nombre d'avis (ex: "72,839" -> 72839)
     */
    private Integer parseNombreAvis(String str) {
        if (str == null || str.isEmpty())
            return 0;
        try {
            String numberPart = str.replaceAll("[^0-9]", "");
            return numberPart.isEmpty() ? 0 : Integer.parseInt(numberPart);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Parse le prix (ex: "$7.99" -> 7.99)
     */
    private BigDecimal parsePrix(String str) {
        if (str == null || str.isEmpty())
            return BigDecimal.ZERO;
        try {
            String cleaned = str.replace("$", "")
                    .replace(",", "")
                    .trim();
            return new BigDecimal(cleaned);
        } catch (Exception e) {
            log.warn("Prix invalide: {}", str);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Génère un nom de produit descriptif basé sur la catégorie et le rang
     */
    private String genererNomProduit(String categorie, String rangStr, String asin) {
        String cat = (categorie != null && !categorie.isEmpty()) ? categorie : "Produit";
        String rang = (rangStr != null && !rangStr.isEmpty()) ? rangStr : "";

        // Générer un nom plus convivial et descriptif
        if (!rang.isEmpty()) {
            // Nettoyer le rang pour enlever le # si présent
            String cleanRang = rang.replace("#", "");

            // Créer des noms plus descriptifs selon la catégorie
            if (cat.contains("Electronics")) {
                return String.format("Amazon Electronics Product - Rank #%s", cleanRang);
            } else if (cat.contains("Clothing") || cat.contains("Shoes") || cat.contains("Jewelry")) {
                return String.format("Top Rated Fashion Item - Rank #%s", cleanRang);
            } else if (cat.contains("Books")) {
                return String.format("Bestselling Book - Rank #%s", cleanRang);
            } else if (cat.contains("Video Games")) {
                return String.format("Popular Video Game - Rank #%s", cleanRang);
            } else if (cat.contains("Gift Cards")) {
                return String.format("Gift Card - Rank #%s", cleanRang);
            } else if (cat.contains("Toys")) {
                return String.format("Top Toy & Game - Rank #%s", cleanRang);
            } else if (cat.contains("Camera") || cat.contains("Photo")) {
                return String.format("Camera & Photo Product - Rank #%s", cleanRang);
            } else {
                return String.format("%s - Top Seller #%s", cat, cleanRang);
            }
        } else {
            return String.format("%s Product", cat);
        }
    }

    /**
     * Génère l'URL de l'image Amazon basée sur l'ASIN
     */
    private String genererUrlImage(String asin) {
        // Utiliser les vraies URLs d'images Amazon basées sur l'ASIN
        // Format standard des images Amazon:
        // https://m.media-amazon.com/images/I/{ASIN}._AC_UL320_.jpg
        // Note: Certaines images peuvent ne pas exister, le frontend gérera le fallback

        if (asin != null && !asin.isEmpty()) {
            // Essayer plusieurs formats d'URL Amazon
            // Format 1: URL mobile standard
            return String.format("https://m.media-amazon.com/images/I/%s._AC_UL320_.jpg", asin);
        }

        // Fallback si pas d'ASIN
        return "https://placehold.co/400x400?text=Produit+Amazon";
    }

    private void sauvegarderProduits(List<Produit> produits) {
        log.info("💾 Sauvegarde de {} produits...", produits.size());

        int saved = 0;
        int errors = 0;

        for (Produit produit : produits) {
            try {
                // Utiliser le proxy self pour garantir la transaction par produit
                self.chargerUnProduit(produit);
                saved++;

                if (saved % 100 == 0) {
                    log.info("✅ {} produits sauvegardés...", saved);
                }
            } catch (Exception e) {
                errors++;
                log.error("❌ Erreur sauvegarde produit {}: {}", produit.getAsin(), e.getMessage());
            }
        }

        produitRepository.flush();
        stockRepository.flush();

        log.info("📊 Résultat final:");
        log.info("   • Produits sauvegardés: {}", saved);
        log.info("   • Erreurs: {}", errors);
        if (!produits.isEmpty()) {
            log.info("   • Taux de réussite: {}%", (saved * 100.0) / produits.size());
        }
    }

    /**
     * Une méthode transactionnelle pour sauvegarder un produit et son stock
     */
    @Transactional
    public void chargerUnProduit(Produit produit) {
        // Vérifier si le produit existe déjà par son ASIN
        java.util.Optional<Produit> existingOpt = produitRepository.findByAsin(produit.getAsin());

        Produit produitSauvegarde;

        if (existingOpt.isPresent()) {
            // Mise à jour du produit existant
            Produit existing = existingOpt.get();
            existing.setNom(produit.getNom());
            existing.setDescription(produit.getDescription());
            existing.setCategorie(produit.getCategorie());
            existing.setUrlProduit(produit.getUrlProduit());
            existing.setUrlImage(produit.getUrlImage());
            existing.setNombreVendeurs(produit.getNombreVendeurs());
            existing.setRang(produit.getRang());
            existing.setNote(produit.getNote());
            existing.setNombreAvis(produit.getNombreAvis());
            existing.setPrix(produit.getPrix());
            existing.setEstDisponible(produit.getEstDisponible());

            // Si le produit CSV a un magasin assigné et existant n'en a pas, ou on force
            // l'update
            if (produit.getMagasin() != null) {
                existing.setMagasin(produit.getMagasin());
            }

            produitSauvegarde = produitRepository.save(existing);

            // Gestion du Stock
            Stock stock = existing.getStock();
            if (stock == null) {
                stock = new Stock();
                stock.setProduit(produitSauvegarde);
            }
            // Mettre à jour le stock avec une valeur aléatoire si c'est une nouvelle
            // création ou réinitialisation
            if (stock.getId() == null) { // Uniquement pour nouveau stock pour éviter écrasement
                stock.setQuantite(new java.util.Random().nextInt(501)); // 0 à 500
            }
            stock.setSeuilMin(10);
            stock.setSeuilMax(1000);
            stock.setDerniereMaj(LocalDateTime.now());
            stockRepository.save(stock);

        } else {
            // Création nouveau produit
            produitSauvegarde = produitRepository.save(produit);

            Stock stock = new Stock();
            stock.setProduit(produitSauvegarde);
            stock.setQuantite(new java.util.Random().nextInt(501)); // 0 à 500
            stock.setSeuilMin(10);
            stock.setSeuilMax(1000);
            stock.setDerniereMaj(LocalDateTime.now());
            stockRepository.save(stock);
        }
    }

    /**
     * Exécute le script SQL d'insertion de données complémentaires (Utilisateurs,
     * Ventes, etc.)
     */
    private void executerScriptSql() {
        log.info("📜 Exécution du script SQL de données de test (insert_data.sql)...");
        try {
            org.springframework.core.io.Resource resource = resourceLoader.getResource("file:insert_data.sql");
            if (!resource.exists()) {
                resource = resourceLoader.getResource("file:./insert_data.sql");
            }

            if (!resource.exists()) {
                log.warn("⚠️  Fichier insert_data.sql introuvable. Ignoré.");
                return;
            }

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(resource);
            populator.setSqlScriptEncoding("UTF-8");
            populator.setContinueOnError(true);

            DatabasePopulatorUtils.execute(populator, jdbcTemplate.getDataSource());

            log.info("✅ Données SQL complémentaires insérées avec succès !");

        } catch (Exception e) {
            log.error("❌ ERREUR lors de l'exécution du script SQL: {}", e.getMessage(), e);
        }
    }

    // METHODE AJOUTEE: Création du magasin global pour les produits CSV
    private com.example.amazonbestseller.entity.Magasin creerMagasinParDefaut() {
        // Créer d'abord le vendeur par défaut s'il n'existe pas
        com.example.amazonbestseller.entity.Vendeur vendeur = vendeurRepository.findByEmail("admin@amazon.com")
                .map(u -> (com.example.amazonbestseller.entity.Vendeur) u)
                .orElse(null);

        // Si l'admin n'existe pas ou n'est pas vendeur (sécurité), on utilise un
        // vendeur "système"
        if (vendeur == null) {
            // Chercher ou créer un vendeur dédié "Amazon Best Seller"
            vendeur = vendeurRepository.findByEmail("bestseller@amazon.com").orElseGet(() -> {
                com.example.amazonbestseller.entity.Vendeur v = new com.example.amazonbestseller.entity.Vendeur();
                v.setNomUtilisateur("Amazon Best Sellers");
                v.setEmail("bestseller@amazon.com");
                v.setMotDePasse(passwordEncoder.encode("password"));
                v.setRole(com.example.amazonbestseller.entity.Utilisateur.Role.VENDEUR);
                v.setEstActif(true);
                return vendeurRepository.save(v);
            });
        }

        final Long vendeurId = vendeur.getId();

        // Créer le magasin s'il n'existe pas
        return magasinRepository.findByIdVendeur(vendeurId).stream().findFirst().orElseGet(() -> {
            com.example.amazonbestseller.entity.Magasin m = new com.example.amazonbestseller.entity.Magasin();
            m.setNom("Amazon Global Store");
            m.setIdVendeur(vendeurId);
            m.setAdresse("Amazon HQ");
            m.setEmail("store@amazon.com");
            m.setNote(BigDecimal.valueOf(5.0));
            return magasinRepository.save(m);
        });
    }
}
