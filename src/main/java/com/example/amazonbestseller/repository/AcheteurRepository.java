// File: AcheteurRepository.java
package com.example.amazonbestseller.repository;

import com.example.amazonbestseller.entity.Acheteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AcheteurRepository extends JpaRepository<Acheteur, Long> {

    // Authentification et recherche de base
    Optional<Acheteur> findByEmail(String email);
    Optional<Acheteur> findByNomUtilisateur(String nomUtilisateur);
    Optional<Acheteur> findByEmailAndMotDePasse(String email, String motDePasse);

    // Recherche par localisation
    List<Acheteur> findByAdresseLivraisonContaining(String adresse);

    // Recherche par téléphone
    List<Acheteur> findByNumeroTelephone(String numeroTelephone);
    List<Acheteur> findByNumeroTelephoneContaining(String telephonePart);

    // Recherche par statut
    List<Acheteur> findByEstActifTrue();
    List<Acheteur> findByEstActifFalse();

    // Recherche par date d'inscription
    List<Acheteur> findByDateInscriptionAfter(LocalDateTime date);
    List<Acheteur> findByDateInscriptionBetween(LocalDateTime start, LocalDateTime end);

    // Recherche avancée avec commandes
    @Query("SELECT a FROM Acheteur a LEFT JOIN FETCH a.commandes WHERE a.id = :id")
    Optional<Acheteur> findByIdWithCommandes(@Param("id") Long id);

    @Query("SELECT a FROM Acheteur a LEFT JOIN FETCH a.avis WHERE a.id = :id")
    Optional<Acheteur> findByIdWithAvis(@Param("id") Long id);

    @Query("SELECT DISTINCT a FROM Acheteur a LEFT JOIN FETCH a.commandes c WHERE c IS NOT NULL")
    List<Acheteur> findAcheteursWithCommandes();

    // Statistiques acheteurs
    @Query("SELECT COUNT(a) FROM Acheteur a WHERE a.estActif = true")
    Long countActiveAcheteurs();

    @Query("SELECT a FROM Acheteur a WHERE SIZE(a.commandes) > 0 ORDER BY SIZE(a.commandes) DESC")
    List<Acheteur> findTopAcheteursByCommandes();

    @Query("SELECT a FROM Acheteur a WHERE SIZE(a.commandes) = 0")
    List<Acheteur> findAcheteursWithoutCommandes();

    @Query("SELECT a FROM Acheteur a WHERE " +
            "(SELECT COUNT(c) FROM a.commandes c WHERE c.dateCommande > :sinceDate) > :minCommandes")
    List<Acheteur> findActiveAcheteursSince(
            @Param("sinceDate") LocalDateTime sinceDate,
            @Param("minCommandes") int minCommandes
    );

    // Recherche par mot-clé
    @Query("SELECT a FROM Acheteur a WHERE " +
            "LOWER(a.nomUtilisateur) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.adresseLivraison) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.numeroTelephone) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Acheteur> searchByKeyword(@Param("keyword") String keyword);

    // Acheteurs récents
    @Query("SELECT a FROM Acheteur a ORDER BY a.dateInscription DESC LIMIT :limit")
    List<Acheteur> findRecentAcheteurs(@Param("limit") int limit);
}