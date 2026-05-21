// File: CommandeDTO.java
package com.example.amazonbestseller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommandeDTO {
    private Long acheteurId;
    private Long vendeurId;
    private String methodePaiement;
    private List<LigneCommandeDTO> lignes;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LigneCommandeDTO {
        private Long produitId;
        private Integer quantite;
    }
}