// File: DashboardDTO.java
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
public class DashboardDTO {
    private Map<String, Object> produits;
    private Map<String, Object> ventes;
    private Map<String, Object> utilisateurs;
    private Map<String, Long> distributionCategories;
    private List<Object> top10Produits;
    private Map<String, Object> alertes;
}