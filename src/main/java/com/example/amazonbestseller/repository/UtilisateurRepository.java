// File: UtilisateurRepository.java
package com.example.amazonbestseller.repository;

import com.example.amazonbestseller.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    // Recherche de base
    Optional<Utilisateur> findByEmail(String email);
    Optional<Utilisateur> findByNomUtilisateur(String nomUtilisateur);
    Optional<Utilisateur> findByEmailAndMotDePasse(String email, String motDePasse);

    // Recherche par rôle
    List<Utilisateur> findByRole(Utilisateur.Role role);
    List<Utilisateur> findByRoleIn(List<Utilisateur.Role> roles);

    // Recherche par statut
    List<Utilisateur> findByEstActifTrue();
    List<Utilisateur> findByEstActifFalse();

    // Recherche par date d'inscription
    List<Utilisateur> findByDateInscriptionAfter(LocalDateTime date);
    List<Utilisateur> findByDateInscriptionBetween(LocalDateTime start, LocalDateTime end);

    // Recherche avancée
    @Query("SELECT u FROM Utilisateur u WHERE " +
            "LOWER(u.nomUtilisateur) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Utilisateur> searchByKeyword(@Param("keyword") String keyword);

    // Statistiques utilisateurs
    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.estActif = true")
    Long countActiveUsers();

    @Query("SELECT u.role, COUNT(u) FROM Utilisateur u GROUP BY u.role")
    List<Object[]> countUsersByRole();

    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.dateInscription >= :date")
    Long countNewUsersSince(@Param("date") LocalDateTime date);

    // Derniers utilisateurs inscrits
    @Query("SELECT u FROM Utilisateur u ORDER BY u.dateInscription DESC LIMIT :limit")
    List<Utilisateur> findRecentUsers(@Param("limit") int limit);
}