// File: VenteDTO.java
package com.example.amazonbestseller.dto;

import com.example.amazonbestseller.entity.Vente;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VenteDTO {
    private Long produitId;
    private Long vendeurId;
    private Long acheteurId;
    private String nomAcheteur;
    private Integer quantite;
    private Vente.Statut statut;
}