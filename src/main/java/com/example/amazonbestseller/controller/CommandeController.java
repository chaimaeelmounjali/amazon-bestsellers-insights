// ========== 3. CommandeController.java ==========
package com.example.amazonbestseller.controller;

import com.example.amazonbestseller.dto.CommandeDTO;
import com.example.amazonbestseller.entity.Commande;
import com.example.amazonbestseller.service.CommandeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;

    @PostMapping
    public ResponseEntity<Commande> creerCommande(@RequestBody CommandeDTO commandeDTO) {
        return ResponseEntity.ok(commandeService.creerCommande(commandeDTO));
    }

    @PutMapping("/{id}/confirmer")
    public ResponseEntity<Commande> confirmerCommande(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.confirmerCommande(id));
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<Commande> annulerCommande(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.annulerCommande(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Commande> getCommande(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.getCommandeById(id));
    }

    @GetMapping("/acheteur/{acheteurId}")
    public ResponseEntity<List<Commande>> getCommandesAcheteur(@PathVariable Long acheteurId) {
        return ResponseEntity.ok(commandeService.getCommandesAcheteur(acheteurId));
    }

    @GetMapping("/vendeur/{vendeurId}")
    public ResponseEntity<List<Commande>> getCommandesVendeur(@PathVariable Long vendeurId) {
        return ResponseEntity.ok(commandeService.getCommandesVendeur(vendeurId));
    }

    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(commandeService.getStatistiquesCommandes(debut, fin));
    }

    @PostMapping("/manuelle")
    public ResponseEntity<Commande> creerVenteManuelle(@RequestBody Map<String, Object> payload) {
        Long vendeurId = payload.get("vendeurId") instanceof Number ? ((Number) payload.get("vendeurId")).longValue()
                : Long.parseLong(payload.get("vendeurId").toString());
        Long produitId = payload.get("produitId") instanceof Number ? ((Number) payload.get("produitId")).longValue()
                : Long.parseLong(payload.get("produitId").toString());
        int quantite = payload.get("quantite") instanceof Number ? ((Number) payload.get("quantite")).intValue()
                : Integer.parseInt(payload.get("quantite").toString());
        BigDecimal prix = new BigDecimal(payload.get("prix").toString());

        return ResponseEntity.ok(commandeService.creerVenteManuelle(vendeurId, produitId, quantite, prix));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Commande> modifierCommande(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Integer quantite = updates.get("quantite") != null ? ((Number) updates.get("quantite")).intValue() : null;
        String statut = (String) updates.get("statut");
        return ResponseEntity.ok(commandeService.modifierCommande(id, quantite, statut));
    }
}