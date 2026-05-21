// File: AcheteurDTO.java
package com.example.amazonbestseller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcheteurDTO {
    private Long id;
    private String nomUtilisateur;
    private String email;
    private String adresseLivraison;
    private String numeroTelephone;
    private Boolean estActif;
}