// File: RechercheIntelligenceService.java
package com.example.amazonbestseller.service;

import com.example.amazonbestseller.entity.Produit;
import com.example.amazonbestseller.repository.ProduitRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RechercheIntelligenceService {

    private final ProduitRepository produitRepository;
    private final Map<String, List<String>> historiqueRecherches = new HashMap<>();

    // Dictionnaire de Synonymes
    private static final Map<String, String> SYNONYMES = new HashMap<>();
    static {
        SYNONYMES.put("ordi", "Informatique");
        SYNONYMES.put("laptop", "Informatique");
        SYNONYMES.put("portable", "Informatique");
        SYNONYMES.put("télé", "Électronique");
        SYNONYMES.put("tv", "Électronique");
        SYNONYMES.put("phone", "Téléphonie");
        SYNONYMES.put("mobile", "Téléphonie");
        SYNONYMES.put("smartphone", "Téléphonie");
        SYNONYMES.put("cellulaire", "Téléphonie");
        SYNONYMES.put("fringues", "Vêtements");
        SYNONYMES.put("habit", "Vêtements");
        SYNONYMES.put("bouquin", "Livres");
        SYNONYMES.put("roman", "Livres");
        SYNONYMES.put("baskets", "Chaussures");
        SYNONYMES.put("pompes", "Chaussures");
    }

    public List<Produit> rechercheSimple(String query) {
        return produitRepository.searchByKeyword(query);
    }

    public List<Produit> rechercheSemantique(String query) {
        query = query.toLowerCase().trim();

        // 1. Analyse des Intentions
        Map<String, Object> criteres = analyserRequete(query);

        // 2. Recherche et Filtrage Intelligent
        return rechercherAvecCriteres(criteres, query);
    }

    private Map<String, Object> analyserRequete(String query) {
        Map<String, Object> criteres = new HashMap<>();
        String[] words = query.split("\\s+");

        // A. Détection Catégorie via Synonymes et Mots-clés
        String[] categoriesConnues = { "électronique", "livres", "vêtements", "maison", "jouets", "sport",
                "informatique", "téléphonie", "chaussures" };

        for (String word : words) {
            // Check direct match
            for (String cat : categoriesConnues) {
                if (word.contains(cat)) {
                    criteres.put("categorie", cat);
                    break;
                }
            }
            // Check synonyms
            if (!criteres.containsKey("categorie") && SYNONYMES.containsKey(word)) {
                criteres.put("categorie", SYNONYMES.get(word));
            }
        }

        // B. Détection Prix
        if (query.matches(".*(sous|moins de|budget|pas cher).*\\d+.*")) {
            String prixStr = query.replaceAll("[^0-9]", "");
            if (!prixStr.isEmpty())
                criteres.put("prixMax", new BigDecimal(prixStr));
        } else if (query.contains("pas cher") || query.contains("petits prix") || query.contains("économique")) {
            criteres.put("sort", "price_asc");
        } else if (query.contains("premium") || query.contains("luxe") || query.contains("cher")) {
            criteres.put("sort", "price_desc");
        }

        // C. Détection Nouveauté
        if (query.contains("nouveau") || query.contains("récent") || query.contains("derniers")) {
            criteres.put("sort", "date_desc");
        }

        // D. Détection Qualité / Avis
        if (query.contains("meilleur") || query.contains("top") || query.contains("mieux noté")) {
            criteres.put("sort", "rating_desc");
            criteres.put("minNote", new BigDecimal("4.0"));
        }

        return criteres;
    }

    private List<Produit> rechercherAvecCriteres(Map<String, Object> criteres, String originalQuery) {
        // Commencer avec tous les produits disponibles (Optimisation: filtrer par
        // catégorie en SQL si possible)
        List<Produit> resultats;

        if (criteres.containsKey("categorie")) {
            // Find by generalized category search
            String cat = (String) criteres.get("categorie");
            resultats = produitRepository.findByCategorieContainingIgnoreCase(cat);
            if (resultats.isEmpty()) {
                // Fallback to all if category strict search fails
                resultats = produitRepository.findByEstDisponibleTrue();
            }
        } else {
            resultats = produitRepository.findByEstDisponibleTrue();
        }

        // Filtrage en mémoire (Stream)
        if (criteres.containsKey("prixMax")) {
            BigDecimal prixMax = (BigDecimal) criteres.get("prixMax");
            resultats = resultats.stream()
                    .filter(p -> p.getPrix().compareTo(prixMax) <= 0)
                    .collect(Collectors.toList());
        }

        if (criteres.containsKey("minNote")) {
            BigDecimal minNote = (BigDecimal) criteres.get("minNote");
            resultats = resultats.stream()
                    .filter(p -> p.getNote().compareTo(minNote) >= 0)
                    .collect(Collectors.toList());
        }

        // Scoring / Tri Pondéré
        String sortCriterium = (String) criteres.get("sort");

        // Calcul du score de pertinence textuelle (Titre > Description)
        final String searchTerms = originalQuery;

        Comparator<Produit> comparator = (p1, p2) -> {
            int score1 = calculateTextScore(p1, searchTerms);
            int score2 = calculateTextScore(p2, searchTerms);
            return Integer.compare(score2, score1); // Descending score
        };

        // Appliquer Tri Spécifique si demandé, sinon par pertinence
        if ("price_asc".equals(sortCriterium)) {
            comparator = Comparator.comparing(Produit::getPrix);
        } else if ("price_desc".equals(sortCriterium)) {
            comparator = Comparator.comparing(Produit::getPrix).reversed();
        } else if ("date_desc".equals(sortCriterium)) {
            comparator = Comparator.comparing(Produit::getDateAjout).reversed();
        } else if ("rating_desc".equals(sortCriterium)) {
            comparator = Comparator.comparing(Produit::getNote).reversed();
        }

        return resultats.stream()
                .sorted(comparator)
                .limit(50)
                .collect(Collectors.toList());
    }

    private int calculateTextScore(Produit p, String query) {
        int score = 0;
        String normalizedQuery = query.toLowerCase();

        if (p.getNom().toLowerCase().contains(normalizedQuery))
            score += 10;
        else {
            // Partial title match
            for (String word : normalizedQuery.split(" ")) {
                if (p.getNom().toLowerCase().contains(word))
                    score += 3;
            }
        }

        if (p.getDescription() != null && p.getDescription().toLowerCase().contains(normalizedQuery))
            score += 5;
        if (p.getCategorie().toLowerCase().contains(normalizedQuery))
            score += 5;

        return score;
    }

    // ========== HISTORIQUE DE RECHERCHE ==========

    public void ajouterAHistorique(String utilisateurId, String query) {
        historiqueRecherches.putIfAbsent(utilisateurId, new ArrayList<>());
        List<String> historique = historiqueRecherches.get(utilisateurId);

        // Ajouter au début et limiter à 20 recherches
        historique.add(0, query);
        if (historique.size() > 20) {
            historique.remove(historique.size() - 1);
        }
    }

    public List<String> getHistoriqueRecherche(String utilisateurId) {
        return historiqueRecherches.getOrDefault(utilisateurId, new ArrayList<>());
    }

    public void effacerHistorique(String utilisateurId) {
        historiqueRecherches.remove(utilisateurId);
    }

    // ========== SUGGESTIONS AUTOMATIQUES ==========

    public List<String> getSuggestions(String partialQuery) {
        if (partialQuery == null || partialQuery.length() < 2) {
            return new ArrayList<>();
        }

        List<String> suggestions = new ArrayList<>();

        // Suggestions de catégories
        String[] categories = { "Électronique", "Livres", "Vêtements", "Maison", "Jouets", "Sport" };
        for (String cat : categories) {
            if (cat.toLowerCase().startsWith(partialQuery.toLowerCase())) {
                suggestions.add(cat);
            }
        }

        // Suggestions de produits populaires
        List<Produit> produits = produitRepository.findTopProduits(PageRequest.of(0, 100));
        for (Produit p : produits) {
            if (p.getNom().toLowerCase().contains(partialQuery.toLowerCase())) {
                suggestions.add(p.getNom());
            }
        }

        // Suggestions de recherches communes
        String[] recherchesCourantes = {
                "Produits électroniques sous $50",
                "Livres avec +1000 reviews",
                "Meilleur rapport qualité-prix",
                "Best-sellers de la semaine",
                "Nouveautés"
        };

        for (String recherche : recherchesCourantes) {
            if (recherche.toLowerCase().contains(partialQuery.toLowerCase())) {
                suggestions.add(recherche);
            }
        }

        return suggestions.stream().limit(10).collect(Collectors.toList());
    }

    // ========== RECHERCHE PAR ASIN ==========

    public Produit rechercherParAsin(String asin) {
        return produitRepository.findByAsin(asin)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ASIN: " + asin));
    }

    // ========== PRODUITS SIMILAIRES ==========

    public List<Produit> getProduitsSimilaires(Long produitId) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        // Rechercher des produits dans la même catégorie avec des prix similaires
        BigDecimal prixMin = produit.getPrix().multiply(BigDecimal.valueOf(0.7));
        BigDecimal prixMax = produit.getPrix().multiply(BigDecimal.valueOf(1.3));

        List<Produit> similaires = produitRepository.findWithFilters(
                produit.getCategorie(),
                prixMin,
                prixMax,
                null,
                null,
                null);

        // Exclure le produit actuel et limiter à 10
        return similaires.stream()
                .filter(p -> !p.getId().equals(produitId))
                .limit(10)
                .collect(Collectors.toList());
    }
    // ========== ANALYSE PRÉDICTIVE (AI) ==========

    public List<Produit> predireFutursBestsellers() {
        // Logique : Produits récents avec une bonne note et beaucoup de vues (simulé
        // par rang)
        // On prend les produits ajoutés récemment qui ont une note > 4.5
        List<Produit> candidats = produitRepository.findTopRatedProduits(new BigDecimal("4.5"), PageRequest.of(0, 50));

        // On mélange pour la variété (simulation d'IA probabiliste)
        Collections.shuffle(candidats);
        return candidats.stream().limit(10).collect(Collectors.toList());
    }

    public Map<String, Object> predireRangFutur(Long produitId) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        // Simulation basée sur les métriques actuelles
        int rangActuel = produit.getRang() != null ? produit.getRang() : 100000;
        int rangPredit;
        String tendance;
        double confiance;

        if (produit.getNote() != null && produit.getNote().compareTo(new BigDecimal("4.5")) >= 0) {
            rangPredit = (int) (rangActuel * 0.85); // Amélioration de 15%
            tendance = "HAUSSIÈRE 📈";
            confiance = 85.5;
        } else if (produit.getNote() != null && produit.getNote().compareTo(new BigDecimal("3.5")) < 0) {
            rangPredit = (int) (rangActuel * 1.10); // Baisse de 10%
            tendance = "BAISSIÈRE 📉";
            confiance = 72.0;
        } else {
            rangPredit = rangActuel;
            tendance = "STABLE ➡️";
            confiance = 60.0;
        }

        return Map.of(
                "id", produitId,
                "rangActuel", rangActuel,
                "rangPredit", rangPredit,
                "tendance", tendance,
                "confiance", confiance);
    }

    public Map<String, Object> recommanderPrixIdeal(Long produitId) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        BigDecimal prixActuel = produit.getPrix();
        if (prixActuel == null)
            prixActuel = BigDecimal.ZERO;

        // Stratégie basique
        BigDecimal prixRecommande;
        String strategie;
        String justification;
        String impact;

        // Si beaucoup de vente (rang bas) et bonne note -> On peut augmenter le prix
        // (Premium)
        if (produit.getRang() != null && produit.getRang() < 1000 && produit.getNote() != null
                && produit.getNote().doubleValue() > 4.5) {
            prixRecommande = prixActuel.multiply(new BigDecimal("1.10")).setScale(2, java.math.RoundingMode.HALF_UP);
            strategie = "PREMIUM 💎";
            justification = "Forte demande et excellente réputation. Le marché peut absorber une hausse.";
            impact = "+8% Marge";
        }
        // Si peu de ventes -> Prix de pénétration
        else if (produit.getRang() != null && produit.getRang() > 50000) {
            prixRecommande = prixActuel.multiply(new BigDecimal("0.85")).setScale(2, java.math.RoundingMode.HALF_UP);
            strategie = "PÉNÉTRATION 🚀";
            justification = "Visibilité faible. Une baisse temporaire stimulera l'algorithme de classement.";
            impact = "+25% Volume Ventes";
        } else {
            prixRecommande = prixActuel;
            strategie = "MAINTIEN 🛡️";
            justification = "Le prix actuel est optimal par rapport à la concurrence directe.";
            impact = "Stable";
        }

        return Map.of(
                "prixActuel", prixActuel,
                "prixRecommande", prixRecommande,
                "strategie", strategie,
                "justification", justification,
                "impactEstime", impact);
    }
}