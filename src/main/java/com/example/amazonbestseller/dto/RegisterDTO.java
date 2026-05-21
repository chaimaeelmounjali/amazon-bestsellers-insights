// File: RegisterDTO.java
package com.example.amazonbestseller.dto;

import com.example.amazonbestseller.entity.Utilisateur;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    private String nomUtilisateur;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String motDePasse;

    private Utilisateur.Role role;

    // Pour Acheteur
    private String adresse;
    private String telephone;

    // Pour Vendeur
    private BigDecimal objectifVentes;

    // Pour Investisseur
    private BigDecimal montantInvestissement;
}