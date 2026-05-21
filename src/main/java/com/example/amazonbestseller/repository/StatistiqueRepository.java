// File: StatistiqueRepository.java
package com.example.amazonbestseller.repository;

import com.example.amazonbestseller.entity.Statistique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatistiqueRepository extends JpaRepository<Statistique, Long> {

    // Recherche par type et période
    List<Statistique> findByTypeStatistique(String typeStatistique);
    List<Statistique> findByPeriode(String periode);
    List<Statistique> findByTypeStatistiqueAndPeriode(String typeStatistique, String periode);

    // Recherche par date
    List<Statistique> findByDateDebutBetween(LocalDate start, LocalDate end);
    List<Statistique> findByDateFin(LocalDate dateFin);

    // Recherche par validité
    List<Statistique> findByEstValideTrue();
    List<Statistique> findByEstValideFalse();

    // Dernières statistiques
    List<Statistique> findByDateCalculAfter(java.time.LocalDateTime date);
    List<Statistique> findByOrderByDateCalculDesc();

    // Statistiques par produit le plus vendu
    List<Statistique> findByProduitPlusVenduId(Long produitId);

    // Statistiques par catégorie
    List<Statistique> findByCategoriePlusVendue(String categorie);

    // Méthodes de recherche avancée
    @Query("SELECT s FROM Statistique s WHERE " +
            "s.dateDebut <= :date AND s.dateFin >= :date AND " +
            "s.typeStatistique = :type AND s.estValide = true")
    Optional<Statistique> findValidStatistiqueForDate(
            @Param("type") String type,
            @Param("date") LocalDate date
    );

    @Query("SELECT s FROM Statistique s WHERE " +
            "s.dateDebut >= :startDate AND s.dateFin <= :endDate AND " +
            "s.typeStatistique = :type AND s.estValide = true " +
            "ORDER BY s.dateCalcul DESC")
    List<Statistique> findValidStatisticsBetweenDates(
            @Param("type") String type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Dashboard statistics
    @Query("SELECT s FROM Statistique s WHERE " +
            "s.typeStatistique = 'DASHBOARD' AND s.estValide = true " +
            "ORDER BY s.dateCalcul DESC LIMIT 1")
    Optional<Statistique> findLatestDashboardStatistics();

    @Query("SELECT s FROM Statistique s WHERE " +
            "s.typeStatistique = :type AND s.periode = :periode AND " +
            "s.dateDebut = :startDate AND s.dateFin = :endDate")
    Optional<Statistique> findByTypePeriodAndDates(
            @Param("type") String type,
            @Param("periode") String periode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Top categories over time
    @Query("SELECT s.categoriePlusVendue, COUNT(s) as occurrence " +
            "FROM Statistique s " +
            "WHERE s.categoriePlusVendue IS NOT NULL AND s.estValide = true " +
            "GROUP BY s.categoriePlusVendue " +
            "ORDER BY occurrence DESC")
    List<Object[]> findMostFrequentTopCategories();

    // Revenue trends
    @Query("SELECT s.periode, s.dateDebut, s.dateFin, s.revenuTotal " +
            "FROM Statistique s " +
            "WHERE s.typeStatistique = 'FINANCIER' AND s.estValide = true " +
            "ORDER BY s.dateDebut")
    List<Object[]> findRevenueTrends();

    // Sales performance
    @Query("SELECT s.periode, s.dateDebut, s.dateFin, " +
            "s.produitsVendus, s.revenuTotal, s.panierMoyen, s.tauxConversion " +
            "FROM Statistique s " +
            "WHERE s.typeStatistique = 'VENTES' AND s.estValide = true " +
            "ORDER BY s.dateCalcul DESC")
    List<Object[]> findSalesPerformance();

    // User growth
    @Query("SELECT s.periode, s.dateDebut, s.dateFin, " +
            "s.nouveauxUtilisateurs, s.utilisateursActifs, s.acheteursActifs, s.vendeursActifs " +
            "FROM Statistique s " +
            "WHERE s.typeStatistique = 'UTILISATEURS' AND s.estValide = true " +
            "ORDER BY s.dateDebut")
    List<Object[]> findUserGrowthStats();
}