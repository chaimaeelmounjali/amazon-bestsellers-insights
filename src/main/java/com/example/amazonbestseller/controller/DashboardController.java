// ========== 4. DashboardController.java ==========
package com.example.amazonbestseller.controller;

import com.example.amazonbestseller.dto.DashboardVendeurDTO;
import com.example.amazonbestseller.entity.Utilisateur;
import com.example.amazonbestseller.security.SecurityUtils;
import com.example.amazonbestseller.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/global")
    public ResponseEntity<Map<String, Object>> getDashboardGlobal() {
        return ResponseEntity.ok(dashboardService.getDashboardGlobal());
    }

    @GetMapping("/acheteur/me")
    public ResponseEntity<Map<String, Object>> getDashboardAcheteurMe() {
        Utilisateur currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié"));

        if (!currentUser.getRole().equals(Utilisateur.Role.ACHETEUR)
                && !currentUser.getRole().equals(Utilisateur.Role.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès réservé aux acheteurs");
        }

        try {
            return ResponseEntity.ok(dashboardService.getDashboardAcheteur(currentUser.getId()));
        } catch (RuntimeException e) {
            // Fallback: If user exists but has no specific "Acheteur" data yet, return
            // empty/default dashboard
            System.err.println(
                    "Warning: Dashboard data missing for user " + currentUser.getId() + ". Returning empty dashboard.");
            Map<String, Object> emptyDashboard = new HashMap<>();
            emptyDashboard.put("commandes", Map.of("total", 0, "montantTotal", 0, "panierMoyen", 0));
            emptyDashboard.put("dernieresCommandes", new ArrayList<>());
            emptyDashboard.put("nombreAvis", 0);
            emptyDashboard.put("recommandations", new ArrayList<>());
            emptyDashboard.put("favoris", new ArrayList<>());
            emptyDashboard.put("alertes",
                    List.of(Map.of("type", "INFO", "message", "Bienvenue ! Commencez votre shopping dès maintenant.")));
            return ResponseEntity.ok(emptyDashboard);
        }
    }

    @GetMapping("/acheteur/{acheteurId}")
    public ResponseEntity<Map<String, Object>> getDashboardAcheteur(@PathVariable Long acheteurId) {
        // Vérifier si l'utilisateur a le droit d'accéder à ce tableau de bord
        Utilisateur currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié"));

        // Vérifier si l'utilisateur est un administrateur ou s'il accède à son propre
        // tableau de bord
        System.out.println(">>> [DEBUG] Controller - Requested acheteurId: " + acheteurId);
        System.out.println(
                ">>> [DEBUG] Controller - CurrentUser id: " + currentUser.getId() + ", role: " + currentUser.getRole());

        if (!currentUser.getRole().equals(Utilisateur.Role.ADMIN) &&
                !currentUser.getId().equals(acheteurId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès non autorisé à ce tableau de bord");
        }

        try {
            return ResponseEntity.ok(dashboardService.getDashboardAcheteur(acheteurId));
        } catch (RuntimeException e) {
            System.err.println(">>> [ERROR] Dashboard not found for ID: " + acheteurId + " - " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Acheteur non trouvé", e);
        }
    }

    @GetMapping("/vendeur/{vendeurId}")
    public ResponseEntity<DashboardVendeurDTO> getDashboardVendeur(@PathVariable Long vendeurId) {
        // Vérifier les autorisations
        Utilisateur currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié"));

        if (!currentUser.getRole().equals(Utilisateur.Role.ADMIN) &&
                !currentUser.getId().equals(vendeurId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès non autorisé à ce tableau de bord");
        }

        try {
            return ResponseEntity.ok(dashboardService.getDashboardVendeur(vendeurId));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendeur non trouvé", e);
        }
    }

    @GetMapping("/investisseur/{investisseurId}")
    public ResponseEntity<Map<String, Object>> getDashboardInvestisseur(@PathVariable Long investisseurId) {
        // Vérifier les autorisations
        Utilisateur currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié"));

        if (!currentUser.getRole().equals(Utilisateur.Role.ADMIN) &&
                !currentUser.getId().equals(investisseurId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès non autorisé à ce tableau de bord");
        }

        try {
            return ResponseEntity.ok(dashboardService.getDashboardInvestisseur(investisseurId));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Investisseur non trouvé", e);
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getDashboardAdmin() {
        // Vérifier que l'utilisateur est un administrateur
        Utilisateur currentUser = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié"));

        if (!currentUser.getRole().equals(Utilisateur.Role.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès réservé aux administrateurs");
        }

        return ResponseEntity.ok(dashboardService.getDashboardAdmin());
    }
}
