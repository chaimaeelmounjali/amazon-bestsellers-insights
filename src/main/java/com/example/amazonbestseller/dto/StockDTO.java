// File: StockDTO.java
package com.example.amazonbestseller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockDTO {
    private Long produitId;
    private Integer quantite;
    private Integer seuilMin;
    private String emplacement;
}