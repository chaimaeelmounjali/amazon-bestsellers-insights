// File: AvisRepository.java
package com.example.amazonbestseller.repository;

import java.util.Optional;
import com.example.amazonbestseller.entity.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {

        // Recherche par produit
        List<Avis> findByProduitId(Long produitId);

        List<Avis> findByProduitAsin(String asin);

        // Recherche par acheteur
        List<Avis> findByAcheteurId(Long acheteurId);

        List<Avis> findByAcheteurNomUtilisateur(String nomAcheteur);

        Optional<Avis> findByProduitIdAndAcheteurId(Long produitId, Long acheteurId);

        // Recherche par note
        List<Avis> findByNote(BigDecimal note);

        List<Avis> findByNoteGreaterThanEqual(BigDecimal minNote);

        List<Avis> findByNoteBetween(BigDecimal min, BigDecimal max);

        // Recherche par date
        List<Avis> findByDateAvisAfter(LocalDateTime date);

        List<Avis> findByDateAvisBetween(LocalDateTime start, LocalDateTime end);

        // Recherche par vérification
        List<Avis> findByEstVerifieTrue();

        List<Avis> findByEstVerifieFalse();

        // Recherche par utilité
        List<Avis> findByNombreUtilesGreaterThanEqual(Integer minUtiles);

        List<Avis> findByOrderByNombreUtilesDesc();

        // Avis avec commentaire
        List<Avis> findByCommentaireIsNotNull();

        List<Avis> findByCommentaireContaining(String texte);

        // Statistiques d'avis
        @Query("SELECT AVG(a.note) FROM Avis a WHERE a.produit.id = :produitId")
        BigDecimal findAverageRatingByProduitId(@Param("produitId") Long produitId);

        @Query("SELECT COUNT(a) FROM Avis a WHERE a.produit.id = :produitId")
        Long countAvisByProduitId(@Param("produitId") Long produitId);

        @Query("SELECT a.note, COUNT(a) FROM Avis a WHERE a.produit.id = :produitId GROUP BY a.note")
        List<Object[]> findRatingDistributionByProduitId(@Param("produitId") Long produitId);

        // Avis vérifiés par produit
        @Query("SELECT COUNT(a) FROM Avis a WHERE a.produit.id = :produitId AND a.estVerifie = true")
        Long countVerifiedReviewsByProduitId(@Param("produitId") Long produitId);

        // Derniers avis
        @Query("SELECT a FROM Avis a ORDER BY a.dateAvis DESC LIMIT :limit")
        List<Avis> findRecentAvis(@Param("limit") int limit);

        // Avis les plus utiles
        @Query("SELECT a FROM Avis a WHERE a.nombreUtiles > 0 ORDER BY a.nombreUtiles DESC, a.dateAvis DESC LIMIT :limit")
        List<Avis> findMostHelpfulAvis(@Param("limit") int limit);

        // Recherche avancée
        @Query("SELECT a FROM Avis a WHERE " +
                        "(:produitId IS NULL OR a.produit.id = :produitId) AND " +
                        "(:acheteurId IS NULL OR a.acheteur.id = :acheteurId) AND " +
                        "(:minNote IS NULL OR a.note >= :minNote) AND " +
                        "(:maxNote IS NULL OR a.note <= :maxNote) AND " +
                        "(:estVerifie IS NULL OR a.estVerifie = :estVerifie) AND " +
                        "a.dateAvis BETWEEN :startDate AND :endDate")
        List<Avis> findWithFilters(
                        @Param("produitId") Long produitId,
                        @Param("acheteurId") Long acheteurId,
                        @Param("minNote") BigDecimal minNote,
                        @Param("maxNote") BigDecimal maxNote,
                        @Param("estVerifie") Boolean estVerifie,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Avis avec produit et acheteur
        @Query("SELECT a FROM Avis a JOIN FETCH a.produit JOIN FETCH a.acheteur WHERE a.id = :id")
        Optional<Avis> findByIdWithDetails(@Param("id") Long id);

        // Top produits par note moyenne
        @Query("SELECT a.produit, AVG(a.note) as avgRating, COUNT(a) as reviewCount " +
                        "FROM Avis a " +
                        "GROUP BY a.produit " +
                        "HAVING COUNT(a) >= :minReviews " +
                        "ORDER BY avgRating DESC")
        List<Object[]> findTopRatedProducts(
                        @Param("minReviews") int minReviews);
}