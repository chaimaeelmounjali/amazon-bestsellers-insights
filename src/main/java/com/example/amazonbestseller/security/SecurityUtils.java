package com.example.amazonbestseller.security;

import com.example.amazonbestseller.entity.Utilisateur;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    public static Optional<Utilisateur> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getPrincipal() instanceof Utilisateur) {
            return Optional.of((Utilisateur) authentication.getPrincipal());
        }
        
        return Optional.empty();
    }
}
