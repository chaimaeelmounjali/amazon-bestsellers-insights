// File: RechercheDTO.java
package com.example.amazonbestseller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RechercheDTO {
    private String query;
    private String type; // "simple", "semantique", "asin"
    private FiltreProduitsDTO filtres;
}
