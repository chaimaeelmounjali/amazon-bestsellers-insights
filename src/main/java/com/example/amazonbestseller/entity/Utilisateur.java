package com.example.amazonbestseller.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateur", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_role", columnList = "role"),
        @Index(name = "idx_nom_utilisateur", columnList = "nom_utilisateur")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_utilisateur", nullable = false, unique = true, length = 100)
    private String nomUtilisateur;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "date_inscription")
    private LocalDateTime dateInscription = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "est_actif")
    private Boolean estActif = true;

    // IMPORTANT: Use uppercase enum values
    public enum Role {
        ADMIN,        // Changed from Admin
        VENDEUR,      // Changed from Vendeur
        INVESTISSEUR, // Changed from Investisseur
        ACHETEUR      // Changed from Acheteur
    }
}