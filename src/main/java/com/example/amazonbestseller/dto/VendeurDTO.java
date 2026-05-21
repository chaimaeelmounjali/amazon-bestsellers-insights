// File: VendeurDTO.java
package com.example.amazonbestseller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendeurDTO {
    private Long id;
    private String nomUtilisateur;
    private String email;
    private BigDecimal ventesTotales;
    private BigDecimal commission;
    private BigDecimal objectifVentes;
    private Boolean estActif;
}