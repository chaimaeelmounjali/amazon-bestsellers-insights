
// File: AvisDTO.java
package com.example.amazonbestseller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvisDTO {
    private Long produitId;
    private Long acheteurId;
    private BigDecimal note;
    private String commentaire;
    private Boolean estVerifie;
}
