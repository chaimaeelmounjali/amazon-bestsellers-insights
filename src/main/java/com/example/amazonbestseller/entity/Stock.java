package com.example.amazonbestseller.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock", indexes = {
        @Index(name = "idx_quantite", columnList = "quantite"),
        @Index(name = "idx_seuil_min", columnList = "seuil_min")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_produit", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Produit produit;

    @Column
    private Integer quantite = 0;

    @Column(name = "seuil_min")
    private Integer seuilMin = 10;

    @Column(name = "seuil_max")
    private Integer seuilMax = 1000;

    @Column(name = "derniere_maj")
    private LocalDateTime derniereMaj = LocalDateTime.now();

    @Column(length = 100)
    private String emplacement;

    @PreUpdate
    public void preUpdate() {
        derniereMaj = LocalDateTime.now();
    }
}
