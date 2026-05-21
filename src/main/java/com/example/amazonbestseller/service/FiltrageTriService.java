// File: FiltrageTriService.java
package com.example.amazonbestseller.service;

import com.example.amazonbestseller.dto.FiltreProduitsDTO;
import com.example.amazonbestseller.entity.Produit;
import com.example.amazonbestseller.repository.ProduitRepository;
import com.example.amazonbestseller.mapper.ProduitMapper;
import com.example.amazonbestseller.dto.ProduitDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class FiltrageTriService {

    private final ProduitRepository produitRepository;
    private final com.example.amazonbestseller.mapper.ProduitMapper produitMapper;

    /**
     * Filtrage et tri avancés des produits selon les critères spécifiés
     */
    public List<com.example.amazonbestseller.dto.ProduitDTO> filtrerEtTrierProduits(FiltreProduitsDTO filtres) {
        // 1. Appliquer les filtres via le repository
        List<Produit> produits = produitRepository.findWithFilters(
                filtres.getCategorie(),
                filtres.getPrixMin(),
                filtres.getPrixMax(),
                filtres.getNoteMin(),
                filtres.getNombreAvisMin(),
                filtres.getMotCle());

        // 2. Appliquer le tri
        if (filtres.getTriPar() != null) {
            produits = trierProduits(produits, filtres.getTriPar(), filtres.getOrdreTri());
        }

        return produits.stream()
                .map(produitMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tri des produits selon le critère spécifié (interne, garde Entité)
     */
    private List<Produit> trierProduits(List<Produit> produits, String triPar, String ordreTri) {
        Comparator<Produit> comparator;
        // ... (rest is same, keep implementation if possible or I replace whole block)
        switch (triPar.toLowerCase()) {
            case "rang":
            case "classement":
                comparator = Comparator.comparingInt(Produit::getRang);
                break;
            case "prix":
                comparator = Comparator.comparing(Produit::getPrix);
                break;
            case "note":
            case "rating":
                comparator = Comparator.comparing(Produit::getNote);
                break;
            case "avis":
            case "reviews":
            case "nombreavis":
                comparator = Comparator.comparingInt(Produit::getNombreAvis);
                break;
            case "nom":
            case "name":
                comparator = Comparator.comparing(Produit::getNom);
                break;
            case "date":
            case "dateajout":
                comparator = Comparator.comparing(Produit::getDateAjout);
                break;
            default:
                comparator = Comparator.comparingInt(Produit::getRang);
        }

        if ("desc".equalsIgnoreCase(ordreTri)) {
            comparator = comparator.reversed();
        }

        return produits.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Filtrage par catégorie uniquement
     */
    public List<Produit> filtrerParCategorie(String categorie) {
        return produitRepository.findByCategorie(categorie);
    }

    /**
     * Filtrage par fourchette de prix
     */
    public List<Produit> filtrerParPrix(BigDecimal min, BigDecimal max) {
        return produitRepository.findByPrixBetween(min, max);
    }

    /**
     * Filtrage par note minimale
     */
    public List<Produit> filtrerParNote(BigDecimal noteMin) {
        return produitRepository.findByNoteGreaterThanEqual(noteMin);
    }

    /**
     * Récupérer toutes les catégories disponibles
     */
    public List<String> getToutesCategories() {
        return produitRepository.findAll()
                .stream()
                .map(Produit::getCategorie)
                .filter(c -> c != null && !c.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les produits avec tri personnalisé
     */
    public List<Produit> getProduitsAvecTri(String triPar, String ordreTri, int limit) {
        List<Produit> produits = produitRepository.findByEstDisponibleTrue();

        List<Produit> produitsTriés = trierProduits(produits, triPar, ordreTri);

        return produitsTriés.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Recherche avec filtres multiples et tri
     */
    public List<com.example.amazonbestseller.dto.ProduitDTO> rechercheAvancee(String keyword,
            FiltreProduitsDTO filtres) {
        // 1. Recherche par mot-clé
        List<Produit> resultats = produitRepository.searchByKeyword(keyword);

        // 2. Appliquer les filtres de catégorie
        if (filtres.getCategorie() != null && !filtres.getCategorie().isEmpty()) {
            resultats = resultats.stream()
                    .filter(p -> p.getCategorie().equalsIgnoreCase(filtres.getCategorie()))
                    .collect(Collectors.toList());
        }

        // 3. Appliquer les filtres de prix
        if (filtres.getPrixMin() != null) {
            resultats = resultats.stream()
                    .filter(p -> p.getPrix().compareTo(filtres.getPrixMin()) >= 0)
                    .collect(Collectors.toList());
        }
        if (filtres.getPrixMax() != null) {
            resultats = resultats.stream()
                    .filter(p -> p.getPrix().compareTo(filtres.getPrixMax()) <= 0)
                    .collect(Collectors.toList());
        }

        // 4. Appliquer le filtre de note
        if (filtres.getNoteMin() != null) {
            resultats = resultats.stream()
                    .filter(p -> p.getNote().compareTo(filtres.getNoteMin()) >= 0)
                    .collect(Collectors.toList());
        }

        // 5. Appliquer le filtre d'avis
        if (filtres.getNombreAvisMin() != null) {
            resultats = resultats.stream()
                    .filter(p -> p.getNombreAvis() >= filtres.getNombreAvisMin())
                    .collect(Collectors.toList());
        }

        // 6. Appliquer le tri
        if (filtres.getTriPar() != null) {
            resultats = trierProduits(resultats, filtres.getTriPar(), filtres.getOrdreTri());
        }

        return resultats.stream()
                .map(produitMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les plages de prix pour les filtres
     */
    public PrixRange getPrixRange() {
        List<Produit> produits = produitRepository.findByEstDisponibleTrue();

        if (produits.isEmpty()) {
            return new PrixRange(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal min = produits.stream()
                .map(Produit::getPrix)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal max = produits.stream()
                .map(Produit::getPrix)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return new PrixRange(min, max);
    }

    // Classe interne pour la plage de prix
    public static class PrixRange {
        public final BigDecimal min;
        public final BigDecimal max;

        public PrixRange(BigDecimal min, BigDecimal max) {
            this.min = min;
            this.max = max;
        }
    }
}