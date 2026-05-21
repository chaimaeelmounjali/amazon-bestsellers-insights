// ========== 5. AlerteController.java ==========
package com.example.amazonbestseller.controller;

import com.example.amazonbestseller.dto.AlerteDTO;
import com.example.amazonbestseller.entity.Alerte;
import com.example.amazonbestseller.service.AlerteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertes")
@RequiredArgsConstructor

public class AlerteController {

    private final AlerteService alerteService;

    @PostMapping
    public ResponseEntity<Alerte> creerAlerte(@RequestBody AlerteDTO alerteDTO) {
        return ResponseEntity.ok(alerteService.creerAlerteDTO(alerteDTO));
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<Alerte>> getAlertesUtilisateur(@PathVariable Long utilisateurId) {
        return ResponseEntity.ok(alerteService.getAlertesUtilisateur(utilisateurId));
    }

    @GetMapping("/non-lues/{utilisateurId}")
    public ResponseEntity<List<Alerte>> getAlertesNonLues(@PathVariable Long utilisateurId) {
        return ResponseEntity.ok(alerteService.getAlertesNonLues(utilisateurId));
    }

    @PutMapping("/{id}/lire")
    public ResponseEntity<Void> marquerCommeLu(@PathVariable Long id) {
        alerteService.marquerCommeLu(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/lire-toutes/{utilisateurId}")
    public ResponseEntity<Void> marquerToutesCommeLues(@PathVariable Long utilisateurId) {
        alerteService.marquerToutesCommeLues(utilisateurId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerAlerte(@PathVariable Long id) {
        alerteService.supprimerAlerte(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        return ResponseEntity.ok(alerteService.getStatistiquesAlertes());
    }
}