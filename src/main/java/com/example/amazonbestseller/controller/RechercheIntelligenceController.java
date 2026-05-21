// ========== 7. RechercheIntelligenceController.java ==========
package com.example.amazonbestseller.controller;

import com.example.amazonbestseller.entity.Produit;
import com.example.amazonbestseller.service.RechercheIntelligenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recherche")
@RequiredArgsConstructor

public class RechercheIntelligenceController {

    private final RechercheIntelligenceService rechercheService;

    @GetMapping("/simple")
    public ResponseEntity<List<Produit>> rechercheSimple(@RequestParam String query) {
        return ResponseEntity.ok(rechercheService.rechercheSimple(query));
    }

    @GetMapping("/semantique")
    public ResponseEntity<List<Produit>> rechercheSemantique(@RequestParam String query) {
        rechercheService.ajouterAHistorique("user", query);
        return ResponseEntity.ok(rechercheService.rechercheSemantique(query));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String partialQuery) {
        return ResponseEntity.ok(rechercheService.getSuggestions(partialQuery));
    }

    @GetMapping("/historique/{utilisateurId}")
    public ResponseEntity<List<String>> getHistorique(@PathVariable String utilisateurId) {
        return ResponseEntity.ok(rechercheService.getHistoriqueRecherche(utilisateurId));
    }

    @GetMapping("/similaires/{produitId}")
    public ResponseEntity<List<Produit>> getProduitsSimilaires(@PathVariable Long produitId) {
        return ResponseEntity.ok(rechercheService.getProduitsSimilaires(produitId));
    }

    @GetMapping("/asin/{asin}")
    public ResponseEntity<Produit> rechercherParAsin(@PathVariable String asin) {
        return ResponseEntity.ok(rechercheService.rechercherParAsin(asin));
    }
}
