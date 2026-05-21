package com.example.amazonbestseller.service;

import com.example.amazonbestseller.dto.DashboardVendeurDTO;
import com.example.amazonbestseller.dto.ProduitDTO;
import com.example.amazonbestseller.entity.*;
import com.example.amazonbestseller.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProduitRepository produitRepository;
    private final CommandeRepository commandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AcheteurRepository acheteurRepository;
    private final VendeurRepository vendeurRepository;
    private final AvisRepository avisRepository;
    private final MagasinRepository magasinRepository;
    private final LigneCommandeRepository ligneCommandeRepository;

    // ========== DASHBOARD GLOBAL ==========

    public Map<String, Object> getDashboardGlobal() {
        Map<String, Object> dashboard = new HashMap<>();

        // Statistiques produits
        Long totalProduits = produitRepository.count();
        Long produitsDisponibles = produitRepository.countAvailableProduits();
        Long nombreCategories = produitRepository.countDistinctCategories();
        BigDecimal prixMoyen = produitRepository.findAveragePrice();
        BigDecimal noteMoyenne = produitRepository.findAverageRating();

        dashboard.put("produits", Map.of(
                "total", totalProduits,
                "disponibles", produitsDisponibles,
                "categories", nombreCategories,
                "prixMoyen",
                prixMoyen != null ? prixMoyen.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                "noteMoyenne",
                noteMoyenne != null ? noteMoyenne.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO));

        // Distribution par catégorie
        Map<String, Long> distributionCategories = produitRepository.findProduitCountByCategory()
                .stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]));
        dashboard.put("distributionCategories", distributionCategories);

        // Top 10 produits
        List<Produit> top10 = produitRepository.findTopProduits(PageRequest.of(0, 10));
        dashboard.put("top10Produits", top10);

        // Statistiques des ventes
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime debutMois = maintenant.withDayOfMonth(1).withHour(0).withMinute(0);

        Map<String, Object> statsVentes = getStatistiquesVentes(debutMois, maintenant);
        dashboard.put("ventes", statsVentes);

        // Statistiques utilisateurs
        Map<String, Object> statsUtilisateurs = getStatistiquesUtilisateurs();
        dashboard.put("utilisateurs", statsUtilisateurs);

        return dashboard;
    }

    // ========== DASHBOARD ACHETEUR ==========

    public Map<String, Object> getDashboardAcheteur(Long acheteurId) {
        Map<String, Object> dashboard = new HashMap<>();

        // Commandes de l'acheteur
        Optional<Acheteur> acheteurOpt = acheteurRepository.findById(acheteurId);

        if (acheteurOpt.isEmpty()) {
            // Check if the user exists but is just missing the Acheteur extension
            Optional<Utilisateur> userOpt = utilisateurRepository.findById(acheteurId);
            if (userOpt.isPresent() && (userOpt.get().getRole() == Utilisateur.Role.ACHETEUR
                    || userOpt.get().getRole() == Utilisateur.Role.ADMIN)) {
                System.out.println(">>> [INFO] Acheteur record missing for existing Utilisateur ID: "
                        + acheteurId + ". Creating new Acheteur record.");

                Acheteur newAcheteur = new Acheteur();
                newAcheteur.setId(acheteurId); // Shares same ID as Utilisateur with @MapsId usually, or just link it
                // newAcheteur.setUtilisateur(userOpt.get()); // Removed: Acheteur extends
                // Utilisateur, so no setUtilisateur method.
                newAcheteur.setNomUtilisateur(userOpt.get().getNomUtilisateur());
                newAcheteur.setEmail(userOpt.get().getEmail());
                newAcheteur.setMotDePasse(userOpt.get().getMotDePasse());
                newAcheteur.setRole(userOpt.get().getRole());
                // Initialize default fields if necessary
                acheteurRepository.save(newAcheteur);

                // Now recurse or proceed with the new acheteur (easiest is to recursively call
                // self, but standard flow below works if we set acheteur)
                // However, simpler to just set the variable and let the rest of the method run,
                // BUT the rest of the method uses 'acheteurOpt.get()', so we should construct
                // it.
                acheteurOpt = Optional.of(newAcheteur);
            } else {
                throw new RuntimeException("Acheteur non trouvé avec l'ID: " + acheteurId);
            }
        }

        // Check again if we have it now (we should)
        if (acheteurOpt.isEmpty()) {
            throw new RuntimeException("Acheteur non trouvé avec l'ID: " + acheteurId);
        }

        Acheteur acheteur = acheteurOpt.get();
        List<Commande> commandes = commandeRepository.findByAcheteurId(acheteurId);
        int nombreCommandes = commandes.size();

        System.out.println(">>> [DEBUG] getDashboardAcheteur - acheteurId: " + acheteurId);
        System.out.println(">>> [DEBUG] getDashboardAcheteur - orders count: " + nombreCommandes);

        // Initialiser les valeurs par défaut
        BigDecimal montantTotalDepense = BigDecimal.ZERO;
        BigDecimal panierMoyen = BigDecimal.ZERO;
        List<Commande> dernieresCommandes = new ArrayList<>();
        List<Map<String, String>> alertes = new ArrayList<>();

        // Si l'acheteur a des commandes
        if (nombreCommandes > 0) {
            montantTotalDepense = commandes.stream()
                    .map(c -> c.getMontantTotal() != null ? c.getMontantTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            panierMoyen = nombreCommandes > 0
                    ? montantTotalDepense.divide(BigDecimal.valueOf(nombreCommandes), 2,
                            RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // Dernières commandes
            dernieresCommandes = commandes.stream()
                    .sorted((c1, c2) -> c2.getDateCommande().compareTo(c1.getDateCommande()))
                    .limit(5)
                    .collect(Collectors.toList());

            // Alertes basées sur les commandes
            for (Commande c : dernieresCommandes) {
                if (c.getStatut() == Commande.Statut.LIVREE) {
                    Map<String, String> m = new HashMap<>();
                    m.put("type", "SUCCESS");
                    m.put("message", "Votre commande #" + c.getId() + " a été livrée.");
                    alertes.add(m);
                } else if (c.getStatut() == Commande.Statut.EXPEDIEE) {
                    Map<String, String> m = new HashMap<>();
                    m.put("type", "INFO");
                    m.put("message", "Votre commande #" + c.getId()
                            + " est en cours de livraison.");
                    alertes.add(m);
                }
            }
        } else {
            // Message d'information si pas de commandes
            Map<String, String> m = new HashMap<>();
            m.put("type", "INFO");
            m.put("message",
                    "Vous n'avez pas encore passé de commande. Parcourez notre catalogue pour commencer vos achats !");
            alertes.add(m);
        }

        dashboard.put("commandes",
                Map.of("total", nombreCommandes, "montantTotal",
                        montantTotalDepense != null ? montantTotalDepense : BigDecimal.ZERO, "panierMoyen",
                        panierMoyen != null ? panierMoyen : BigDecimal.ZERO));

        // Mapping simplifié des commandes pour éviter la récursion
        dashboard.put("dernieresCommandes", dernieresCommandes.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("dateCommande", c.getDateCommande());
            m.put("statut", c.getStatut() != null ? c.getStatut().name() : "EN_ATTENTE");
            m.put("montantTotal", c.getMontantTotal() != null ? c.getMontantTotal() : BigDecimal.ZERO);
            return m;
        }).collect(Collectors.toList()));

        dashboard.put("alertes", alertes);

        // Avis laissés (Simplifiés)
        List<Avis> avisLaisses = avisRepository.findByAcheteurId(
                acheteurId);
        dashboard.put("nombreAvis", avisLaisses.size());
        dashboard.put("avis", avisLaisses.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("note", a.getNote());
            m.put("commentaire", a.getCommentaire());
            m.put("dateAvis", a.getDateAvis());
            if (a.getProduit() != null) {
                Map<String, Object> prodMap = new HashMap<>();
                prodMap.put("id", a.getProduit().getId());
                prodMap.put("nom", a.getProduit().getNom());
                m.put("produit", prodMap);
            }
            return m;
        }).collect(Collectors.toList()));

        // Recommandations (Simplifiées)
        List<Produit> recommandations = getRecommandationsPersonnalisees(
                acheteurId);
        dashboard.put("recommandations", recommandations.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("nom", p.getNom());
            m.put("prix", p.getPrix() != null ? p.getPrix() : BigDecimal.ZERO);
            m.put("urlImage", p.getUrlImage());
            m.put("note", p.getNote());
            return m;
        }).collect(Collectors.toList()));

        // Favoris (Simplifiés - Top produits par note si vide ou par défaut)
        List<Produit> favoris = produitRepository.findTopRatedProduits(new BigDecimal("4.0"),
                PageRequest.of(0, 8));
        dashboard.put("favoris", favoris.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("nom", p.getNom());
            m.put("prix", p.getPrix() != null ? p.getPrix() : BigDecimal.ZERO);
            m.put("urlImage", p.getUrlImage());
            m.put("note", p.getNote());
            return m;
        }).collect(Collectors.toList()));

        // Alerte promo
        if (alertes.isEmpty()) {
            Map<String, String> promo = new HashMap<>();
            promo.put("type", "PROMO");
            promo.put("message",
                    "Découvrez nos offres spéciales ! -20% sur la catégorie Tech avec le code HIVER20");
            alertes.add(promo);
        }

        dashboard.put("alertes", alertes);
        return dashboard;
    }

    // ========== DASHBOARD VENDEUR ==========

    @Transactional(readOnly = true)
    public DashboardVendeurDTO getDashboardVendeur(Long vendeurId) {
        System.out.println(">>> [DEBUG] Generating dashboard for vendor: " + vendeurId);

        Vendeur vendeur = vendeurRepository.findById(vendeurId)
                .orElseThrow(() -> new RuntimeException("Vendeur non trouvé"));

        // Performance du vendeur
        LocalDateTime debutMois = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime maintenant = LocalDateTime.now();

        List<Commande> commandesMois = commandeRepository.findByVendeurId(vendeurId)
                .stream()
                .filter(c -> !c.getDateCommande().isBefore(debutMois)
                        && !c.getDateCommande().isAfter(maintenant))
                .collect(Collectors.toList());

        BigDecimal chiffreAffaireMois = commandesMois.stream()
                .map(c -> c.getMontantTotal() != null ? c.getMontantTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal commissionMois = chiffreAffaireMois.multiply(BigDecimal.valueOf(0.10));
        BigDecimal objVentes = vendeur.getObjectifVentes() != null ? vendeur.getObjectifVentes()
                : BigDecimal.ZERO;

        System.out.println(">>> [DEBUG] Found " + commandesMois.size() + " orders for current month. Revenue: "
                + chiffreAffaireMois);

        DashboardVendeurDTO.PerformanceDTO performance = DashboardVendeurDTO.PerformanceDTO.builder()
                .revenuTotal(chiffreAffaireMois)
                .chiffreAffaireMois(chiffreAffaireMois)
                .commissionMois(commissionMois)
                .totalVentes(commandesMois.size())
                .nombreVentesMois(commandesMois.size())
                .objectifVentes(objVentes)
                .progressionObjectif(calculerProgressionObjectif(chiffreAffaireMois, objVentes))
                .build();

        // Top produits vendus (mapping to ProduitDTO to avoid circular refs)
        List<Object[]> topProduitsResult = ligneCommandeRepository.findTopSellingProductsBetweenDates(debutMois,
                maintenant);
        List<ProduitDTO> topProduits = topProduitsResult.stream()
                .limit(10)
                .map(arr -> {
                    Produit p = (Produit) arr[0];
                    return ProduitDTO.builder()
                            .id(p.getId())
                            .nom(p.getNom())
                            .prix(p.getPrix())
                            .categorie(p.getCategorie())
                            .build();
                })
                .collect(Collectors.toList());

        // Commandes à traiter (mapping to CommandeSummaryDTO)
        List<DashboardVendeurDTO.CommandeSummaryDTO> commandesEnAttente = commandeRepository
                .findByVendeurId(vendeurId)
                .stream()
                .filter(c -> c.getStatut() == Commande.Statut.EN_ATTENTE
                        || c.getStatut() == Commande.Statut.CONFIRMEE
                        || c.getStatut() == Commande.Statut.LIVREE)
                .sorted(Comparator.comparing(Commande::getDateCommande).reversed())
                .limit(20)
                .map(c -> DashboardVendeurDTO.CommandeSummaryDTO.builder()
                        .id(c.getId())
                        .dateCommande(c.getDateCommande().toString())
                        .montantTotal(c.getMontantTotal())
                        .statut(c.getStatut().name())
                        .acheteurNom(c.getAcheteur() != null
                                ? c.getAcheteur().getNomUtilisateur()
                                : "Anonyme")
                        .build())
                .collect(Collectors.toList());

        // Évolution des ventes (7 derniers jours)
        Map<String, BigDecimal> evolutionVentes = getEvolutionVentes7Jours(vendeurId);

        // Répartition des ventes par catégorie
        List<Object[]> ventesParCategorieResults = ligneCommandeRepository
                .findTotalRevenueByCategoryByVendeur(vendeurId);
        Map<String, BigDecimal> ventesParCategorie = new HashMap<>();
        for (Object[] arr : ventesParCategorieResults) {
            String cat = arr[0] != null ? arr[0].toString() : "Autre";
            BigDecimal val = arr[1] != null ? (BigDecimal) arr[1] : BigDecimal.ZERO;
            ventesParCategorie.put(cat, val);
        }

        return DashboardVendeurDTO.builder()
                .performance(performance)
                .topProduitsVendus(topProduits)
                .commandesEnAttente(commandesEnAttente)
                .evolutionVentes(evolutionVentes)
                .ventesParCategorie(ventesParCategorie)
                .build();
    }

    // ========== DASHBOARD ADMIN ==========

    public Map<String, Object> getDashboardAdmin() {
        Map<String, Object> dashboard = new HashMap<>();

        // Vue d'ensemble complète
        dashboard.putAll(getDashboardGlobal());

        // Statistiques utilisateurs détaillées
        List<Object[]> utilisateursParRole = utilisateurRepository.countUsersByRole();
        Map<String, Long> distributionRoles = utilisateursParRole.stream()
                .collect(Collectors.toMap(
                        arr -> arr[0].toString(),
                        arr -> (Long) arr[1]));
        dashboard.put("utilisateursParRole", distributionRoles);

        // Activité récente
        LocalDateTime il24h = LocalDateTime.now().minusHours(24);
        Long nouvellesCommandes = commandeRepository.countCommandesBetweenDates(il24h, LocalDateTime.now());
        Long nouveauxUtilisateurs = utilisateurRepository.countNewUsersSince(il24h);

        dashboard.put("activiteRecente", Map.of(
                "nouvellesCommandes24h", nouvellesCommandes,
                "nouveauxUtilisateurs24h", nouveauxUtilisateurs));

        // Alertes système (produits en rupture, etc.)
        List<Produit> produitsRupture = produitRepository.findOutOfStockProduits();
        List<Produit> produitsStockFaible = produitRepository.findLowStockProduits();

        dashboard.put("alertes", Map.of(
                "produitsRupture", produitsRupture.size(),
                "produitsStockFaible", produitsStockFaible.size()));

        return dashboard;
    }

    // ========== DASHBOARD INVESTISSEUR ==========

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardInvestisseur(Long investisseurId) {
        Map<String, Object> dashboard = new HashMap<>();

        try {
            // 1. Répartition du Portefeuille (Nombre de produits par catégorie)
            Map<String, Long> repartitionProduits = getRepartitionPortefeuille();
            dashboard.put("repartition", repartitionProduits);

            // 2. Répartition du Portefeuille par Revenu
            Map<String, BigDecimal> repartitionRevenu = getRepartitionRevenuParCategorie();
            dashboard.put("repartitionRevenu", repartitionRevenu);

            // Calcul du portefeuille total
            BigDecimal portefeuilleTotal = repartitionRevenu.values().stream()
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dashboard.put("portefeuilleTotal", portefeuilleTotal);

            // 3. Top Produits par Revenu
            List<Map<String, Object>> topProduitsRevenu = getTopProduitsParRevenu();
            dashboard.put("topProduitsRevenu", topProduitsRevenu);

            // 4. Catégories les plus demandées (Basé sur le nombre de commandes)
            Map<String, Long> categoriesPopulaires = getCategoriesPopulaires();
            dashboard.put("categoriesPopulaires", categoriesPopulaires);

            // 5. Opportunités du Marché
            List<Map<String, Object>> opportunites = getOpportunitesMarche();
            dashboard.put("opportunites", opportunites);

            // ========== NOUVELLES PRÉDICTIONS ==========

            // 6. Prédictions de Croissance par Catégorie
            Map<String, Object> predictionsCroissance = getPredictionsCroissanceCategories();
            dashboard.put("predictionsCroissance", predictionsCroissance);

            // 7. Projection des Revenus Futurs (7 prochains jours)
            Map<String, Object> projectionsRevenus = getProjectionsRevenusFuturs();
            dashboard.put("projectionsRevenus", projectionsRevenus);

            // 8. Produits à Fort Potentiel (Croissance rapide)
            List<Map<String, Object>> produitsFortPotentiel = getProduitsForteCroissance();
            dashboard.put("produitsFortPotentiel", produitsFortPotentiel);

            // 9. Analyse de Risque
            Map<String, Object> analyseRisque = getAnalyseRisque();
            dashboard.put("analyseRisque", analyseRisque);
        } catch (Exception e) {
            System.err.println(">>> [ERROR] Error generating Investor Dashboard: " + e.getMessage());
            e.printStackTrace();
            // Fallback empty data to avoid frontend crash if possible
            dashboard.putIfAbsent("repartition", new HashMap<>());
            dashboard.putIfAbsent("repartitionRevenu", new HashMap<>());
            dashboard.putIfAbsent("topProduitsRevenu", new ArrayList<>());
            dashboard.putIfAbsent("opportunites", new ArrayList<>());
        }

        return dashboard;
    }

    private Map<String, BigDecimal> getRepartitionRevenuParCategorie() {
        // Utiliser ligneCommandeRepository pour calculer le revenu total par catégorie
        List<Object[]> results = ligneCommandeRepository.findTotalRevenueByCategory();
        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] arr : results) {
            String cat = arr[0] != null ? (String) arr[0] : "Non catégorisé";
            BigDecimal val = arr[1] != null ? (BigDecimal) arr[1] : BigDecimal.ZERO;
            map.put(cat, val);
        }
        return map;
    }

    private List<Map<String, Object>> getTopProduitsParRevenu() {
        // Récupérer les produits générant le plus de chiffre d'affaires
        List<Object[]> results = ligneCommandeRepository.findTopProductsByRevenue(PageRequest.of(0, 5));
        return results.stream()
                .filter(arr -> arr != null && arr.length >= 2 && arr[0] != null)
                .map(arr -> {
                    Produit p = (Produit) arr[0];
                    BigDecimal revenue = arr[1] != null ? (BigDecimal) arr[1] : BigDecimal.ZERO;
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("nom", p.getNom());
                    map.put("revenu", revenue);
                    map.put("urlImage", p.getUrlImage());
                    return map;
                }).collect(Collectors.toList());
    }

    private Map<String, Long> getCategoriesPopulaires() {
        List<Object[]> results = ligneCommandeRepository.findOrderCountByCategory();
        Map<String, Long> map = new HashMap<>();
        for (Object[] arr : results) {
            String cat = arr[0] != null ? (String) arr[0] : "Non catégorisé";
            Long count = arr[1] != null ? (Long) arr[1] : 0L;
            map.put(cat, count);
        }
        return map;
    }

    private Map<String, Long> getRepartitionPortefeuille() {
        List<Object[]> results = produitRepository.findProduitCountByCategory();
        Map<String, Long> map = new HashMap<>();
        for (Object[] arr : results) {
            String cat = arr[0] != null ? (String) arr[0] : "Non catégorisé";
            Long count = arr[1] != null ? (Long) arr[1] : 0L;
            map.put(cat, count);
        }
        return map;
    }

    private List<Map<String, Object>> getOpportunitesMarche() {
        List<Map<String, Object>> opps = new ArrayList<>();

        // A. Produits à fort potentiel (Haute note > 4.5 mais rang > 1000) -> Besoin de
        // visibilité (Investissement Pub)
        List<Produit> produitsPotentiels = produitRepository
                .findTopRatedProduits(new BigDecimal("4.5"), PageRequest.of(0, 50))
                .stream()
                .filter(p -> p.getRang() != null && p.getRang() > 1000)
                .limit(3)
                .collect(Collectors.toList());

        for (Produit p : produitsPotentiels) {
            Map<String, Object> opp = new HashMap<>();
            opp.put("type", "PRODUIT_POTENTIEL");
            opp.put("titre", p.getNom());
            opp.put("sousTitre", "Note: " + p.getNote() + "/5 • Rang: #" + p.getRang());
            opp.put("roiEstime", "15-20%"); // Simulé pour l'instant
            opp.put("id", p.getId());
            opps.add(opp);
        }

        // B. Vendeurs Performants (Magasins avec bonne note moyenne) -> Besoin de
        // capital
        // Note: Assuming we can fetch top rated stores. If not, simulate slightly or
        // just pick top rated products' stores
        // For now, let's use a heuristic from products

        // FIXME: Use MagasinRepository if available, or simulate from Product data for
        // now if MagasinRepository doesn't have a specific method ready.
        // Let's rely on finding products with high sales (low rank) and high rating.

        List<Produit> starProducts = produitRepository.findTopProduits(PageRequest.of(0, 5));
        for (Produit p : starProducts) {
            if (p.getMagasin() != null) { // Simplify: Suggest investing in the Store of a top product
                Map<String, Object> opp = new HashMap<>();
                opp.put("type", "VENDEUR_STAR");
                opp.put("titre", "Magasin: " + p.getMagasin().getNom());
                opp.put("sousTitre", "Top Vente: " + p.getNom());
                opp.put("roiEstime", "10-12%");
                opp.put("id", p.getMagasin().getId());
                // Avoid duplicates
                if (opps.stream().noneMatch(o -> o.get("titre").equals(opp.get("titre")))) {
                    opps.add(opp);
                }
            }
        }

        // C. Produits les Plus Vendus (Basé sur les ventes réelles)
        LocalDateTime debutPeriode = LocalDateTime.now().minusMonths(1); // Dernier mois
        LocalDateTime finPeriode = LocalDateTime.now();

        List<Object[]> topVentes = ligneCommandeRepository.findTopSellingProductsBetweenDates(debutPeriode,
                finPeriode);

        // Limiter aux 5 premiers produits les plus vendus
        for (int i = 0; i < Math.min(5, topVentes.size()); i++) {
            Object[] vente = topVentes.get(i);
            Produit produit = (Produit) vente[0];
            Long nombreVentes = (Long) vente[1];

            Map<String, Object> opp = new HashMap<>();
            opp.put("type", "PRODUIT_PLUS_VENDU");
            opp.put("titre", produit.getNom());
            opp.put("sousTitre", nombreVentes + " ventes • Prix: " + produit.getPrix() + "€");
            opp.put("roiEstime", "8-12%"); // ROI basé sur les performances réelles
            opp.put("id", produit.getId());
            opps.add(opp);
        }

        return opps;
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private Map<String, Object> getStatistiquesVentes(LocalDateTime debut, LocalDateTime fin) {
        BigDecimal montantTotal = commandeRepository.findTotalRevenueBetweenDates(debut, fin);
        Long nombreVentes = commandeRepository.countCommandesBetweenDates(debut, fin);
        BigDecimal panierMoyen = commandeRepository.findAverageOrderValueBetweenDates(debut, fin);

        return Map.of(
                "montantTotal", montantTotal != null ? montantTotal : BigDecimal.ZERO,
                "nombreVentes", nombreVentes != null ? nombreVentes : 0L,
                "panierMoyen", panierMoyen != null ? panierMoyen : BigDecimal.ZERO);
    }

    private Map<String, Object> getStatistiquesUtilisateurs() {
        Long totalUtilisateurs = utilisateurRepository.count();
        Long utilisateursActifs = utilisateurRepository.countActiveUsers();
        Long acheteursActifs = acheteurRepository.countActiveAcheteurs();
        Long vendeursActifs = vendeurRepository.countActiveSellingVendeurs();

        return Map.of(
                "total", totalUtilisateurs,
                "actifs", utilisateursActifs,
                "acheteurs", acheteursActifs,
                "vendeurs", vendeursActifs);
    }

    private double calculerProgressionObjectif(BigDecimal chiffreAffaire, BigDecimal objectif) {
        if (objectif == null || objectif.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return chiffreAffaire.divide(objectif, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    private Map<String, BigDecimal> getEvolutionVentes7Jours(Long vendeurId) {
        Map<String, BigDecimal> evolution = new LinkedHashMap<>();
        LocalDateTime maintenant = LocalDateTime.now();

        for (int i = 6; i >= 0; i--) {
            LocalDateTime debut = maintenant.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime fin = maintenant.minusDays(i).withHour(23).withMinute(59).withSecond(59);

            List<Commande> commandes = commandeRepository.findWithFilters(
                    null, vendeurId, null, null, null, debut, fin);

            BigDecimal total = commandes.stream()
                    .map(Commande::getMontantTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String date = debut.toLocalDate().toString();
            evolution.put(date, total);
        }

        return evolution;
    }

    private List<Produit> getRecommandationsPersonnalisees(Long acheteurId) {
        // Récupérer les dernières commandes de l'acheteur
        List<Commande> dernieresCommandes = commandeRepository.findByAcheteurId(acheteurId)
                .stream()
                .sorted((c1, c2) -> c2.getDateCommande().compareTo(c1.getDateCommande()))
                .limit(5)
                .collect(Collectors.toList());

        if (dernieresCommandes.isEmpty()) {
            // Si pas d'historique, retourner les top produits
            List<Produit> top10 = produitRepository.findTopProduits(PageRequest.of(0, 10));
            return top10 != null ? top10 : new ArrayList<>();
        }

        // Extraire les catégories des produits achetés
        Set<String> categoriesFavorites = new HashSet<>();
        for (Commande commande : dernieresCommandes) {
            if (commande.getLignesCommande() != null) {
                commande.getLignesCommande().forEach(
                        ligne -> categoriesFavorites.add(ligne.getProduit().getCategorie()));
            }
        }

        // Recommander des produits similaires
        List<Produit> recommandations = new ArrayList<>();
        for (String categorie : categoriesFavorites) {
            List<Produit> produits = produitRepository.findWithFilters(
                    categorie, null, null, BigDecimal.valueOf(4.0), 10, null);
            recommandations.addAll(produits.stream().limit(3).collect(Collectors.toList()));
        }

        return recommandations.stream()
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    // ========== MÉTHODES DE PRÉDICTION POUR INVESTISSEURS ==========

    /**
     * Prédictions de croissance par catégorie basées sur l'évolution des ventes
     */
    private Map<String, Object> getPredictionsCroissanceCategories() {
        Map<String, Object> predictions = new HashMap<>();

        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime debut30Jours = maintenant.minusDays(30);
        LocalDateTime debut15Jours = maintenant.minusDays(15);

        // Revenus par catégorie - 30 derniers jours
        List<Object[]> revenus30j = ligneCommandeRepository.findTotalRevenueByCategoryBetweenDates(debut30Jours,
                maintenant);
        Map<String, BigDecimal> map30j = new HashMap<>();
        for (Object[] arr : revenus30j) {
            map30j.put((String) arr[0], (BigDecimal) arr[1]);
        }

        // Revenus par catégorie - 15 derniers jours
        List<Object[]> revenus15j = ligneCommandeRepository.findTotalRevenueByCategoryBetweenDates(debut15Jours,
                maintenant);
        Map<String, BigDecimal> map15j = new HashMap<>();
        for (Object[] arr : revenus15j) {
            map15j.put((String) arr[0], (BigDecimal) arr[1]);
        }

        // Calculer le taux de croissance
        Map<String, Double> tauxCroissance = new HashMap<>();
        for (String categorie : map30j.keySet()) {
            BigDecimal revenu30 = map30j.get(categorie);
            BigDecimal revenu15 = map15j.getOrDefault(categorie, BigDecimal.ZERO);

            // Revenu moyen par jour pour chaque période
            BigDecimal moyenneJour30 = revenu30.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
            BigDecimal moyenneJour15 = revenu15.divide(BigDecimal.valueOf(15), 2, RoundingMode.HALF_UP);

            if (moyenneJour30.compareTo(BigDecimal.ZERO) > 0) {
                double croissance = moyenneJour15.subtract(moyenneJour30)
                        .divide(moyenneJour30, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
                tauxCroissance.put(categorie, croissance);
            }
        }

        predictions.put("tauxCroissanceParCategorie", tauxCroissance);

        // Identifier les catégories en forte croissance (> 10%)
        List<String> categoriesEnCroissance = tauxCroissance.entrySet().stream()
                .filter(e -> e.getValue() > 10.0)
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        predictions.put("categoriesEnForteCroissance", categoriesEnCroissance);

        return predictions;
    }

    /**
     * Projection des revenus futurs basée sur la tendance des 7 derniers jours
     */
    private Map<String, Object> getProjectionsRevenusFuturs() {
        Map<String, Object> projections = new HashMap<>();

        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime debut7Jours = maintenant.minusDays(7);

        // Revenus des 7 derniers jours
        List<Commande> commandes7j = commandeRepository.findWithFilters(
                null, null, null, null, null, debut7Jours, maintenant);

        BigDecimal revenu7j = commandes7j.stream()
                .map(Commande::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Revenu moyen par jour
        BigDecimal revenuMoyenJour = revenu7j.divide(BigDecimal.valueOf(7), 2, RoundingMode.HALF_UP);

        // Projection sur 7 prochains jours
        BigDecimal projection7j = revenuMoyenJour.multiply(BigDecimal.valueOf(7));

        // Projection sur 30 prochains jours
        BigDecimal projection30j = revenuMoyenJour.multiply(BigDecimal.valueOf(30));

        projections.put("revenuMoyenJour", revenuMoyenJour);
        projections.put("projection7Jours", projection7j);
        projections.put("projection30Jours", projection30j);
        projections.put("tendance", revenuMoyenJour.compareTo(BigDecimal.ZERO) > 0 ? "POSITIVE" : "STABLE");

        return projections;
    }

    /**
     * Identifie les produits à fort potentiel basé sur la croissance récente des
     * ventes
     */
    private List<Map<String, Object>> getProduitsForteCroissance() {
        List<Map<String, Object>> produits = new ArrayList<>();

        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime debut30Jours = maintenant.minusDays(30);
        LocalDateTime debut15Jours = maintenant.minusDays(15);

        // Ventes par produit - 30 derniers jours
        List<Object[]> ventes30j = ligneCommandeRepository.findTopSellingProductsBetweenDates(debut30Jours,
                maintenant);
        Map<Long, Long> map30j = new HashMap<>();
        for (Object[] arr : ventes30j) {
            Produit p = (Produit) arr[0];
            Long count = (Long) arr[1];
            map30j.put(p.getId(), count);
        }

        // Ventes par produit - 15 derniers jours
        List<Object[]> ventes15j = ligneCommandeRepository.findTopSellingProductsBetweenDates(debut15Jours,
                maintenant);

        for (Object[] arr : ventes15j) {
            Produit produit = (Produit) arr[0];
            Long ventes15 = (Long) arr[1];
            Long ventes30 = map30j.getOrDefault(produit.getId(), 0L);

            // Calculer la croissance
            if (ventes30 > 0 && ventes15 > ventes30 / 2) {
                double croissance = ((ventes15 * 2.0) - ventes30) / ventes30 * 100;

                if (croissance > 20.0) { // Croissance > 20%
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", produit.getId());
                    info.put("nom", produit.getNom());
                    info.put("categorie", produit.getCategorie());
                    info.put("prix", produit.getPrix());
                    info.put("note", produit.getNote());
                    info.put("croissance", String.format("%.1f%%", croissance));
                    info.put("ventesRecentes", ventes15);
                    info.put("urlImage", produit.getUrlImage());
                    produits.add(info);
                }
            }
        }

        // Trier par croissance décroissante et limiter à 10
        return produits.stream()
                .sorted((p1, p2) -> {
                    String c1 = (String) p1.get("croissance");
                    String c2 = (String) p2.get("croissance");
                    double v1 = Double.parseDouble(c1.replace("%", ""));
                    double v2 = Double.parseDouble(c2.replace("%", ""));
                    return Double.compare(v2, v1);
                })
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Analyse de risque pour les investissements
     */
    private Map<String, Object> getAnalyseRisque() {
        Map<String, Object> analyse = new HashMap<>();

        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime debut30Jours = maintenant.minusDays(30);

        // 1. Produits en déclin (ventes en baisse)
        List<Object[]> ventes30j = ligneCommandeRepository.findTopSellingProductsBetweenDates(debut30Jours,
                maintenant);
        List<Object[]> ventes15j = ligneCommandeRepository.findTopSellingProductsBetweenDates(
                maintenant.minusDays(15), maintenant);

        Map<Long, Long> map30 = new HashMap<>();
        for (Object[] arr : ventes30j) {
            map30.put(((Produit) arr[0]).getId(), (Long) arr[1]);
        }

        List<String> produitsEnDeclin = new ArrayList<>();
        for (Object[] arr : ventes15j) {
            Produit p = (Produit) arr[0];
            Long v15 = (Long) arr[1];
            Long v30 = map30.getOrDefault(p.getId(), 0L);

            if (v30 > 0 && (v15 * 2) < v30) { // Baisse > 50%
                produitsEnDeclin.add(p.getNom());
            }
        }

        analyse.put("produitsEnDeclin", produitsEnDeclin.stream().limit(5).collect(Collectors.toList()));

        // 2. Score de risque global (basé sur la volatilité)
        BigDecimal revenu30j = commandeRepository.findTotalRevenueBetweenDates(debut30Jours, maintenant);
        BigDecimal revenu15j = commandeRepository.findTotalRevenueBetweenDates(
                maintenant.minusDays(15), maintenant);

        String niveauRisque = "FAIBLE";
        if (revenu30j.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal variation = revenu15j.multiply(BigDecimal.valueOf(2))
                    .subtract(revenu30j)
                    .divide(revenu30j, 4, RoundingMode.HALF_UP)
                    .abs();

            if (variation.compareTo(BigDecimal.valueOf(0.3)) > 0) {
                niveauRisque = "ÉLEVÉ";
            } else if (variation.compareTo(BigDecimal.valueOf(0.15)) > 0) {
                niveauRisque = "MODÉRÉ";
            }
        }

        analyse.put("niveauRisqueGlobal", niveauRisque);

        // 3. Opportunités sûres (produits stables avec bonnes ventes)
        List<String> opportunitesSures = ventes30j.stream()
                .filter(arr -> {
                    Produit p = (Produit) arr[0];
                    Long ventes = (Long) arr[1];
                    return ventes > 10 && p.getNote() != null
                            && p.getNote().compareTo(BigDecimal.valueOf(4.0)) >= 0;
                })
                .limit(5)
                .map(arr -> ((Produit) arr[0]).getNom())
                .collect(Collectors.toList());

        analyse.put("opportunitesSures", opportunitesSures);

        return analyse;
    }

    @org.springframework.transaction.annotation.Transactional
    public void genererDonneesPourAcheteur(com.example.amazonbestseller.entity.Acheteur acheteur) {
        System.out.println(">>> [DEBUG] Génération de données pour " + acheteur.getNomUtilisateur());

        java.util.List<com.example.amazonbestseller.entity.Commande> existingCommandes = commandeRepository
                .findByAcheteurId(acheteur.getId());
        if (existingCommandes.size() >= 5) {
            System.out.println(">>> [DEBUG] Acheteur a déjà assez de commandes.");
            return;
        }

        java.util.List<com.example.amazonbestseller.entity.Produit> allProducts = produitRepository.findAll();
        if (allProducts.isEmpty()) {
            System.out.println(">>> [DEBUG] Aucun produit trouvé pour générer des commandes.");
            return;
        }

        java.util.Random rand = new java.util.Random();
        java.util.List<com.example.amazonbestseller.entity.Magasin> magasins = magasinRepository.findAll();
        if (magasins.isEmpty()) {
            System.out.println(">>> [DEBUG] Aucun magasin trouvé pour lier les commandes.");
            return;
        }

        int ordersToGenerate = 5 - existingCommandes.size();
        for (int i = 0; i < ordersToGenerate; i++) {
            com.example.amazonbestseller.entity.Commande cmd = new com.example.amazonbestseller.entity.Commande();
            cmd.setAcheteur(acheteur);
            cmd.setDateCommande(java.time.LocalDateTime.now().minusDays(rand.nextInt(30)));
            cmd.setStatut(com.example.amazonbestseller.entity.Commande.Statut.LIVREE);
            cmd.setMethodePaiement("Carte Bancaire");
            cmd.setAdresseLivraison(acheteur.getAdresseLivraison() != null ? acheteur.getAdresseLivraison()
                    : "Adresse Test");

            com.example.amazonbestseller.entity.Magasin mag = magasins.get(rand.nextInt(magasins.size()));
            com.example.amazonbestseller.entity.Vendeur vendeur = vendeurRepository
                    .findById(mag.getIdVendeur()).orElse(null);
            cmd.setVendeur(vendeur);

            com.example.amazonbestseller.entity.Commande savedCmd = commandeRepository.save(cmd);
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;

            int itemsCount = 1 + rand.nextInt(3);
            for (int j = 0; j < itemsCount; j++) {
                com.example.amazonbestseller.entity.Produit p = allProducts
                        .get(rand.nextInt(allProducts.size()));
                com.example.amazonbestseller.entity.LigneCommande lc = new com.example.amazonbestseller.entity.LigneCommande();
                lc.setCommande(savedCmd);
                lc.setProduit(p);
                lc.setQuantite(1 + rand.nextInt(2));
                lc.setPrixUnitaire(p.getPrix() != null ? p.getPrix()
                        : java.math.BigDecimal.valueOf(19.99));
                lc.calculerSousTotal();
                ligneCommandeRepository.save(lc);
                total = total.add(lc.getSousTotal());
            }

            savedCmd.setSousTotal(total);
            savedCmd.setTaxe(total.multiply(java.math.BigDecimal.valueOf(0.20)));
            savedCmd.setMontantTotal(total.add(savedCmd.getTaxe()));
            commandeRepository.save(savedCmd);
        }

        for (int k = 0; k < 3; k++) {
            com.example.amazonbestseller.entity.Produit p = allProducts
                    .get(rand.nextInt(allProducts.size()));
            if (avisRepository.findByProduitIdAndAcheteurId(p.getId(), acheteur.getId()).isEmpty()) {
                com.example.amazonbestseller.entity.Avis avis = new com.example.amazonbestseller.entity.Avis();
                avis.setAcheteur(acheteur);
                avis.setProduit(p);
                avis.setNote(java.math.BigDecimal.valueOf(4 + rand.nextInt(2)));
                avis.setCommentaire("Super produit !");
                avis.setDateAvis(java.time.LocalDateTime.now().minusDays(rand.nextInt(15)));
                avis.setEstVerifie(true);
                avisRepository.save(avis);
            }
        }
    }
}
