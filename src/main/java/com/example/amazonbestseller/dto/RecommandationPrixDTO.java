// File: RecommandationPrixDTO.java
package com.example.amazonbestseller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommandationPrixDTO {
    private BigDecimal prixActuel;
    private BigDecimal prixRecommande;
    private BigDecimal prixMin;
    private BigDecimal prixMax;
    private BigDecimal prixMoyen;
    private String strategie;
    private String impactEstime;
    private String justification;
}