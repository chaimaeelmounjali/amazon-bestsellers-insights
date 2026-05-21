package com.example.amazonbestseller.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "statistique", indexes = {
        @Index(name = "idx_type_periode", columnList = "type_statistique, periode"),
        @Index(name = "idx_dates", columnList = "date_debut, date_fin"),
        @Index(name = "idx_date_calcul", columnList = "date_calcul")
}, uniqueConstraints = {
        @UniqueConstraint(name = "unique_stat_periode", columnNames = {"type_statistique", "periode", "date_debut", "date_fin"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Statistique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_statistique", nullable = false, length = 50)
    private String typeStatistique;

    @Column(nullable = false, length = 20)
    private String periode;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    // Statistiques produits
    @Column(name = "produits_vendus")
    private Integer produitsVendus = 0;

    @Column(name = "revenu_total", precision = 15, scale = 2)
    private BigDecimal revenuTotal = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "produit_plus_vendu_id")
    @JsonIgnore
    private Produit produitPlusVendu;

    @Column(name = "categorie_plus_vendue", length = 100)
    private String categoriePlusVendue;

    // Statistiques utilisateurs
    @Column(name = "nouveaux_utilisateurs")
    private Integer nouveauxUtilisateurs = 0;

    @Column(name = "utilisateurs_actifs")
    private Integer utilisateursActifs = 0;

    @Column(name = "acheteurs_actifs")
    private Integer acheteursActifs = 0;

    @Column(name = "vendeurs_actifs")
    private Integer vendeursActifs = 0;

    // Statistiques ventes
    @Column(name = "commandes_total")
    private Integer commandesTotal = 0;

    @Column(name = "commandes_completees")
    private Integer commandesCompletees = 0;

    @Column(name = "commandes_annulees")
    private Integer commandesAnnulees = 0;

    @Column(name = "panier_moyen", precision = 10, scale = 2)
    private BigDecimal panierMoyen = BigDecimal.ZERO;

    // Statistiques financières
    @Column(name = "commission_totale", precision = 15, scale = 2)
    private BigDecimal commissionTotale = BigDecimal.ZERO;

    @Column(name = "frais_transaction_total", precision = 15, scale = 2)
    private BigDecimal fraisTransactionTotal = BigDecimal.ZERO;

    @Column(name = "revenu_net", precision = 15, scale = 2)
    private BigDecimal revenuNet = BigDecimal.ZERO;

    // Performances
    @Column(name = "taux_conversion", precision = 5, scale = 2)
    private BigDecimal tauxConversion = BigDecimal.ZERO;

    @Column(name = "satisfaction_moyenne", precision = 3, scale = 2)
    private BigDecimal satisfactionMoyenne = BigDecimal.ZERO;

    // Stock
    @Column(name = "produits_en_rupture")
    private Integer produitsEnRupture = 0;

    @Column(name = "produits_reapprovisionnes")
    private Integer produitsReapprovisionnes = 0;

    // Métadonnées
    @Column(name = "date_calcul")
    private LocalDateTime dateCalcul = LocalDateTime.now();

    @Column(name = "est_valide")
    private Boolean estValide = true;
}
