// File: UtilisateurDTO.java
package com.example.amazonbestseller.dto;

import com.example.amazonbestseller.entity.Utilisateur;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UtilisateurDTO {
    private Long id;
    private String nomUtilisateur;
    private String email;
    private Utilisateur.Role role;
    private Boolean estActif;
    private LocalDateTime dateInscription;
}