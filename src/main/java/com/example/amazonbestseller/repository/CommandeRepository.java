// File: CommandeRepository.java
package com.example.amazonbestseller.repository;

import com.example.amazonbestseller.entity.Commande;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {

    // Recherche par acheteur
    List<Commande> findByAcheteurId(Long acheteurId);
    List<Commande> findByAcheteurNomUtilisateur(String nomAcheteur);

    // Recherche par vendeur
    List<Commande> findByVendeurId(Long vendeurId);
    List<Commande> findByVendeurNomUtilisateur(String nomVendeur);

    // Recherche par statut
    List<Commande> findByStatut(Commande.Statut statut);
    List<Commande> findByStatutIn(List<Commande.Statut> statuts);

    // Recherche par date
    List<Commande> findByDateCommandeBetween(LocalDateTime start, LocalDateTime end);
    List<Commande> findByDateCommandeAfter(LocalDateTime date);
    List<Commande> findByDateCommandeBefore(LocalDateTime date);

    // Recherche par montant
    List<Commande> findByMontantTotalBetween(BigDecimal min, BigDecimal max);
    List<Commande> findByMontantTotalGreaterThanEqual(BigDecimal montant);

    // Recherche par méthode de paiement
    List<Commande> findByMethodePaiement(String methodePaiement);

    // Commandes récentes
    List<Commande> findByOrderByDateCommandeDesc();

    // Statistiques de commandes
    @Query("SELECT COUNT(c) FROM Commande c WHERE c.dateCommande BETWEEN :start AND :end")
    Long countCommandesBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT SUM(c.montantTotal) FROM Commande c WHERE c.dateCommande BETWEEN :start AND :end")
    BigDecimal findTotalRevenueBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT AVG(c.montantTotal) FROM Commande c WHERE c.dateCommande BETWEEN :start AND :end")
    BigDecimal findAverageOrderValueBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT c.statut, COUNT(c) FROM Commande c WHERE c.dateCommande BETWEEN :start AND :end GROUP BY c.statut")
    List<Object[]> findOrderStatusDistributionBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Commandes avec détails
    @Query("SELECT c FROM Commande c LEFT JOIN FETCH c.lignesCommande WHERE c.id = :id")
    Optional<Commande> findByIdWithLignes(@Param("id") Long id);

    @Query("SELECT c FROM Commande c LEFT JOIN FETCH c.ventes WHERE c.id = :id")
    Optional<Commande> findByIdWithVentes(@Param("id") Long id);

    // Recherche avancée
    @Query("SELECT c FROM Commande c WHERE " +
            "(:acheteurId IS NULL OR c.acheteur.id = :acheteurId) AND " +
            "(:vendeurId IS NULL OR c.vendeur.id = :vendeurId) AND " +
            "(:statut IS NULL OR c.statut = :statut) AND " +
            "(:minAmount IS NULL OR c.montantTotal >= :minAmount) AND " +
            "(:maxAmount IS NULL OR c.montantTotal <= :maxAmount) AND " +
            "c.dateCommande BETWEEN :startDate AND :endDate")
    List<Commande> findWithFilters(
            @Param("acheteurId") Long acheteurId,
            @Param("vendeurId") Long vendeurId,
            @Param("statut") Commande.Statut statut,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Top acheteurs
    @Query("SELECT c.acheteur, SUM(c.montantTotal) as totalSpent, COUNT(c) as orderCount " +
            "FROM Commande c " +
            "WHERE c.dateCommande BETWEEN :start AND :end " +
            "GROUP BY c.acheteur " +
            "ORDER BY totalSpent DESC")
    List<Object[]> findTopBuyersBetweenDates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Commandes en attente
    @Query("SELECT c FROM Commande c WHERE c.statut = 'EN_ATTENTE' ORDER BY c.dateCommande ASC")
    List<Commande> findPendingOrders();

    // Commandes à expédier
    @Query("SELECT c FROM Commande c WHERE c.statut = 'CONFIRMEE' ORDER BY c.dateCommande ASC")
    List<Commande> findOrdersToShip();

    // Commandes récentes pour dashboard
    @Query("SELECT c FROM Commande c ORDER BY c.dateCommande DESC LIMIT :limit")
    List<Commande> findRecentOrders(@Param("limit") int limit);
}