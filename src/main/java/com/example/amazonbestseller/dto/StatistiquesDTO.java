// File: StatistiquesDTO.java
package com.example.amazonbestseller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatistiquesDTO {
    private Long totalProduits;
    private Long produitsDisponibles;
    private Long nombreCategories;
    private BigDecimal prixMoyen;
    private BigDecimal noteMoyenne;
    private Map<String, Long> distributionCategories;
}
