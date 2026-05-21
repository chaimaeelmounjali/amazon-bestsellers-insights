// File: VenteService.java
package com.example.amazonbestseller.service;

import com.example.amazonbestseller.dto.VenteDTO;
import com.example.amazonbestseller.entity.*;
import com.example.amazonbestseller.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenteService {

    private final VenteRepository venteRepository;
    private final ProduitRepository produitRepository;
    private final VendeurRepository vendeurRepository;
    private final AcheteurRepository acheteurRepository;
    private final StockRepository stockRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final CommandeRepository commandeRepository;
    private final com.example.amazonbestseller.repository.LigneCommandeRepository ligneCommandeRepository;

    // ========== CRÉATION ET GESTION DES VENTES ==========

    @Transactional
    public Vente creerVente(VenteDTO venteDTO) {
        // Récupérer les entités liées
        Produit produit = produitRepository.findById(venteDTO.getProduitId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        Vendeur vendeur = vendeurRepository.findById(venteDTO.getVendeurId())
                .orElseThrow(() -> new RuntimeException("Vendeur non trouvé"));

        Acheteur acheteur;
        if (venteDTO.getAcheteurId() != null) {
            acheteur = acheteurRepository.findById(venteDTO.getAcheteurId())
                    .orElseThrow(() -> new RuntimeException("Acheteur non trouvé"));
        } else if (venteDTO.getNomAcheteur() != null && !venteDTO.getNomAcheteur().isEmpty()) {
            // Chercher par nom ou créer un nouveau
            String nom = venteDTO.getNomAcheteur();
            acheteur = acheteurRepository.findByNomUtilisateur(nom)
                    .orElseGet(() -> {
                        Acheteur newAcheteur = new Acheteur();
                        newAcheteur.setNomUtilisateur(nom);
                        newAcheteur.setEmail(nom.replaceAll("\\s+", "").toLowerCase() + "_" + System.currentTimeMillis()
                                + "@example.com");
                        newAcheteur.setMotDePasse(passwordEncoder.encode("password")); // Mot de passe par défaut
                        newAcheteur.setRole(Utilisateur.Role.ACHETEUR);
                        newAcheteur.setEstActif(true);
                        newAcheteur.setDateInscription(LocalDateTime.now());
                        return acheteurRepository.save(newAcheteur);
                    });
        } else {
            throw new RuntimeException("Acheteur ID ou Nom requis");
        }

        // Vérifier la disponibilité du stock
        Stock stock = stockRepository.findByProduitId(produit.getId())
                .orElseThrow(() -> new RuntimeException("Stock non trouvé pour ce produit"));

        if (stock.getQuantite() < venteDTO.getQuantite()) {
            throw new RuntimeException("Stock insuffisant. Disponible: " + stock.getQuantite());
        }

        BigDecimal montantTotal = produit.getPrix().multiply(BigDecimal.valueOf(venteDTO.getQuantite()));

        // Créer une commande parente pour cette vente
        Commande commande = new Commande();
        commande.setAcheteur(acheteur);
        commande.setVendeur(vendeur);
        commande.setDateCommande(LocalDateTime.now());
        commande.setStatut(Commande.Statut.CONFIRMEE); // Vente directe = confirmée
        commande.setMontantTotal(montantTotal);
        commande.setSousTotal(montantTotal);
        commande.setMethodePaiement("ESPECES"); // Par défaut pour vente manuelle
        commande.setAdresseLivraison(acheteur.getAdresseLivraison());

        commande = commandeRepository.save(commande);

        // Créer une ligne de commande pour la cohérence avec le dashboard
        LigneCommande ligne = new LigneCommande();
        ligne.setCommande(commande);
        ligne.setProduit(produit);
        ligne.setQuantite(venteDTO.getQuantite());
        ligne.setPrixUnitaire(produit.getPrix());
        ligne.setSousTotal(montantTotal);
        ligneCommandeRepository.save(ligne);

        // Créer la vente
        Vente vente = new Vente();
        vente.setCommande(commande); // Liaison avec la commande
        vente.setProduit(produit);
        vente.setVendeur(vendeur);
        vente.setAcheteur(acheteur);
        vente.setQuantite(venteDTO.getQuantite());
        vente.setPrixUnitaire(produit.getPrix());

        vente.setMontantTotal(montantTotal);

        // Calculer la commission (par exemple 10%)
        BigDecimal commission = montantTotal.multiply(BigDecimal.valueOf(0.10));
        vente.setCommission(commission);

        vente.setStatut(Vente.Statut.VALIDEE);
        vente.setDateVente(LocalDateTime.now());

        Vente savedVente = venteRepository.save(vente);

        // Mettre à jour le stock
        stock.setQuantite(stock.getQuantite() - venteDTO.getQuantite());
        stock.setDerniereMaj(LocalDateTime.now());
        stockRepository.save(stock);

        // Mettre à jour les ventes totales du vendeur
        vendeur.setVentesTotales(vendeur.getVentesTotales().add(montantTotal));
        vendeurRepository.save(vendeur);

        return savedVente;
    }

    @Transactional
    public Vente modifierVente(Long id, VenteDTO venteDTO) {
        Vente vente = venteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vente non trouvée avec l'ID: " + id));

        if (vente.getStatut() == Vente.Statut.ANNULEE || vente.getStatut() == Vente.Statut.REMBOURSEE) {
            throw new RuntimeException("Impossible de modifier une vente annulée ou remboursée");
        }

        // Modification de la quantité
        if (venteDTO.getQuantite() != null && !venteDTO.getQuantite().equals(vente.getQuantite())) {
            int difference = venteDTO.getQuantite() - vente.getQuantite();

            Stock stock = stockRepository.findByProduitId(vente.getProduit().getId())
                    .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

            if (difference > 0 && stock.getQuantite() < difference) {
                throw new RuntimeException("Stock insuffisant pour augmenter la quantité");
            }

            stock.setQuantite(stock.getQuantite() - difference);
            stockRepository.save(stock);

            vente.setQuantite(venteDTO.getQuantite());
            BigDecimal nouveauMontant = vente.getPrixUnitaire().multiply(BigDecimal.valueOf(venteDTO.getQuantite()));
            vente.setMontantTotal(nouveauMontant);
            vente.setCommission(nouveauMontant.multiply(BigDecimal.valueOf(0.10)));
        }

        // Modification du statut
        if (venteDTO.getStatut() != null) {
            vente.setStatut(venteDTO.getStatut());
        }

        return venteRepository.save(vente);
    }

    @Transactional
    public void annulerVente(Long id, String motif) {
        Vente vente = venteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vente non trouvée avec l'ID: " + id));

        if (vente.getStatut() == Vente.Statut.ANNULEE) {
            throw new RuntimeException("Cette vente est déjà annulée");
        }

        // Remettre le stock
        Stock stock = stockRepository.findByProduitId(vente.getProduit().getId())
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

        stock.setQuantite(stock.getQuantite() + vente.getQuantite());
        stockRepository.save(stock);

        // Ajuster les ventes totales du vendeur
        Vendeur vendeur = vente.getVendeur();
        vendeur.setVentesTotales(vendeur.getVentesTotales().subtract(vente.getMontantTotal()));
        vendeurRepository.save(vendeur);

        // Marquer la vente comme annulée
        vente.setStatut(Vente.Statut.ANNULEE);
        venteRepository.save(vente);
    }

    // ========== VENTES PAR CATÉGORIE ==========

    public Map<String, Object> getVentesParCategorie(LocalDateTime dateDebut, LocalDateTime dateFin) {
        List<Object[]> ventes = venteRepository.findSalesByCategoryBetweenDates(dateDebut, dateFin);

        return ventes.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0], // catégorie
                        arr -> Map.of(
                                "montantTotal", arr[1], // BigDecimal
                                "nombreVentes", arr[2] // Long
                        )));
    }

    public List<Object[]> getTopProduitsVendus(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return venteRepository.findTopSellingProductsBetweenDates(dateDebut, dateFin);
    }

    // ========== STATISTIQUES DES VENTES ==========

    public Map<String, Object> getStatistiquesVentes(LocalDateTime dateDebut, LocalDateTime dateFin) {
        BigDecimal montantTotal = venteRepository.findTotalSalesBetweenDates(dateDebut, dateFin);
        Long nombreVentes = venteRepository.countSalesBetweenDates(dateDebut, dateFin);
        BigDecimal panierMoyen = venteRepository.findAverageSaleAmountBetweenDates(dateDebut, dateFin);

        return Map.of(
                "montantTotal", montantTotal != null ? montantTotal : BigDecimal.ZERO,
                "nombreVentes", nombreVentes,
                "panierMoyen", panierMoyen != null ? panierMoyen : BigDecimal.ZERO);
    }

    public List<Object[]> getVentesQuotidiennes(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return venteRepository.findDailySalesBetweenDates(dateDebut, dateFin);
    }

    public List<Object[]> getPerformanceVendeurs(LocalDateTime dateDebut, LocalDateTime dateFin) {
        return venteRepository.findSellerPerformanceBetweenDates(dateDebut, dateFin);
    }

    // ========== RECHERCHE ET FILTRAGE ==========

    public Vente getVenteById(Long id) {
        return venteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vente non trouvée avec l'ID: " + id));
    }

    public List<Vente> getVentesByProduit(Long produitId) {
        return venteRepository.findByProduitId(produitId);
    }

    public List<Vente> getVentesByVendeur(Long vendeurId) {
        return venteRepository.findByVendeurId(vendeurId);
    }

    public List<Vente> getVentesByAcheteur(Long acheteurId) {
        return venteRepository.findByAcheteurId(acheteurId);
    }

    public List<Vente> filtrerVentes(Long produitId, Long vendeurId, Long acheteurId,
            Vente.Statut statut, LocalDateTime dateDebut,
            LocalDateTime dateFin) {
        return venteRepository.findWithFilters(produitId, vendeurId, acheteurId,
                statut, dateDebut, dateFin);
    }

    public List<Vente> getVentesAnnuleesEtRemboursees() {
        return venteRepository.findCancelledAndRefundedSales(
                Vente.Statut.ANNULEE,
                Vente.Statut.REMBOURSEE);
    }

    // ========== ANALYSES AVANCÉES ==========

    public Map<String, Object> getAnalyseVendeur(Long vendeurId, LocalDateTime dateDebut, LocalDateTime dateFin) {
        List<Vente> ventes = venteRepository.findWithFilters(null, vendeurId, null, null, dateDebut, dateFin);

        if (ventes.isEmpty()) {
            return Map.of("message", "Aucune vente trouvée pour cette période");
        }

        BigDecimal montantTotal = ventes.stream()
                .map(Vente::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal commissionTotale = ventes.stream()
                .map(Vente::getCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer quantiteTotale = ventes.stream()
                .mapToInt(Vente::getQuantite)
                .sum();

        return Map.of(
                "nombreVentes", ventes.size(),
                "montantTotal", montantTotal,
                "commissionTotale", commissionTotale,
                "quantiteTotale", quantiteTotale,
                "panierMoyen",
                montantTotal.divide(BigDecimal.valueOf(ventes.size()), 2, java.math.RoundingMode.HALF_UP));
    }
}