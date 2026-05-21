// File: AnalysePredictiveService.java
package com.example.amazonbestseller.service;

import com.example.amazonbestseller.entity.Produit;
import com.example.amazonbestseller.entity.Vente;
import com.example.amazonbestseller.repository.ProduitRepository;
import com.example.amazonbestseller.repository.VenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysePredictiveService {

    private final ProduitRepository produitRepository;
    private final VenteRepository venteRepository;

    // ========== PRÉDICTION DE POSITION FUTURE ==========

    public Map<String, Object> predirePositionFuture(Long produitId, int joursAvenir) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        // Calculer les tendances basées sur l'historique des ventes
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime il7Jours = maintenant.minusDays(7);
        LocalDateTime il14Jours = maintenant.minusDays(14);
        LocalDateTime il30Jours = maintenant.minusDays(30);

        // Récupérer les ventes récentes
        List<Vente> ventes7j = venteRepository.findWithFilters(produitId, null, null, null, il7Jours, maintenant);
        List<Vente> ventes14j = venteRepository.findWithFilters(produitId, null, null, null, il14Jours, maintenant);
        List<Vente> ventes30j = venteRepository.findWithFilters(produitId, null, null, null, il30Jours, maintenant);

        // Calculer les métriques de tendance
        double tendanceVentes = calculerTendanceVentes(ventes7j, ventes14j, ventes30j);
        double scorePopularite = calculerScorePopularite(produit);
        double scorePrix = calculerScorePrix(produit);
        double scoreAvis = calculerScoreAvis(produit);

        // Prédiction composite
        int rangActuel = produit.getRang();
        int changementPredit = (int) Math.round(
                (tendanceVentes * 0.4) +
                        (scorePopularite * 0.3) +
                        (scorePrix * 0.2) +
                        (scoreAvis * 0.1));

        int nouveauRangPredit = Math.max(1, rangActuel + changementPredit);

        String tendance;
        if (changementPredit < -5)
            tendance = "HAUSSE_FORTE";
        else if (changementPredit < 0)
            tendance = "HAUSSE_MODEREE";
        else if (changementPredit == 0)
            tendance = "STABLE";
        else if (changementPredit < 5)
            tendance = "BAISSE_MODEREE";
        else
            tendance = "BAISSE_FORTE";

        double confiance = calculerConfiance(ventes30j.size(), produit.getNombreAvis());

        return Map.of(
                "produitId", produitId,
                "rangActuel", rangActuel,
                "rangPredit", nouveauRangPredit,
                "changement", changementPredit,
                "tendance", tendance,
                "confiance", confiance,
                "facteurs", Map.of(
                        "tendanceVentes", tendanceVentes,
                        "scorePopularite", scorePopularite,
                        "scorePrix", scorePrix,
                        "scoreAvis", scoreAvis));
    }

    private double calculerTendanceVentes(List<Vente> ventes7j, List<Vente> ventes14j, List<Vente> ventes30j) {
        int count7j = ventes7j.size();
        int count14j = ventes14j.size();
        int count30j = ventes30j.size();

        // Calculer la croissance hebdomadaire
        double croissance7vs14 = count7j - (count14j - count7j);
        double croissance14vs30 = (count14j - count7j) - (count30j - count14j);

        // Tendance négative = amélioration du rang (meilleur)
        return -(croissance7vs14 + croissance14vs30) / 2.0;
    }

    private double calculerScorePopularite(Produit produit) {
        // Score basé sur le nombre d'avis et la note
        int avis = produit.getNombreAvis() != null ? produit.getNombreAvis() : 0;
        BigDecimal note = produit.getNote() != null ? produit.getNote() : BigDecimal.ZERO;

        if (avis < 10)
            return 5;
        if (avis < 50)
            return 2;
        if (avis < 100)
            return 0;
        if (avis < 500 && note.compareTo(BigDecimal.valueOf(4)) >= 0)
            return -2;
        if (avis >= 500 && note.compareTo(BigDecimal.valueOf(4.5)) >= 0)
            return -5;

        return 0;
    }

    private double calculerScorePrix(Produit produit) {
        if (produit.getPrix() == null)
            return 0;

        // Comparer avec la moyenne de la catégorie
        List<Produit> produitsCategorie = produitRepository.findByCategorie(produit.getCategorie());

        if (produitsCategorie.isEmpty())
            return 0;

        BigDecimal prixMoyen = produitsCategorie.stream()
                .map(p -> p.getPrix() != null ? p.getPrix() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(produitsCategorie.size()), 2, RoundingMode.HALF_UP);

        if (prixMoyen.compareTo(BigDecimal.ZERO) == 0)
            return 0;

        BigDecimal ratio = produit.getPrix().divide(prixMoyen, 2, RoundingMode.HALF_UP);

        if (ratio.compareTo(BigDecimal.valueOf(0.7)) < 0)
            return -3; // Très bon prix
        if (ratio.compareTo(BigDecimal.valueOf(0.9)) < 0)
            return -1; // Bon prix
        if (ratio.compareTo(BigDecimal.valueOf(1.1)) <= 0)
            return 0; // Prix moyen
        if (ratio.compareTo(BigDecimal.valueOf(1.3)) < 0)
            return 2; // Prix élevé
        return 5; // Prix très élevé
    }

    private double calculerScoreAvis(Produit produit) {
        BigDecimal note = produit.getNote() != null ? produit.getNote() : BigDecimal.ZERO;

        if (note.compareTo(BigDecimal.valueOf(4.7)) >= 0)
            return -2;
        if (note.compareTo(BigDecimal.valueOf(4.3)) >= 0)
            return -1;
        if (note.compareTo(BigDecimal.valueOf(4.0)) >= 0)
            return 0;
        if (note.compareTo(BigDecimal.valueOf(3.5)) >= 0)
            return 1;
        return 3;
    }

    private double calculerConfiance(int nombreVentes, int nombreAvis) {
        // Confiance basée sur la quantité de données disponibles
        int score = 0;

        if (nombreVentes >= 100)
            score += 30;
        else if (nombreVentes >= 50)
            score += 20;
        else if (nombreVentes >= 20)
            score += 10;
        else
            score += 5;

        if (nombreAvis >= 1000)
            score += 70;
        else if (nombreAvis >= 500)
            score += 60;
        else if (nombreAvis >= 100)
            score += 40;
        else if (nombreAvis >= 50)
            score += 20;
        else
            score += 10;

        return Math.min(100, score);
    }

    // ========== IDENTIFIER LES FUTURS BEST-SELLERS ==========

    public List<Map<String, Object>> identifierFutursBestSellers() {
        List<Produit> produits = produitRepository.findByEstDisponibleTrue();
        List<Map<String, Object>> candidats = new ArrayList<>();

        for (Produit produit : produits) {
            // Ignorer les produits déjà dans le top 50
            if (produit.getRang() != null && produit.getRang() <= 50)
                continue;

            double score = calculerPotentielBestSeller(produit);

            if (score >= 70) { // Seuil de potentiel élevé
                candidats.add(Map.of(
                        "produit", produit,
                        "scorePotentiel", score,
                        "facteurs", analyserFacteursPotentiel(produit)));
            }
        }

        // Trier par score décroissant
        candidats.sort((a, b) -> Double.compare((Double) b.get("scorePotentiel"), (Double) a.get("scorePotentiel")));

        return candidats.stream().limit(20).collect(Collectors.toList());
    }

    private double calculerPotentielBestSeller(Produit produit) {
        double score = 0;
        BigDecimal note = produit.getNote() != null ? produit.getNote() : BigDecimal.ZERO;
        int nbAvis = produit.getNombreAvis() != null ? produit.getNombreAvis() : 0;

        // Croissance récente des ventes (40 points)
        LocalDateTime il7Jours = LocalDateTime.now().minusDays(7);
        LocalDateTime il30Jours = LocalDateTime.now().minusDays(30);
        List<Vente> ventes7j = venteRepository.findByProduitId(produit.getId());

        long ventesRecentes = ventes7j.stream()
                .filter(v -> v.getDateVente().isAfter(il7Jours))
                .count();
        long ventesAnciennes = ventes7j.stream()
                .filter(v -> v.getDateVente().isBefore(il7Jours) && v.getDateVente().isAfter(il30Jours))
                .count();

        if (ventesRecentes > ventesAnciennes * 2)
            score += 40;
        else if (ventesRecentes > ventesAnciennes * 1.5)
            score += 30;
        else if (ventesRecentes > ventesAnciennes)
            score += 20;

        // Note élevée (30 points)
        if (note.compareTo(BigDecimal.valueOf(4.5)) >= 0)
            score += 30;
        else if (note.compareTo(BigDecimal.valueOf(4.0)) >= 0)
            score += 20;
        else if (note.compareTo(BigDecimal.valueOf(3.5)) >= 0)
            score += 10;

        // Nombre d'avis croissant (20 points)
        if (nbAvis >= 100)
            score += 20;
        else if (nbAvis >= 50)
            score += 15;
        else if (nbAvis >= 20)
            score += 10;

        // Prix compétitif (10 points)
        score += Math.max(0, 10 - calculerScorePrix(produit));

        return score;
    }

    private Map<String, String> analyserFacteursPotentiel(Produit produit) {
        Map<String, String> facteurs = new HashMap<>();
        BigDecimal note = produit.getNote() != null ? produit.getNote() : BigDecimal.ZERO;
        int nbAvis = produit.getNombreAvis() != null ? produit.getNombreAvis() : 0;

        if (note.compareTo(BigDecimal.valueOf(4.5)) >= 0) {
            facteurs.put("note", "Excellente note client");
        }

        LocalDateTime il7Jours = LocalDateTime.now().minusDays(7);
        List<Vente> ventesRecentes = venteRepository.findWithFilters(
                produit.getId(), null, null, null, il7Jours, LocalDateTime.now());

        if (ventesRecentes.size() > 10) {
            facteurs.put("ventes", "Forte croissance des ventes");
        }

        if (nbAvis >= 50) {
            facteurs.put("avis", "Bonne base d'avis clients");
        }

        return facteurs;
    }

    // ========== RECOMMANDER LE PRIX IDÉAL ==========

    public Map<String, Object> recommanderPrixIdeal(Long produitId) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        if (produit.getPrix() == null) {
            return Map.of("message", "Le produit n'a pas de prix défini.");
        }

        // Analyser les produits similaires
        List<Produit> concurrents = produitRepository.findWithFilters(
                produit.getCategorie(), null, null,
                BigDecimal.valueOf(3.5), 10, null);

        if (concurrents.isEmpty()) {
            return Map.of("message", "Pas assez de données pour recommander un prix");
        }

        // Calculer statistiques de prix
        BigDecimal prixMin = concurrents.stream()
                .map(p -> p.getPrix() != null ? p.getPrix() : BigDecimal.ZERO)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal prixMax = concurrents.stream()
                .map(p -> p.getPrix() != null ? p.getPrix() : BigDecimal.ZERO)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal prixMoyen = concurrents.stream()
                .map(p -> p.getPrix() != null ? p.getPrix() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(concurrents.size()), 2, RoundingMode.HALF_UP);

        // Recommandation basée sur la stratégie
        BigDecimal prixOptimal;
        String strategie;

        BigDecimal note = produit.getNote() != null ? produit.getNote() : BigDecimal.ZERO;
        int nbAvis = produit.getNombreAvis() != null ? produit.getNombreAvis() : 0;
        int rang = produit.getRang() != null ? produit.getRang() : Integer.MAX_VALUE;

        if (note.compareTo(BigDecimal.valueOf(4.5)) >= 0 && nbAvis >= 100) {
            // Produit premium - prix au-dessus de la moyenne
            prixOptimal = prixMoyen.multiply(BigDecimal.valueOf(1.1));
            strategie = "PREMIUM";
        } else if (rang > 100) {
            // Nouveau produit - prix compétitif
            prixOptimal = prixMoyen.multiply(BigDecimal.valueOf(0.9));
            strategie = "PENETRATION";
        } else {
            // Prix standard
            prixOptimal = prixMoyen;
            strategie = "STANDARD";
        }

        // Calculer l'impact estimé
        BigDecimal impactVentes = calculerImpactPrix(produit.getPrix(), prixOptimal);

        return Map.of(
                "prixActuel", produit.getPrix(),
                "prixRecommande", prixOptimal.setScale(2, RoundingMode.HALF_UP),
                "prixMin", prixMin,
                "prixMax", prixMax,
                "prixMoyen", prixMoyen,
                "strategie", strategie,
                "impactEstime", impactVentes + "% de ventes",
                "justification", genererJustificationPrix(produit, prixOptimal, strategie));
    }

    private BigDecimal calculerImpactPrix(BigDecimal prixActuel, BigDecimal prixNouveau) {
        if (prixActuel == null || prixActuel.compareTo(BigDecimal.ZERO) == 0)
            return BigDecimal.ZERO;

        // Élasticité-prix simplifiée
        BigDecimal variation = prixNouveau.subtract(prixActuel)
                .divide(prixActuel, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        // Impact inversé : baisse de prix = hausse des ventes
        return variation.multiply(BigDecimal.valueOf(-1.5)).setScale(1, RoundingMode.HALF_UP);
    }

    private String genererJustificationPrix(Produit produit, BigDecimal prixOptimal, String strategie) {
        switch (strategie) {
            case "PREMIUM":
                return "Votre excellent score (note " + produit.getNote() +
                        " avec " + produit.getNombreAvis() + " avis) justifie un prix premium.";
            case "PENETRATION":
                return "Un prix compétitif aidera à améliorer votre rang actuel (#" +
                        produit.getRang() + ") et gagner des parts de marché.";
            default:
                return "Prix aligné sur la moyenne du marché pour maintenir la compétitivité.";
        }
    }
}