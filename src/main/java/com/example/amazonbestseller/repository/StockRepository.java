// File: StockRepository.java
package com.example.amazonbestseller.repository;

import com.example.amazonbestseller.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    // Recherche par produit
    Optional<Stock> findByProduitId(Long produitId);
    Optional<Stock> findByProduitAsin(String asin);

    // Recherche par quantité
    List<Stock> findByQuantiteLessThan(Integer quantite);
    List<Stock> findByQuantiteGreaterThan(Integer quantite);
    List<Stock> findByQuantiteBetween(Integer min, Integer max);

    // Recherche par seuil
    // Recherche les stocks dont la quantité est inférieure au seuil minimum
    @Query("SELECT s FROM Stock s WHERE s.quantite < s.seuilMin")
    List<Stock> findStocksWithLowQuantity();

    // Stock faible (quantité < seuil minimum)
    @Query("SELECT s FROM Stock s WHERE s.quantite < s.seuilMin")
    List<Stock> findLowStock();

    // Stock épuisé
    @Query("SELECT s FROM Stock s WHERE s.quantite = 0")
    List<Stock> findOutOfStock();

    // Stock à réapprovisionner
    @Query("SELECT s FROM Stock s WHERE s.quantite <= s.seuilMin")
    List<Stock> findStockToReplenish();

    // Recherche par emplacement
    List<Stock> findByEmplacement(String emplacement);
    List<Stock> findByEmplacementContaining(String emplacement);

    // Recherche par date de dernière mise à jour
    List<Stock> findByDerniereMajAfter(LocalDateTime date);
    List<Stock> findByDerniereMajBetween(LocalDateTime start, LocalDateTime end);

    // Statistiques de stock
    @Query("SELECT SUM(s.quantite) FROM Stock s")
    Long findTotalStockQuantity();

    @Query("SELECT AVG(s.quantite) FROM Stock s")
    Double findAverageStockQuantity();

    @Query("SELECT COUNT(s) FROM Stock s WHERE s.quantite = 0")
    Long countOutOfStock();

    @Query("SELECT COUNT(s) FROM Stock s WHERE s.quantite < s.seuilMin")
    Long countLowStock();

    // Produits nécessitant réapprovisionnement urgent
    @Query("SELECT s FROM Stock s WHERE s.quantite < (s.seuilMin * 0.5)")
    List<Stock> findUrgentReplenishmentNeeded();

    // Recherche avec produit
    @Query("SELECT s FROM Stock s JOIN FETCH s.produit p WHERE p.id = :produitId")
    Optional<Stock> findByProduitIdWithProduit(@Param("produitId") Long produitId);

    @Query("SELECT s FROM Stock s JOIN FETCH s.produit p WHERE p.estDisponible = true")
    List<Stock> findAllWithAvailableProduits();
}