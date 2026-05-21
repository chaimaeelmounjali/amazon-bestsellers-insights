// File: InvestisseurRepository.java
package com.example.amazonbestseller.repository;

import com.example.amazonbestseller.entity.Investisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvestisseurRepository extends JpaRepository<Investisseur, Long> {

    // Recherche de base
    Optional<Investisseur> findByEmail(String email);
    Optional<Investisseur> findByNomUtilisateur(String nomUtilisateur);
    Optional<Investisseur> findByEmailAndMotDePasse(String email, String motDePasse);

    // Recherche par montant d'investissement
    List<Investisseur> findByMontantInvestissementGreaterThanEqual(BigDecimal montant);
    List<Investisseur> findByMontantInvestissementBetween(BigDecimal min, BigDecimal max);

    // Recherche par ROI
    List<Investisseur> findByRoiGreaterThanEqual(BigDecimal minRoi);
    List<Investisseur> findByRoiBetween(BigDecimal min, BigDecimal max);

    // Recherche par statut
    List<Investisseur> findByEstActifTrue();
    List<Investisseur> findByEstActifFalse();

    // Top investisseurs
    @Query("SELECT i FROM Investisseur i ORDER BY i.montantInvestissement DESC")
    List<Investisseur> findTopInvestisseurs();

    @Query("SELECT i FROM Investisseur i ORDER BY i.roi DESC")
    List<Investisseur> findTopInvestisseursByRoi();

    // Statistiques investisseurs
    @Query("SELECT COUNT(i) FROM Investisseur i WHERE i.estActif = true")
    Long countActiveInvestisseurs();

    @Query("SELECT SUM(i.montantInvestissement) FROM Investisseur i WHERE i.estActif = true")
    BigDecimal findTotalInvestment();

    @Query("SELECT AVG(i.roi) FROM Investisseur i WHERE i.roi > 0")
    BigDecimal findAverageRoi();

    // Recherche avancée
    @Query("SELECT i FROM Investisseur i WHERE " +
            "LOWER(i.nomUtilisateur) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Investisseur> searchByKeyword(@Param("keyword") String keyword);

    // Investisseurs avec ROI élevé
    @Query("SELECT i FROM Investisseur i WHERE i.roi >= :minRoi ORDER BY i.roi DESC")
    List<Investisseur> findHighRoiInvestisseurs(@Param("minRoi") BigDecimal minRoi);

    // Limite de résultats
    @Query("SELECT i FROM Investisseur i WHERE i.estActif = true ORDER BY i.montantInvestissement DESC LIMIT :limit")
    List<Investisseur> findTopInvestisseursByLimit(@Param("limit") int limit);
}