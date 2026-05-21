// File: AlerteService.java
package com.example.amazonbestseller.service;

import com.example.amazonbestseller.dto.AlerteDTO;
import com.example.amazonbestseller.entity.Alerte;
import com.example.amazonbestseller.entity.Alerte.Priorite;
import com.example.amazonbestseller.entity.Produit;
import com.example.amazonbestseller.entity.Utilisateur;
import com.example.amazonbestseller.repository.AlerteRepository;
import com.example.amazonbestseller.repository.ProduitRepository;
import com.example.amazonbestseller.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlerteService {

    private final AlerteRepository alerteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ProduitRepository produitRepository;

    // ========== CRÉATION D'ALERTES ==========

    @Transactional
    public Alerte creerAlerte(Long utilisateurId, String type, String message, Priorite priorite) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Alerte alerte = new Alerte();
        alerte.setUtilisateur(utilisateur);
        alerte.setType(type);
        alerte.setMessage(message);
        alerte.setPriorite(priorite);
        alerte.setEstLu(false);
        alerte.setDateCreation(LocalDateTime.now());

        return alerteRepository.save(alerte);
    }

    @Transactional
    public Alerte creerAlerteDTO(AlerteDTO alerteDTO) {
        return creerAlerte(
                alerteDTO.getUtilisateurId(),
                alerteDTO.getType(),
                alerteDTO.getMessage(),
                alerteDTO.getPriorite());
    }

    // ========== ALERTES AUTOMATIQUES ==========

    @Transactional
    public void creerAlerteNouveauTop10(Produit produit) {
        String message = String.format(
                "Nouveau produit dans le Top 10 : %s (Rang #%d)",
                produit.getNom(),
                produit.getRang());

        // Créer une alerte pour tous les utilisateurs actifs (ou administrateurs)
        List<Utilisateur> admins = utilisateurRepository.findByRole(Utilisateur.Role.ADMIN);

        for (Utilisateur admin : admins) {
            creerAlerte(admin.getId(), "NOUVEAU_TOP_10", message, Priorite.HAUTE);
        }
    }

    @Transactional
    public void creerAlerteChangementCategorie(Produit produit, String ancienneCategorie) {
        String message = String.format(
                "Changement de catégorie pour '%s' : %s → %s",
                produit.getNom(),
                ancienneCategorie,
                produit.getCategorie());

        List<Utilisateur> admins = utilisateurRepository.findByRole(Utilisateur.Role.ADMIN);

        for (Utilisateur admin : admins) {
            creerAlerte(admin.getId(), "CHANGEMENT_CATEGORIE", message, Priorite.MOYENNE);
        }
    }

    @Transactional
    public void creerAlerteCategorieTop10(String categorie) {
        String message = String.format(
                "La catégorie '%s' a maintenant des produits dans le Top 10",
                categorie);

        List<Utilisateur> admins = utilisateurRepository.findByRole(Utilisateur.Role.ADMIN);

        for (Utilisateur admin : admins) {
            creerAlerte(admin.getId(), "CATEGORIE_TOP_10", message, Priorite.HAUTE);
        }
    }

    @Transactional
    public void creerAlerteStockFaible(Produit produit, Integer quantite) {
        String message = String.format(
                "Stock faible pour '%s' : %d unités restantes",
                produit.getNom(),
                quantite);

        List<Utilisateur> vendeurs = utilisateurRepository.findByRole(Utilisateur.Role.VENDEUR);

        for (Utilisateur vendeur : vendeurs) {
            creerAlerte(vendeur.getId(), "STOCK_FAIBLE", message, Priorite.CRITIQUE);
        }
    }

    // ========== SURVEILLANCE AUTOMATIQUE (SCHEDULED) ==========

    @Scheduled(cron = "0 0 * * * *") // Toutes les heures
    @Transactional
    public void surveillerTop10() {
        List<Produit> top10 = produitRepository.findTopProduits(PageRequest.of(0, 10));

        for (Produit produit : top10) {
            // Vérifier si c'est un nouveau produit dans le top 10
            // (logique à adapter selon vos besoins)
            if (produit.getRang() <= 10) {
                // Créer une alerte si nécessaire
                // Cette logique nécessiterait un historique des rangs
            }
        }
    }

    @Scheduled(cron = "0 0 8 * * *") // Tous les jours à 8h
    @Transactional
    public void surveillerStock() {
        List<Produit> produitsStockFaible = produitRepository.findLowStockProduits();

        for (Produit produit : produitsStockFaible) {
            if (produit.getStock() != null && produit.getStock().getQuantite() < produit.getStock().getSeuilMin()) {
                creerAlerteStockFaible(produit, produit.getStock().getQuantite());
            }
        }
    }

    // ========== GESTION DES ALERTES ==========

    @Transactional
    public void marquerCommeLu(Long alerteId) {
        Alerte alerte = alerteRepository.findById(alerteId)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée"));

        alerte.setEstLu(true);
        alerteRepository.save(alerte);
    }

    @Transactional
    public void marquerPlusieursCommeLues(List<Long> alerteIds) {
        for (Long id : alerteIds) {
            marquerCommeLu(id);
        }
    }

    @Transactional
    public void marquerToutesCommeLues(Long utilisateurId) {
        List<Alerte> alertesNonLues = alerteRepository.findByUtilisateurIdAndEstLuFalse(utilisateurId);

        for (Alerte alerte : alertesNonLues) {
            alerte.setEstLu(true);
            alerteRepository.save(alerte);
        }
    }

    @Transactional
    public void supprimerAlerte(Long alerteId) {
        alerteRepository.deleteById(alerteId);
    }

    @Transactional
    public void supprimerAnciennesAlertes(int joursAnciennete) {
        LocalDateTime dateLimit = LocalDateTime.now().minusDays(joursAnciennete);
        alerteRepository.deleteOldReadAlertes(dateLimit);
    }

    // ========== RECHERCHE ET RÉCUPÉRATION ==========

    public Alerte getAlerteById(Long id) {
        return alerteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerte non trouvée"));
    }

    public List<Alerte> getAlertesUtilisateur(Long utilisateurId) {
        return alerteRepository.findByUtilisateurId(utilisateurId);
    }

    public List<Alerte> getAlertesNonLues(Long utilisateurId) {
        return alerteRepository.findByUtilisateurIdAndEstLuFalse(utilisateurId);
    }

    public List<Alerte> getAlertesParType(String type) {
        return alerteRepository.findByType(type);
    }

    public List<Alerte> getAlertesParPriorite(Priorite priorite) {
        return alerteRepository.findByPriorite(priorite);
    }

    public List<Alerte> getAlertesCritiquesNonLues() {
        return alerteRepository.findByPrioriteInAndEstLuFalse(
                List.of(Priorite.CRITIQUE, Priorite.HAUTE));
    }

    public List<Alerte> getAlertesRecentes(int limit) {
        return alerteRepository.findRecentUnreadAlertes(limit);
    }

    // ========== STATISTIQUES DES ALERTES ==========

    public Map<String, Object> getStatistiquesAlertes() {
        Long totalNonLues = alerteRepository.countUnreadAlertes();
        Long critiquesNonLues = alerteRepository.countCriticalUnreadAlertes();

        List<Object[]> parPriorite = alerteRepository.countUnreadAlertesByPriority();
        Map<Priorite, Long> distributionPriorite = parPriorite.stream()
                .collect(Collectors.toMap(
                        arr -> (Priorite) arr[0],
                        arr -> (Long) arr[1]));

        List<Object[]> typesFrequents = alerteRepository.findMostFrequentAlerteTypes();
        Map<String, Long> distributionTypes = typesFrequents.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]));

        return Map.of(
                "totalNonLues", totalNonLues,
                "critiquesNonLues", critiquesNonLues,
                "parPriorite", distributionPriorite,
                "parType", distributionTypes);
    }

    public Map<String, Object> getStatistiquesUtilisateur(Long utilisateurId) {
        List<Alerte> toutesAlertes = alerteRepository.findByUtilisateurId(utilisateurId);
        List<Alerte> nonLues = alerteRepository.findByUtilisateurIdAndEstLuFalse(utilisateurId);

        long critiques = nonLues.stream()
                .filter(a -> a.getPriorite() == Priorite.CRITIQUE)
                .count();

        return Map.of(
                "total", toutesAlertes.size(),
                "nonLues", nonLues.size(),
                "critiquesNonLues", critiques);
    }

    // ========== FILTRAGE AVANCÉ ==========

    public List<Alerte> filtrerAlertes(Long utilisateurId, String type,
            Priorite priorite, Boolean estLu,
            LocalDateTime dateDebut, LocalDateTime dateFin) {
        return alerteRepository.findWithFilters(
                utilisateurId, type, priorite, estLu, dateDebut, dateFin);
    }
}