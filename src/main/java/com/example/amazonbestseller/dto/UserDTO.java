package com.example.amazonbestseller.dto;

import com.example.amazonbestseller.entity.Utilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String nomUtilisateur;
    private String email;
    private Utilisateur.Role role;
    private Boolean estActif;
    private LocalDateTime dateCreation;

    // Champs spécifiques selon le rôle
    private String telephone;
    private String adresse;

    // Pour la création/modification
    private String password;
}
