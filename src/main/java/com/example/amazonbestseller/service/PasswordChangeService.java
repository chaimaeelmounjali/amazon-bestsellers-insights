package com.example.amazonbestseller.service;

import com.example.amazonbestseller.entity.Utilisateur;
import com.example.amazonbestseller.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordChangeService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(String userEmail, String oldPassword, String newPassword) {
        // Récupérer l'utilisateur
        Utilisateur user = utilisateurRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(oldPassword, user.getMotDePasse())) {
            throw new IllegalArgumentException("L'ancien mot de passe est incorrect");
        }

        // Valider la force du nouveau mot de passe
        Map<String, Object> strength = validatePasswordStrength(newPassword);
        if (!"strong".equals(strength.get("level")) && !"medium".equals(strength.get("level"))) {
            throw new IllegalArgumentException("Le nouveau mot de passe est trop faible");
        }

        // Encoder et sauvegarder le nouveau mot de passe
        user.setMotDePasse(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(user);

        log.info("Mot de passe changé avec succès pour l'utilisateur: {}", userEmail);
    }

    public Map<String, Object> validatePasswordStrength(String password) {
        Map<String, Object> result = new HashMap<>();

        if (password == null || password.isEmpty()) {
            result.put("level", "weak");
            result.put("score", 0);
            result.put("message", "Le mot de passe ne peut pas être vide");
            return result;
        }

        int score = 0;

        // Longueur
        if (password.length() >= 8)
            score++;
        if (password.length() >= 12)
            score++;

        // Contient des minuscules
        if (password.matches(".*[a-z].*"))
            score++;

        // Contient des majuscules
        if (password.matches(".*[A-Z].*"))
            score++;

        // Contient des chiffres
        if (password.matches(".*\\d.*"))
            score++;

        // Contient des caractères spéciaux
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"))
            score++;

        String level;
        String message;

        if (score <= 2) {
            level = "weak";
            message = "Mot de passe faible. Ajoutez des majuscules, chiffres et caractères spéciaux.";
        } else if (score <= 4) {
            level = "medium";
            message = "Mot de passe moyen. Ajoutez plus de caractères pour le renforcer.";
        } else {
            level = "strong";
            message = "Mot de passe fort.";
        }

        result.put("level", level);
        result.put("score", score);
        result.put("message", message);

        return result;
    }
}
