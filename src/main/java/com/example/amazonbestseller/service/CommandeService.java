// File: CommandeService.java
package com.example.amazonbestseller.service;

import com.example.amazonbestseller.dto.CommandeDTO;
import com.example.amazonbestseller.entity.*;
import com.example.amazonbestseller.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final AcheteurRepository acheteurRepository;
    private final VendeurRepository vendeurRepository;
    private final LigneCommandeRepository ligneCommandeRepository;
    private final ProduitRepository produitRepository;
    private final StockRepository stockRepository;

    @Transactional
    public Commande creerCommande(CommandeDTO commandeDTO) {
        Acheteur acheteur = acheteurRepository.findById(commandeDTO.getAcheteurId())
                .orElseThrow(() -> new RuntimeException("Acheteur non trouvé"));

        Commande commande = new Commande();
        commande.setAcheteur(acheteur);
        commande.setDateCommande(LocalDateTime.now());
        commande.setStatut(Commande.Statut.EN_ATTENTE);
        commande.setMethodePaiement(commandeDTO.getMethodePaiement());

        BigDecimal montantTotal = BigDecimal.ZERO;
        Commande savedCommande = commandeRepository.save(commande);

        // Créer les lignes de commande
        for (CommandeDTO.LigneCommandeDTO ligneDTO : commandeDTO.getLignes()) {
            Produit produit = produitRepository.findById(ligneDTO.getProduitId())
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

            // Vérifier le stock
            Stock stock = stockRepository.findByProduitId(produit.getId())
                    .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

            if (stock.getQuantite() < ligneDTO.getQuantite()) {
                throw new RuntimeException("Stock insuffisant pour " + produit.getNom());
            }

            LigneCommande ligne = new LigneCommande();
            ligne.setCommande(savedCommande);
            ligne.setProduit(produit);
            ligne.setQuantite(ligneDTO.getQuantite());
            ligne.setPrixUnitaire(produit.getPrix());
            ligne.setSousTotal(produit.getPrix().multiply(BigDecimal.valueOf(ligneDTO.getQuantite())));

            ligneCommandeRepository.save(ligne);
            montantTotal = montantTotal.add(ligne.getSousTotal());

            // Réduire le stock
            stock.setQuantite(stock.getQuantite() - ligneDTO.getQuantite());
            stockRepository.save(stock);
        }

        savedCommande.setMontantTotal(montantTotal);
        return commandeRepository.save(savedCommande);
    }

    @Transactional
    public Commande confirmerCommande(Long commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        commande.setStatut(Commande.Statut.CONFIRMEE);
        return commandeRepository.save(commande);
    }

    @Transactional
    public Commande annulerCommande(Long commandeId) {
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        // Remettre le stock
        List<LigneCommande> lignes = ligneCommandeRepository.findByCommandeId(commandeId);
        for (LigneCommande ligne : lignes) {
            Stock stock = stockRepository.findByProduitId(ligne.getProduit().getId())
                    .orElseThrow(() -> new RuntimeException("Stock non trouvé"));
            stock.setQuantite(stock.getQuantite() + ligne.getQuantite());
            stockRepository.save(stock);
        }

        commande.setStatut(Commande.Statut.ANNULEE);
        return commandeRepository.save(commande);
    }

    public List<Commande> getCommandesAcheteur(Long acheteurId) {
        return commandeRepository.findByAcheteurId(acheteurId);
    }

    public List<Commande> getCommandesVendeur(Long vendeurId) {
        return commandeRepository.findByVendeurId(vendeurId);
    }

    public Commande getCommandeById(Long id) {
        return commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
    }

    public Map<String, Object> getStatistiquesCommandes(LocalDateTime debut, LocalDateTime fin) {
        Long total = commandeRepository.countCommandesBetweenDates(debut, fin);
        BigDecimal revenu = commandeRepository.findTotalRevenueBetweenDates(debut, fin);
        BigDecimal panierMoyen = commandeRepository.findAverageOrderValueBetweenDates(debut, fin);

        return Map.of(
                "nombreCommandes", total,
                "revenuTotal", revenu != null ? revenu : BigDecimal.ZERO,
                "panierMoyen", panierMoyen != null ? panierMoyen : BigDecimal.ZERO);
    }

    @Transactional
    public Commande creerVenteManuelle(Long vendeurId, Long produitId, int quantite, BigDecimal prixVente) {
        // Find Product and Stock
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        Stock stock = stockRepository.findByProduitId(produitId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

        if (stock.getQuantite() < quantite) {
            throw new RuntimeException("Stock insuffisant");
        }

        // Find or create a default "Manual Sale" buyer
        Acheteur acheteurDefault = acheteurRepository.findByEmail("acheteur@test.com")
                .orElseThrow(() -> new RuntimeException("Acheteur par défaut non trouvé (acheteur@test.com)"));

        // Find the Vendeur
        Vendeur vendeur = vendeurRepository.findById(vendeurId)
                .orElseThrow(() -> new RuntimeException("Vendeur non trouvé"));

        // Decrement Stock
        stock.setQuantite(stock.getQuantite() - quantite);
        stockRepository.save(stock);

        // Create Commande (Manual)
        Commande commande = new Commande();
        commande.setAcheteur(acheteurDefault);
        commande.setVendeur(vendeur);
        commande.setDateCommande(LocalDateTime.now());
        commande.setStatut(Commande.Statut.LIVREE); // Assumed sold locally

        BigDecimal sousTotal = prixVente.multiply(BigDecimal.valueOf(quantite));
        BigDecimal taxe = sousTotal.multiply(new BigDecimal("0.20")).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal total = sousTotal.add(taxe);

        commande.setSousTotal(sousTotal);
        commande.setTaxe(taxe);
        commande.setFraisLivraison(BigDecimal.ZERO);
        commande.setMontantTotal(total);
        commande.setMethodePaiement("ESPECES");
        commande.setAdresseLivraison("Vente en magasin");

        Commande saved = commandeRepository.save(commande);

        // Create LigneCommande
        LigneCommande ligne = new LigneCommande();
        ligne.setCommande(saved);
        ligne.setProduit(produit);
        ligne.setQuantite(quantite);
        ligne.setPrixUnitaire(prixVente);
        ligne.setSousTotal(sousTotal);
        ligneCommandeRepository.save(ligne);

        return saved;
    }

    @Transactional
    public Commande modifierCommande(Long id, Integer quantite, String statut) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        if (statut != null) {
            commande.setStatut(Commande.Statut.valueOf(statut));
        }

        if (quantite != null) {
            List<LigneCommande> lignes = ligneCommandeRepository.findByCommandeId(id);
            if (!lignes.isEmpty()) {
                LigneCommande ligne = lignes.get(0);
                int diff = quantite - ligne.getQuantite();

                Stock stock = stockRepository.findByProduitId(ligne.getProduit().getId())
                        .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

                if (stock.getQuantite() < diff) {
                    throw new RuntimeException("Stock insuffisant");
                }

                stock.setQuantite(stock.getQuantite() - diff);
                stockRepository.save(stock);

                ligne.setQuantite(quantite);
                ligne.setSousTotal(ligne.getPrixUnitaire().multiply(BigDecimal.valueOf(quantite)));
                ligneCommandeRepository.save(ligne);

                BigDecimal nouveauTotal = lignes.stream()
                        .map(LigneCommande::getSousTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                commande.setSousTotal(nouveauTotal);
                commande.setMontantTotal(
                        nouveauTotal.add(commande.getTaxe() != null ? commande.getTaxe() : BigDecimal.ZERO));
            }
        }

        return commandeRepository.save(commande);
    }
}