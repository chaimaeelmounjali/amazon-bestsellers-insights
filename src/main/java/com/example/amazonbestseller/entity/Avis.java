package com.example.amazonbestseller.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "avis", indexes = {
        @Index(name = "idx_produit", columnList = "id_produit"),
        @Index(name = "idx_acheteur", columnList = "id_acheteur"),
        @Index(name = "idx_date", columnList = "date_avis"),
        @Index(name = "idx_note", columnList = "note"),
        @Index(name = "idx_est_verifie", columnList = "est_verifie")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Avis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_produit", nullable = false)
    @JsonIgnore
    private Produit produit;

    @ManyToOne
    @JoinColumn(name = "id_acheteur", nullable = false)
    @JsonIgnore
    private Acheteur acheteur;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal note;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_avis")
    private LocalDateTime dateAvis = LocalDateTime.now();

    @Column(name = "est_verifie")
    private Boolean estVerifie = false;

    @Column(name = "nombre_utiles")
    private Integer nombreUtiles = 0;
}
