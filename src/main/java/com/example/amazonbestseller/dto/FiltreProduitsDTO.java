// File: FiltreProduitsDTO.java
package com.example.amazonbestseller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FiltreProduitsDTO {
    private String categorie;
    private BigDecimal prixMin;
    private BigDecimal prixMax;
    private BigDecimal noteMin;
    private Integer nombreAvisMin;
    private String triPar; // "rang", "prix", "note", "avis"
    private String ordreTri; // "asc", "desc"
    private String motCle; // Pour la recherche unifiée
}
