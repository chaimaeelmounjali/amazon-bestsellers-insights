// ========== 12. AvisController.java ==========
package com.example.amazonbestseller.controller;

import com.example.amazonbestseller.dto.AvisDTO;
import com.example.amazonbestseller.entity.Avis;
import com.example.amazonbestseller.service.AvisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/avis")
@RequiredArgsConstructor

public class AvisController {

    private final AvisService avisService;

    @PostMapping
    public ResponseEntity<Avis> ajouterAvis(@RequestBody AvisDTO avisDTO) {
        return ResponseEntity.ok(avisService.ajouterAvis(avisDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Avis> modifierAvis(@PathVariable Long id, @RequestBody AvisDTO avisDTO) {
        return ResponseEntity.ok(avisService.modifierAvis(id, avisDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerAvis(@PathVariable Long id) {
        avisService.supprimerAvis(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/utile")
    public ResponseEntity<Avis> marquerUtile(@PathVariable Long id) {
        return ResponseEntity.ok(avisService.marquerUtile(id));
    }

    @GetMapping("/produit/{produitId}")
    public ResponseEntity<List<Avis>> getAvisProduit(@PathVariable Long produitId) {
        return ResponseEntity.ok(avisService.getAvisProduit(produitId));
    }

    @GetMapping("/acheteur/{acheteurId}")
    public ResponseEntity<List<Avis>> getAvisAcheteur(@PathVariable Long acheteurId) {
        return ResponseEntity.ok(avisService.getAvisAcheteur(acheteurId));
    }

    @GetMapping("/note-moyenne/{produitId}")
    public ResponseEntity<BigDecimal> getNoteMoyenne(@PathVariable Long produitId) {
        return ResponseEntity.ok(avisService.getNoteMoyenneProduit(produitId));
    }

    @GetMapping("/recents")
    public ResponseEntity<List<Avis>> getAvisRecents(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(avisService.getAvisRecents(limit));
    }
}