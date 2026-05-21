// File: LigneCommandeRepository.java
package com.example.amazonbestseller.repository;

import com.example.amazonbestseller.entity.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Long> {

        // Recherche par commande
        List<LigneCommande> findByCommandeId(Long commandeId);

        // Recherche par produit
        List<LigneCommande> findByProduitId(Long produitId);

        List<LigneCommande> findByProduitAsin(String asin);

        // Recherche par quantité
        List<LigneCommande> findByQuantiteGreaterThanEqual(Integer quantite);

        // Recherche par prix
        List<LigneCommande> findByPrixUnitaireBetween(BigDecimal min, BigDecimal max);

        List<LigneCommande> findBySousTotalGreaterThanEqual(BigDecimal montant);

        // Lignes de commande avec produits populaires
        @Query("SELECT lc.produit, SUM(lc.quantite) as totalQuantity, SUM(lc.sousTotal) as totalRevenue " +
                        "FROM LigneCommande lc " +
                        "WHERE lc.commande.dateCommande BETWEEN :start AND :end " +
                        "GROUP BY lc.produit " +
                        "ORDER BY totalQuantity DESC")
        List<Object[]> findTopSellingProductsBetweenDates(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // LigneCommande avec produits populaires sans date
        @Query("SELECT lc.produit, SUM(lc.quantite) as totalQuantity, SUM(lc.sousTotal) as totalRevenue " +
                        "FROM LigneCommande lc " +
                        "GROUP BY lc.produit " +
                        "ORDER BY totalQuantity DESC")
        List<Object[]> findTopSellingProducts();

        // Statistiques par produit
        @Query("SELECT SUM(lc.quantite) FROM LigneCommande lc WHERE lc.produit.id = :produitId")
        Long findTotalQuantitySoldByProduitId(@Param("produitId") Long produitId);

        @Query("SELECT SUM(lc.sousTotal) FROM LigneCommande lc WHERE lc.produit.id = :produitId")
        BigDecimal findTotalRevenueByProduitId(@Param("produitId") Long produitId);

        // Lignes de commande avec détails
        @Query("SELECT lc FROM LigneCommande lc JOIN FETCH lc.produit WHERE lc.commande.id = :commandeId")
        List<LigneCommande> findByCommandeIdWithProduit(@Param("commandeId") Long commandeId);

        // Recherche avancée
        @Query("SELECT lc FROM LigneCommande lc WHERE " +
                        "(:commandeId IS NULL OR lc.commande.id = :commandeId) AND " +
                        "(:produitId IS NULL OR lc.produit.id = :produitId) AND " +
                        "(:minQuantite IS NULL OR lc.quantite >= :minQuantite) AND " +
                        "(:maxQuantite IS NULL OR lc.quantite <= :maxQuantite)")
        List<LigneCommande> findWithFilters(
                        @Param("commandeId") Long commandeId,
                        @Param("produitId") Long produitId,
                        @Param("minQuantite") Integer minQuantite,
                        @Param("maxQuantite") Integer maxQuantite);

        // Produits fréquemment achetés ensemble (analyse du panier)
        @Query("SELECT lc2.produit, COUNT(*) as frequency " +
                        "FROM LigneCommande lc1 JOIN LigneCommande lc2 ON lc1.commande.id = lc2.commande.id " +
                        "WHERE lc1.produit.id = :produitId AND lc2.produit.id != :produitId " +
                        "GROUP BY lc2.produit " +
                        "ORDER BY frequency DESC")
        List<Object[]> findFrequentlyBoughtTogether(@Param("produitId") Long produitId);

        // Quantité moyenne par commande
        @Query("SELECT AVG(lc.quantite) FROM LigneCommande lc")
        Double findAverageQuantityPerLine();

        // Nouveaux indicateurs pour Dashboard Investisseur
        @Query("SELECT lc.produit.categorie, SUM(lc.sousTotal) FROM LigneCommande lc GROUP BY lc.produit.categorie ORDER BY SUM(lc.sousTotal) DESC")
        List<Object[]> findTotalRevenueByCategory();

        @Query("SELECT lc.produit, SUM(lc.sousTotal) FROM LigneCommande lc GROUP BY lc.produit ORDER BY SUM(lc.sousTotal) DESC")
        List<Object[]> findTopProductsByRevenue(org.springframework.data.domain.Pageable pageable);

        @Query("SELECT lc.produit.categorie, COUNT(DISTINCT lc.commande.id) FROM LigneCommande lc GROUP BY lc.produit.categorie ORDER BY COUNT(DISTINCT lc.commande.id) DESC")
        List<Object[]> findOrderCountByCategory();

        @Query("SELECT lc.produit.categorie, SUM(lc.sousTotal) FROM LigneCommande lc WHERE lc.commande.vendeur.id = :vendeurId GROUP BY lc.produit.categorie")
        List<Object[]> findTotalRevenueByCategoryByVendeur(@Param("vendeurId") Long vendeurId);

        // Revenus par catégorie entre deux dates (pour prédictions)
        @Query("SELECT lc.produit.categorie, SUM(lc.sousTotal) FROM LigneCommande lc WHERE lc.commande.dateCommande BETWEEN :start AND :end GROUP BY lc.produit.categorie ORDER BY SUM(lc.sousTotal) DESC")
        List<Object[]> findTotalRevenueByCategoryBetweenDates(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);
}