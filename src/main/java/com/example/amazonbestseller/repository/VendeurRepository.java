// File: VendeurRepository.java
package com.example.amazonbestseller.repository;

import com.example.amazonbestseller.entity.Vendeur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendeurRepository extends JpaRepository<Vendeur, Long> {

    // Authentification et recherche de base
    Optional<Vendeur> findByEmail(String email);
    Optional<Vendeur> findByNomUtilisateur(String nomUtilisateur);
    Optional<Vendeur> findByEmailAndMotDePasse(String email, String motDePasse);

    // Recherche par magasin
    List<Vendeur> findByMagasinId(Long magasinId);
    List<Vendeur> findByMagasinNomContaining(String nomMagasin);

    // Recherche par performance
    List<Vendeur> findByVentesTotalesGreaterThan(BigDecimal montant);
    List<Vendeur> findByCommissionGreaterThan(BigDecimal commission);
    List<Vendeur> findByObjectifVentesLessThan(BigDecimal objectif);

    // Recherche par statut
    List<Vendeur> findByEstActifTrue();
    List<Vendeur> findByEstActifFalse();

    // Statistiques vendeurs
    @Query("SELECT v FROM Vendeur v ORDER BY v.ventesTotales DESC")
    List<Vendeur> findTopVendeurs();

    @Query("SELECT COUNT(v) FROM Vendeur v WHERE v.estActif = true AND v.ventesTotales > 0")
    Long countActiveSellingVendeurs();

    @Query("SELECT AVG(v.commission) FROM Vendeur v WHERE v.estActif = true")
    BigDecimal findAverageCommission();

    @Query("SELECT SUM(v.ventesTotales) FROM Vendeur v WHERE v.estActif = true")
    BigDecimal findTotalSales();

    // Recherche avancée
    @Query("SELECT v FROM Vendeur v WHERE " +
            "LOWER(v.nomUtilisateur) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(v.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "(v.magasin IS NOT NULL AND LOWER(v.magasin.nom) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Vendeur> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT v FROM Vendeur v WHERE v.ventesTotales >= :objectifVentes")
    List<Vendeur> findVendeursMeetingSalesTarget(@Param("objectifVentes") BigDecimal objectifVentes);

    // Top vendeurs avec limite
    @Query("SELECT v FROM Vendeur v WHERE v.estActif = true ORDER BY v.ventesTotales DESC LIMIT :limit")
    List<Vendeur> findTopVendeursByLimit(@Param("limit") int limit);
}