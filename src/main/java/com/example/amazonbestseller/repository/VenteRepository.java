// File: VenteRepository.java
package com.example.amazonbestseller.repository;

import com.example.amazonbestseller.entity.Vente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VenteRepository extends JpaRepository<Vente, Long> {

    // Recherche par commande
    List<Vente> findByCommandeId(Long commandeId);

    // Recherche par produit
    List<Vente> findByProduitId(Long produitId);
    List<Vente> findByProduitAsin(String asin);

    // Recherche par vendeur
    List<Vente> findByVendeurId(Long vendeurId);
    List<Vente> findByVendeurNomUtilisateur(String nomVendeur);

    // Recherche par acheteur
    List<Vente> findByAcheteurId(Long acheteurId);
    List<Vente> findByAcheteurNomUtilisateur(String nomAcheteur);

    // Recherche par statut
    List<Vente> findByStatut(Vente.Statut statut);
    List<Vente> findByStatutIn(List<Vente.Statut> statuts);

    // Recherche par date
    List<Vente> findByDateVenteBetween(LocalDateTime start, LocalDateTime end);
    List<Vente> findByDateVenteAfter(LocalDateTime date);
    List<Vente> findByDateVenteBefore(LocalDateTime date);

    // Recherche par montant
    List<Vente> findByMontantTotalGreaterThanEqual(BigDecimal montant);
    List<Vente> findByMontantTotalBetween(BigDecimal min, BigDecimal max);

    // Recherche par quantité
    List<Vente> findByQuantiteGreaterThanEqual(Integer quantite);

    // Statistiques de ventes
    @Query("SELECT SUM(v.montantTotal) FROM Vente v WHERE v.dateVente BETWEEN :start AND :end")
    BigDecimal findTotalSalesBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(v) FROM Vente v WHERE v.dateVente BETWEEN :start AND :end")
    Long countSalesBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT AVG(v.montantTotal) FROM Vente v WHERE v.dateVente BETWEEN :start AND :end")
    BigDecimal findAverageSaleAmountBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Ventes par catégorie
    @Query("SELECT p.categorie, SUM(v.montantTotal) as total, COUNT(v) as count " +
            "FROM Vente v JOIN v.produit p " +
            "WHERE v.dateVente BETWEEN :start AND :end " +
            "GROUP BY p.categorie " +
            "ORDER BY total DESC")
    List<Object[]> findSalesByCategoryBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Top produits vendus
    @Query("SELECT p, SUM(v.quantite) as totalQuantity, SUM(v.montantTotal) as totalAmount " +
            "FROM Vente v JOIN v.produit p " +
            "WHERE v.dateVente BETWEEN :start AND :end " +
            "GROUP BY p " +
            "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProductsBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Top vendeurs
    @Query("SELECT v.vendeur, SUM(v.montantTotal) as totalSales, COUNT(v) as saleCount " +
            "FROM Vente v " +
            "WHERE v.vendeur IS NOT NULL AND v.dateVente BETWEEN :start AND :end " +
            "GROUP BY v.vendeur " +
            "ORDER BY totalSales DESC")
    List<Object[]> findTopSellersBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Analyse temporelle
    @Query("SELECT FUNCTION('DATE', v.dateVente), SUM(v.montantTotal), COUNT(v) " +
            "FROM Vente v " +
            "WHERE v.dateVente BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', v.dateVente) " +
            "ORDER BY FUNCTION('DATE', v.dateVente)")
    List<Object[]> findDailySalesBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Recherche avancée avec filtres multiples
    @Query("SELECT v FROM Vente v WHERE " +
            "(:produitId IS NULL OR v.produit.id = :produitId) AND " +
            "(:vendeurId IS NULL OR v.vendeur.id = :vendeurId) AND " +
            "(:acheteurId IS NULL OR v.acheteur.id = :acheteurId) AND " +
            "(:statut IS NULL OR v.statut = :statut) AND " +
            "v.dateVente BETWEEN :startDate AND :endDate")
    List<Vente> findWithFilters(
            @Param("produitId") Long produitId,
            @Param("vendeurId") Long vendeurId,
            @Param("acheteurId") Long acheteurId,
            @Param("statut") Vente.Statut statut,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Ventes annulées/remboursées
    @Query("SELECT v FROM Vente v WHERE v.statut IN (:cancelled, :refunded)")
    List<Vente> findCancelledAndRefundedSales(
            @Param("cancelled") Vente.Statut cancelled,
            @Param("refunded") Vente.Statut refunded
    );

    // Performance des vendeurs
    @Query("SELECT v.vendeur, " +
            "SUM(v.montantTotal) as totalSales, " +
            "SUM(v.commission) as totalCommission, " +
            "COUNT(v) as saleCount, " +
            "AVG(v.montantTotal) as averageSale " +
            "FROM Vente v " +
            "WHERE v.vendeur IS NOT NULL AND v.dateVente BETWEEN :start AND :end " +
            "GROUP BY v.vendeur")
    List<Object[]> findSellerPerformanceBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}