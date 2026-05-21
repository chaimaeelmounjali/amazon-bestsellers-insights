// File: ProduitDTO.java
package com.example.amazonbestseller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProduitDTO {
    private Long id;
    private Long vendeurId; // ID du vendeur pour lier au magasin
    private String asin;
    private String nom;
    private String description;
    private String categorie;
    private BigDecimal prix;
    private BigDecimal note;
    private Integer nombreAvis;
    private Integer rang;
    private Boolean estDisponible;

    // Pour la gestion du stock
    private Integer quantiteStock;
    private Integer seuilMin;
    private String emplacement;
    private String urlImage;
    private String urlProduit;
}