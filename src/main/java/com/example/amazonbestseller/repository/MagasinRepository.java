// File: MagasinRepository.java
package com.example.amazonbestseller.repository;

import com.example.amazonbestseller.entity.Magasin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MagasinRepository extends JpaRepository<Magasin, Long> {

    // Recherche par nom
    Optional<Magasin> findByNom(String nom);
    List<Magasin> findByNomContaining(String nom);

    // Recherche par email
    Optional<Magasin> findByEmail(String email);

    // Recherche par téléphone
    Optional<Magasin> findByNumeroTelephone(String telephone);

    // Recherche par adresse
    List<Magasin> findByAdresseContaining(String adresse);

    // Recherche par vendeur
    List<Magasin> findByIdVendeur(Long idVendeur);

    // Recherche par note
    List<Magasin> findByNoteGreaterThanEqual(BigDecimal minNote);
    List<Magasin> findByNoteBetween(BigDecimal min, BigDecimal max);

    // Magasins avec produits
    @Query("SELECT DISTINCT m FROM Magasin m LEFT JOIN FETCH m.produits WHERE m.id = :id")
    Optional<Magasin> findByIdWithProduits(@Param("id") Long id);

    @Query("SELECT DISTINCT m FROM Magasin m LEFT JOIN FETCH m.vendeurs WHERE m.id = :id")
    Optional<Magasin> findByIdWithVendeurs(@Param("id") Long id);

    // Recherche avancée
    @Query("SELECT m FROM Magasin m WHERE " +
            "LOWER(m.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.adresse) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(m.numeroTelephone) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Magasin> searchByKeyword(@Param("keyword") String keyword);

    // Top magasins par note
    @Query("SELECT m FROM Magasin m WHERE m.note IS NOT NULL ORDER BY m.note DESC LIMIT :limit")
    List<Magasin> findTopRatedMagasins(@Param("limit") int limit);

    // Magasins avec plus de produits
    @Query("SELECT m, COUNT(p) as productCount FROM Magasin m LEFT JOIN m.produits p GROUP BY m ORDER BY productCount DESC")
    List<Object[]> findMagasinsByProductCount();

    // Statistiques
    @Query("SELECT AVG(m.note) FROM Magasin m WHERE m.note > 0")
    BigDecimal findAverageRating();

    @Query("SELECT COUNT(DISTINCT m.idVendeur) FROM Magasin m")
    Long countUniqueVendeurs();
}