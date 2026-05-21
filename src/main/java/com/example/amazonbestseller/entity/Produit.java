package com.example.amazonbestseller.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "produit", indexes = {
        @Index(name = "idx_asin", columnList = "asin"),
        @Index(name = "idx_categorie", columnList = "categorie"),
        @Index(name = "idx_rang", columnList = "rang"),
        @Index(name = "idx_prix", columnList = "prix"),
        @Index(name = "idx_note", columnList = "note"),
        @Index(name = "idx_est_disponible", columnList = "est_disponible"),
        @Index(name = "idx_magasin", columnList = "id_magasin")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String asin;

    @Column(length = 500)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Column(precision = 3, scale = 2)
    private BigDecimal note = BigDecimal.ZERO;

    @Column(name = "nombre_avis")
    private Integer nombreAvis = 0;

    @Column
    private Integer rang = 0;

    @Column(length = 100)
    private String categorie;

    @Column(name = "url_image", length = 500)
    private String urlImage;

    @Column(name = "url_produit", nullable = false, length = 500)
    private String urlProduit;

    @Column(name = "nombre_vendeurs")
    private Integer nombreVendeurs = 1;

    @Column(name = "est_disponible")
    private Boolean estDisponible = true;

    @Column(name = "date_ajout")
    private LocalDateTime dateAjout = LocalDateTime.now();

    // Relation ManyToOne avec Magasin
    // IMPORTANT : LAZY est correct, on utilisera JOIN FETCH quand nécessaire
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_magasin")
    @JsonIgnore
    private Magasin magasin;

    // Relation OneToOne avec Stock
    // IMPORTANT : LAZY est correct, on utilisera JOIN FETCH quand nécessaire
    @OneToOne(mappedBy = "produit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Stock stock;

    // Relations OneToMany (toujours LAZY par défaut)
    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<LigneCommande> lignesCommande;

    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Vente> ventes;

    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Avis> avis;

    // Méthode utilitaire pour ajouter un stock
    public void ajouterStock(Stock stock) {
        this.stock = stock;
        stock.setProduit(this);
    }

    // Méthode utilitaire pour vérifier la disponibilité
    public boolean estEnStock() {
        return this.stock != null && this.stock.getQuantite() > 0;
    }

    // Méthode utilitaire pour vérifier si le stock est faible
    public boolean estStockFaible() {
        return this.stock != null && this.stock.getQuantite() < this.stock.getSeuilMin();
    }
}