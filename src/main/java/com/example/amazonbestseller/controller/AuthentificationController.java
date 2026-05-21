// ========== 10. AuthentificationController.java ==========
package com.example.amazonbestseller.controller;

import com.example.amazonbestseller.dto.LoginDTO;
import com.example.amazonbestseller.dto.RegisterDTO;
import com.example.amazonbestseller.entity.Utilisateur;
import com.example.amazonbestseller.service.AuthentificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthentificationController {

    private final AuthentificationService authentificationService;

    @PostMapping("/inscription")
    public ResponseEntity<Utilisateur> inscrire(@RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.ok(authentificationService.inscrireUtilisateur(registerDTO));
    }

    @PostMapping("/connexion")
    public ResponseEntity<Utilisateur> connecter(@RequestBody LoginDTO loginDTO, HttpSession session) {
        return ResponseEntity.ok(authentificationService.connecter(loginDTO, session));
    }

    @PostMapping("/deconnexion")
    public ResponseEntity<Void> deconnecter(HttpSession session) {
        authentificationService.deconnecter(session);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/utilisateur-connecte")
    public ResponseEntity<Utilisateur> getUtilisateurConnecte(HttpSession session) {
        return ResponseEntity.ok(authentificationService.getUtilisateurConnecte(session));
    }

    @PutMapping("/profil/{id}")
    public ResponseEntity<Utilisateur> mettreAJourProfil(
            @PathVariable Long id,
            @RequestBody RegisterDTO updateDTO) {
        return ResponseEntity.ok(authentificationService.mettreAJourProfil(id, updateDTO));
    }

    @PutMapping("/changer-mot-de-passe/{id}")
    public ResponseEntity<Void> changerMotDePasse(
            @PathVariable Long id,
            @RequestParam String ancienMotDePasse,
            @RequestParam String nouveauMotDePasse) {
        authentificationService.changerMotDePasse(id, ancienMotDePasse, nouveauMotDePasse);
        return ResponseEntity.ok().build();
    }
}
