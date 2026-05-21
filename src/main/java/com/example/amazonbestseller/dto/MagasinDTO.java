// File: MagasinDTO.java
package com.example.amazonbestseller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MagasinDTO {
    private String nom;
    private String adresse;
    private String email;
    private String numeroTelephone;
    private Long idVendeur;
    private BigDecimal note;
}
