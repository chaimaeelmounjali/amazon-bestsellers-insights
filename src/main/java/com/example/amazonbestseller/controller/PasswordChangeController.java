package com.example.amazonbestseller.controller;

import com.example.amazonbestseller.dto.PasswordChangeDTO;
import com.example.amazonbestseller.service.PasswordChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor

public class PasswordChangeController {

    private final PasswordChangeService passwordChangeService;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody PasswordChangeDTO passwordChangeDTO,
            Authentication authentication) {

        try {
            // Vérifier que les mots de passe correspondent
            if (!passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Les mots de passe ne correspondent pas"));
            }

            // Récupérer l'email de l'utilisateur connecté
            String userEmail = authentication.getName();

            // Changer le mot de passe
            passwordChangeService.changePassword(
                    userEmail,
                    passwordChangeDTO.getOldPassword(),
                    passwordChangeDTO.getNewPassword());

            return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Une erreur est survenue lors du changement de mot de passe"));
        }
    }

    @PostMapping("/validate-password-strength")
    public ResponseEntity<?> validatePasswordStrength(@RequestBody Map<String, String> request) {
        String password = request.get("password");
        Map<String, Object> strength = passwordChangeService.validatePasswordStrength(password);
        return ResponseEntity.ok(strength);
    }
}
