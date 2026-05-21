package com.example.amazonbestseller.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "ligne_commande", indexes = {
        @Index(name = "idx_commande", columnList = "id_commande"),
        @Index(name = "idx_produit", columnList = "id_produit")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneCommande {

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

    @Column(nullable = false)
    private Integer quantite = 1;

    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "sous_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal sousTotal;

    @PrePersist
    @PreUpdate
    public void calculerSousTotal() {
        if (prixUnitaire != null && quantite != null) {
            sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }
}
