// File: ProduitRepository.java
package com.example.amazonbestseller.repository;

import com.example.amazonbestseller.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

        // Recherche par ASIN
        Optional<Produit> findByAsin(String asin);

        List<Produit> findByAsinIn(List<String> asins);

        // Recherche par catégorie
        List<Produit> findByCategorie(String categorie);

        List<Produit> findByCategorieIgnoreCase(String categorie);

        List<Produit> findByCategorieContaining(String categorie);

        List<Produit> findByCategorieContainingIgnoreCase(String categorie);

        List<Produit> findByCategorieIn(List<String> categories);

        // Recherche par prix
        List<Produit> findByPrixBetween(BigDecimal min, BigDecimal max);

        List<Produit> findByPrixLessThanEqual(BigDecimal maxPrice);

        List<Produit> findByPrixGreaterThanEqual(BigDecimal minPrice);

        // Recherche par note
        List<Produit> findByNoteBetween(BigDecimal min, BigDecimal max);

        List<Produit> findByNoteGreaterThanEqual(BigDecimal minNote);

        // Recherche par disponibilité
        List<Produit> findByEstDisponibleTrue();

        List<Produit> findByEstDisponibleFalse();

        // Recherche par rang
        List<Produit> findByRangBetween(int minRang, int maxRang);

        List<Produit> findByRangLessThanEqual(int maxRang);

        List<Produit> findByRangGreaterThanEqual(int minRang);

        // Recherche par nombre d'avis
        List<Produit> findByNombreAvisGreaterThanEqual(int minAvis);

        // Recherche par nom
        List<Produit> findByNomContaining(String nom);

        List<Produit> findByNomContainingIgnoreCase(String nom);

        // Recherche par magasin
        List<Produit> findByMagasinId(Long magasinId);

        List<Produit> findByMagasinIdVendeur(Long idVendeur);

        List<Produit> findByMagasinNomContaining(String magasinNom);

        // Recherche par date
        List<Produit> findByDateAjoutAfter(java.time.LocalDateTime date);

        // Méthodes de recherche avancée
        @Query("SELECT p FROM Produit p WHERE " +
                        "LOWER(p.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.categorie) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.asin) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        List<Produit> searchByKeyword(@Param("keyword") String keyword);

        @Query("SELECT p FROM Produit p WHERE " +
                        "(:categorie IS NULL OR :categorie = '' OR LOWER(p.categorie) = LOWER(:categorie)) AND " +
                        "(:minPrix IS NULL OR p.prix >= :minPrix) AND " +
                        "(:maxPrix IS NULL OR p.prix <= :maxPrix) AND " +
                        "(:minNote IS NULL OR p.note >= :minNote) AND " +
                        "(:minAvis IS NULL OR p.nombreAvis >= :minAvis) AND " +
                        "(:motCle IS NULL OR :motCle = '' OR LOWER(p.nom) LIKE LOWER(CONCAT('%', :motCle, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :motCle, '%')))")
        List<Produit> findWithFilters(
                        @Param("categorie") String categorie,
                        @Param("minPrix") BigDecimal minPrix,
                        @Param("maxPrix") BigDecimal maxPrix,
                        @Param("minNote") BigDecimal minNote,
                        @Param("minAvis") Integer minAvis,
                        @Param("motCle") String motCle);

        // Top produits
        @Query("SELECT p FROM Produit p ORDER BY p.rang ASC")
        List<Produit> findTopProduits(org.springframework.data.domain.Pageable pageable);

        @Query("SELECT p FROM Produit p WHERE p.note >= :minNote ORDER BY p.note DESC")
        List<Produit> findTopRatedProduits(
                        @Param("minNote") BigDecimal minNote,
                        org.springframework.data.domain.Pageable pageable);

        @Query("SELECT p FROM Produit p WHERE p.nombreAvis >= :minReviews ORDER BY p.nombreAvis DESC")
        List<Produit> findMostReviewedProduits(
                        @Param("minReviews") int minReviews,
                        org.springframework.data.domain.Pageable pageable);

        // Produits en rupture de stock
        @Query("SELECT p FROM Produit p WHERE p.estDisponible = false OR (p.stock IS NOT NULL AND p.stock.quantite = 0)")
        List<Produit> findOutOfStockProduits();

        // Produits avec stock faible
        @Query("SELECT p FROM Produit p WHERE p.stock IS NOT NULL AND p.stock.quantite < p.stock.seuilMin")
        List<Produit> findLowStockProduits();

        // Statistiques
        @Query("SELECT COUNT(p) FROM Produit p WHERE p.estDisponible = true")
        Long countAvailableProduits();

        @Query("SELECT COUNT(DISTINCT p.categorie) FROM Produit p")
        Long countDistinctCategories();

        @Query("SELECT AVG(p.prix) FROM Produit p WHERE p.estDisponible = true")
        BigDecimal findAveragePrice();

        @Query("SELECT AVG(p.note) FROM Produit p WHERE p.note > 0")
        BigDecimal findAverageRating();

        @Query("SELECT p.categorie, COUNT(p) as count FROM Produit p GROUP BY p.categorie ORDER BY count DESC")
        List<Object[]> findProduitCountByCategory();

        // Export des données
        @Query("SELECT p.asin, p.nom, p.categorie, p.prix, p.note, p.nombreAvis, p.rang, p.estDisponible, p.dateAjout FROM Produit p")
        List<Object[]> findAllForExport();
}