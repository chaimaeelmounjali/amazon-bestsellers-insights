// File: PredictionDTO.java
package com.example.amazonbestseller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PredictionDTO {
    private Long produitId;
    private Integer joursAvenir;
    private Integer rangActuel;
    private Integer rangPredit;
    private String tendance;
    private Double confiance;
}