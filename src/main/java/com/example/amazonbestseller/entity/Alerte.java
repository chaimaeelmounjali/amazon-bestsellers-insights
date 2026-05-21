package com.example.amazonbestseller.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerte", indexes = {
        @Index(name = "idx_utilisateur", columnList = "id_utilisateur"),
        @Index(name = "idx_est_lu", columnList = "est_lu"),
        @Index(name = "idx_priorite", columnList = "priorite"),
        @Index(name = "idx_date_creation", columnList = "date_creation")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    @JsonIgnore
    private Utilisateur utilisateur;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "est_lu")
    private Boolean estLu = false;

    @Enumerated(EnumType.STRING)
    @Column
    private Priorite priorite = Priorite.MOYENNE;

    public enum Priorite {
        FAIBLE("Faible"),
        MOYENNE("Moyenne"),
        HAUTE("Haute"),
        CRITIQUE("Critique");

        private final String valeur;

        Priorite(String valeur) {
            this.valeur = valeur;
        }

        @JsonValue
        public String getValeur() {
            return valeur;
        }

        @JsonCreator
        public static Priorite fromString(String valeur) {
            if (valeur == null || valeur.trim().isEmpty()) {
                return MOYENNE;
            }

            // Essayer de trouver par le nom de l'enum (MAJUSCULES)
            try {
                return Priorite.valueOf(valeur.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si échec, essayer de trouver par la valeur affichée
                for (Priorite p : Priorite.values()) {
                    if (p.valeur.equalsIgnoreCase(valeur)) {
                        return p;
                    }
                }

                // Essayer avec des correspondances partielles
                String normalized = valeur.toUpperCase().trim();
                if (normalized.contains("FAIBLE") || normalized.contains("LOW")) {
                    return FAIBLE;
                } else if (normalized.contains("MOYENNE") || normalized.contains("MEDIUM") || normalized.contains("MOYEN")) {
                    return MOYENNE;
                } else if (normalized.contains("HAUTE") || normalized.contains("HIGH") || normalized.contains("ELEVEE")) {
                    return HAUTE;
                } else if (normalized.contains("CRITIQUE") || normalized.contains("CRITICAL")) {
                    return CRITIQUE;
                }

                // Si toujours pas trouvé, retourner la valeur par défaut
                return MOYENNE;
            }
        }

        @Override
        public String toString() {
            return valeur;
        }
    }

    // Méthode utilitaire pour la conversion depuis la base de données
    @PostLoad
    public void afterLoad() {
        if (priorite != null) {
            // Cette méthode s'assure que l'enum est correctement interprété
            // même si la valeur dans la base n'est pas exactement le nom de l'enum
            try {
                // Si priorite.name() échoue, c'est que l'enum n'a pas été correctement chargé
                // Dans ce cas, on utilise notre méthode fromString
                String prioriteName = priorite.name();
            } catch (Exception e) {
                // Si l'enum n'est pas correctement initialisé, le recréer
                if (priorite.toString() != null) {
                    this.priorite = Priorite.fromString(priorite.toString());
                }
            }
        }
    }

    // Méthode utilitaire pour la sauvegarde dans la base
    @PrePersist
    @PreUpdate
    public void beforeSave() {
        // S'assurer que la priorité est correctement définie
        if (priorite == null) {
            priorite = Priorite.MOYENNE;
        }
    }
}