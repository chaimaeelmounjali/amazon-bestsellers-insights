package com.example.amazonbestseller.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "magasin", indexes = {
        @Index(name = "idx_nom", columnList = "nom"),
        @Index(name = "idx_note", columnList = "note")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Magasin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(length = 255)
    private String adresse;

    @Column(name = "numero_telephone", length = 20)
    private String numeroTelephone;

    @Column(length = 150)
    private String email;

    @Column(name = "heures_ouverture", length = 100)
    private String heuresOuverture;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal note = BigDecimal.ZERO;

    @Column(name = "id_vendeur")
    private Long idVendeur;

    @OneToMany(mappedBy = "magasin", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<Produit> produits;

    @OneToMany(mappedBy = "magasin", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<Vendeur> vendeurs;
}