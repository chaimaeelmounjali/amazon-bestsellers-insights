package com.example.amazonbestseller.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vente", indexes = {
        @Index(name = "idx_commande", columnList = "id_commande"),
        @Index(name = "idx_produit", columnList = "id_produit"),
        @Index(name = "idx_vendeur", columnList = "id_vendeur"),
        @Index(name = "idx_acheteur", columnList = "id_acheteur"),
        @Index(name = "idx_date_vente", columnList = "date_vente"),
        @Index(name = "idx_statut", columnList = "statut"),
        @Index(name = "idx_montant_total", columnList = "montant_total")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_commande", nullable = false)
    @JsonIgnore
    private Commande commande;

    @ManyToOne
    @JoinColumn(name = "id_produit", nullable = false)
    @JsonIgnore
    private Produit produit;

    @ManyToOne
    @JoinColumn(name = "id_vendeur")
    @JsonIgnore
    private Vendeur vendeur;

    @ManyToOne
    @JoinColumn(name = "id_acheteur", nullable = false)
    @JsonIgnore
    private Acheteur acheteur;

    @Column(nullable = false)
    private Integer quantite = 1;

    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "montant_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal commission = BigDecimal.ZERO;

    @Column(name = "montant_net", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantNet;

    @Column(name = "date_vente")
    private LocalDateTime dateVente = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column
    private Statut statut = Statut.ENREGISTREE;

    @Column(name = "type_paiement", length = 50)
    private String typePaiement;

    @Column(name = "frais_transaction", precision = 10, scale = 2)
    private BigDecimal fraisTransaction = BigDecimal.ZERO;

    public enum Statut {
        ENREGISTREE("Enregistrée"),
        VALIDEE("Validée"),
        ANNULEE("Annulée"),
        REMBOURSEE("Remboursée");

        private final String valeur;

        Statut(String valeur) {
            this.valeur = valeur;
        }

        public String getValeur() {
            return valeur;
        }
    }

    @PrePersist
    @PreUpdate
    public void calculerMontants() {
        if (prixUnitaire != null && quantite != null) {
            montantTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
            montantNet = montantTotal.subtract(commission).subtract(fraisTransaction);
        }
    }
}