// File: AdminRepository.java
package com.example.amazonbestseller.repository;

import com.example.amazonbestseller.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // Authentification et recherche
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByNomUtilisateur(String nomUtilisateur);
    Optional<Admin> findByEmailAndMotDePasse(String email, String motDePasse);

    // Recherche par permissions
    List<Admin> findByPermissionsContaining(String permission);

    // Recherche par statut et date
    List<Admin> findByEstActifTrue();
    List<Admin> findByDerniereConnexionAfter(LocalDateTime date);
    List<Admin> findByDerniereConnexionBetween(LocalDateTime start, LocalDateTime end);

    // Statistiques admin
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.estActif = true")
    Long countActiveAdmins();

    @Query("SELECT a FROM Admin a WHERE a.derniereConnexion IS NOT NULL ORDER BY a.derniereConnexion DESC")
    List<Admin> findRecentActiveAdmins();

    // Recherche avancée
    @Query("SELECT a FROM Admin a WHERE LOWER(a.nomUtilisateur) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Admin> searchByKeyword(@Param("keyword") String keyword);
}