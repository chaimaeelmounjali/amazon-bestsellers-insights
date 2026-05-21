package com.example.amazonbestseller.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "vendeur", indexes = {
        @Index(name = "idx_magasin", columnList = "id_magasin"),
        @Index(name = "idx_ventes_totales", columnList = "ventes_totales")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Vendeur extends Utilisateur {

    @ManyToOne
    @JoinColumn(name = "id_magasin")
    @JsonIgnore
    @ToString.Exclude
    private Magasin magasin;

    @Column(precision = 5, scale = 2)
    private BigDecimal commission = BigDecimal.ZERO;

    @Column(name = "objectif_ventes", precision = 12, scale = 2)
    private BigDecimal objectifVentes = BigDecimal.ZERO;

    @Column(name = "ventes_totales", precision = 12, scale = 2)
    private BigDecimal ventesTotales = BigDecimal.ZERO;

    @OneToMany(mappedBy = "vendeur", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<Commande> commandes;

    @OneToMany(mappedBy = "vendeur", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<Vente> ventes;
}
