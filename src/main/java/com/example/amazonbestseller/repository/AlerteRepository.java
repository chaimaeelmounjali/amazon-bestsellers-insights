// File: AlerteRepository.java
package com.example.amazonbestseller.repository;
import java.util.Optional;
import com.example.amazonbestseller.entity.Alerte;
import com.example.amazonbestseller.entity.Alerte.Priorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    // Recherche par utilisateur
    List<Alerte> findByUtilisateurId(Long utilisateurId);
    List<Alerte> findByUtilisateurNomUtilisateur(String nomUtilisateur);

    // Recherche par type
    List<Alerte> findByType(String type);
    List<Alerte> findByTypeContaining(String type);

    // Recherche par priorité
    List<Alerte> findByPriorite(Priorite priorite);
    List<Alerte> findByPrioriteIn(List<Priorite> priorites);

    // Recherche par statut de lecture
    List<Alerte> findByEstLuTrue();
    List<Alerte> findByEstLuFalse();

    // Recherche par date
    List<Alerte> findByDateCreationAfter(LocalDateTime date);
    List<Alerte> findByDateCreationBetween(LocalDateTime start, LocalDateTime end);

    // Alertes non lues par utilisateur
    List<Alerte> findByUtilisateurIdAndEstLuFalse(Long utilisateurId);

    // Alertes critiques/hautes priorités
    List<Alerte> findByPrioriteInAndEstLuFalse(List<Priorite> priorites);

    // Recherche avancée
    @Query("SELECT a FROM Alerte a WHERE " +
            "(:utilisateurId IS NULL OR a.utilisateur.id = :utilisateurId) AND " +
            "(:type IS NULL OR a.type = :type) AND " +
            "(:priorite IS NULL OR a.priorite = :priorite) AND " +
            "(:estLu IS NULL OR a.estLu = :estLu) AND " +
            "a.dateCreation BETWEEN :startDate AND :endDate")
    List<Alerte> findWithFilters(
            @Param("utilisateurId") Long utilisateurId,
            @Param("type") String type,
            @Param("priorite") Priorite priorite,
            @Param("estLu") Boolean estLu,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Alertes récentes non lues
    @Query("SELECT a FROM Alerte a WHERE a.estLu = false ORDER BY a.dateCreation DESC LIMIT :limit")
    List<Alerte> findRecentUnreadAlertes(@Param("limit") int limit);

    // Alertes par priorité pour dashboard
    @Query("SELECT a.priorite, COUNT(a) FROM Alerte a WHERE a.estLu = false GROUP BY a.priorite")
    List<Object[]> countUnreadAlertesByPriority();

    // Statistiques d'alertes
    @Query("SELECT COUNT(a) FROM Alerte a WHERE a.estLu = false")
    Long countUnreadAlertes();

    @Query("SELECT COUNT(a) FROM Alerte a WHERE a.estLu = false AND a.priorite = 'CRITIQUE'")
    Long countCriticalUnreadAlertes();

    // Alertes avec utilisateur
    @Query("SELECT a FROM Alerte a JOIN FETCH a.utilisateur WHERE a.id = :id")
    Optional<Alerte> findByIdWithUtilisateur(@Param("id") Long id);

    // Types d'alertes les plus fréquents
    @Query("SELECT a.type, COUNT(a) FROM Alerte a GROUP BY a.type ORDER BY COUNT(a) DESC")
    List<Object[]> findMostFrequentAlerteTypes();

    // Marquer comme lu
    @Query("UPDATE Alerte a SET a.estLu = true WHERE a.id = :id")
    void markAsRead(@Param("id") Long id);

    // Marquer plusieurs comme lu
    @Query("UPDATE Alerte a SET a.estLu = true WHERE a.id IN :ids")
    void markMultipleAsRead(@Param("ids") List<Long> ids);

    // Supprimer les anciennes alertes
    @Query("DELETE FROM Alerte a WHERE a.dateCreation < :date AND a.estLu = true")
    void deleteOldReadAlertes(@Param("date") LocalDateTime date);
}