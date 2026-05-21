package com.example.amazonbestseller.controller;

import com.example.amazonbestseller.entity.Acheteur;
import com.example.amazonbestseller.entity.Commande;
import com.example.amazonbestseller.repository.AcheteurRepository;
import com.example.amazonbestseller.repository.CommandeRepository;
import com.example.amazonbestseller.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/diag")
@RequiredArgsConstructor
public class DiagnosticsController {

    private final AcheteurRepository acheteurRepository;
    private final CommandeRepository commandeRepository;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/alice")
    public ResponseEntity<Map<String, Object>> checkAlice() {
        Map<String, Object> response = new HashMap<>();

        // 1. Check User in Base Repos
        var user = utilisateurRepository.findByEmail("alice@gmail.com").orElse(null);
        response.put("userFound", user != null);
        if (user != null) {
            response.put("userId", user.getId());
            response.put("userRole", user.getRole());
            response.put("userClass", user.getClass().getSimpleName());
        }

        // 2. Check Acheteur
        var acheteur = acheteurRepository.findByEmail("alice@gmail.com").orElse(null);
        response.put("acheteurFound", acheteur != null);
        if (acheteur != null) {
            response.put("acheteurId", acheteur.getId());

            // 3. Check Orders
            List<Commande> commandes = commandeRepository.findByAcheteurId(acheteur.getId());
            response.put("orderCount", commandes.size());
            response.put("orders", commandes.stream().map(c -> Map.<String, Object>of(
                    "id", c.getId(),
                    "montant", c.getMontantTotal(),
                    "statut", c.getStatut())).collect(Collectors.toList()));
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> allUsers() {
        return ResponseEntity.ok(utilisateurRepository.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "email", u.getEmail(),
                        "role", u.getRole()))
                .collect(Collectors.toList()));
    }

    private final com.example.amazonbestseller.service.DashboardService dashboardService;

    @GetMapping("/debug-dashboard-alice")
    public ResponseEntity<Map<String, Object>> debugDashboardAlice() {
        return ResponseEntity.ok(dashboardService.getDashboardAcheteur(6L));
    }

    @GetMapping("/generate-alice-data")
    public ResponseEntity<Map<String, Object>> generateAliceData() {
        Acheteur alice = acheteurRepository.findByEmail("alice@gmail.com")
                .orElseThrow(() -> new RuntimeException("Alice non trouvée pour la génération"));

        dashboardService.genererDonneesPourAcheteur(alice);

        return ResponseEntity.ok(dashboardService.getDashboardAcheteur(alice.getId()));
    }

    @GetMapping("/users-detailed")
    public ResponseEntity<Map<String, Object>> diagnoseUsers(jakarta.servlet.http.HttpSession session) {
        Map<String, Object> diag = new HashMap<>();

        // 1. Database Check
        long count = utilisateurRepository.count();
        diag.put("db_user_count", count);
        if (count > 0) {
            diag.put("first_5_users", utilisateurRepository.findAll().stream()
                    .limit(5)
                    .map(u -> u.getEmail() + " (" + u.getRole() + ")")
                    .collect(Collectors.toList()));
        }

        // 2. SecurityContext Check
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> authInfo = new HashMap<>();
        if (auth != null) {
            authInfo.put("name", auth.getName());
            authInfo.put("authorities", auth.getAuthorities().toString());
            authInfo.put("isAuthenticated", auth.isAuthenticated());
            authInfo.put("principal", auth.getPrincipal().toString());
        } else {
            authInfo.put("status", "No Authentication Object");
        }
        diag.put("security_context", authInfo);

        // 3. Session Check
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("id", session.getId());
        sessionInfo.put("utilisateurId", session.getAttribute("utilisateurId"));
        sessionInfo.put("utilisateurRole", session.getAttribute("utilisateurRole"));

        // Check for SPRING_SECURITY_CONTEXT in session
        var securityContextInSession = session.getAttribute("SPRING_SECURITY_CONTEXT");
        sessionInfo.put("has_spring_security_context", securityContextInSession != null);

        diag.put("session_info", sessionInfo);

        return ResponseEntity.ok(diag);
    }
}
