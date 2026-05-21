package com.example.amazonbestseller.controller;

import com.example.amazonbestseller.dto.ProduitDTO;
import com.example.amazonbestseller.service.ProduitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;

    @PostMapping
    public ResponseEntity<ProduitDTO> ajouterProduit(@RequestBody ProduitDTO produitDTO) {
        return ResponseEntity.ok(produitService.ajouterProduit(produitDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProduitDTO> modifierProduit(@PathVariable Long id, @RequestBody ProduitDTO produitDTO) {
        return ResponseEntity.ok(produitService.modifierProduit(id, produitDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerProduit(@PathVariable Long id) {
        produitService.supprimerProduit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduitDTO> getProduit(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.getProduitById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProduitDTO>> getTousProduits() {
        return ResponseEntity.ok(produitService.getTousProduits());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<ProduitDTO>> getProduitsDisponibles() {
        return ResponseEntity.ok(produitService.getProduitsDisponibles());
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<ProduitDTO>> rechercherProduits(@RequestParam String keyword) {
        return ResponseEntity.ok(produitService.rechercherProduits(keyword));
    }

    @GetMapping("/asin/{asin}")
    public ResponseEntity<ProduitDTO> getProduitByAsin(@PathVariable String asin) {
        return ResponseEntity.ok(produitService.getProduitByAsin(asin));
    }

    @GetMapping("/top")
    public ResponseEntity<List<ProduitDTO>> getTopProduits(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(produitService.getTopProduits(limit));
    }

    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<ProduitDTO>> getProduitsByCategorie(@PathVariable String categorie) {
        return ResponseEntity.ok(produitService.getProduitsByCategorie(categorie));
    }

    @GetMapping("/vendeur/{id}")
    public ResponseEntity<List<ProduitDTO>> getProduitsByVendeur(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.getProduitsByVendeur(id));
    }

    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        return ResponseEntity.ok(produitService.getStatistiquesDashboard());
    }

    @GetMapping("/analyse-categorie")
    public ResponseEntity<Map<String, Object>> getAnalyseCategorie(@RequestParam String categorie) {
        return ResponseEntity.ok(produitService.getAnalyseCategorie(categorie));
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<Void> mettreAJourStock(@PathVariable Long id, @RequestParam Integer quantite) {
        produitService.mettreAJourStock(id, quantite);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/seller/{sellerId}/dashboard")
    public ResponseEntity<Map<String, Object>> getSellerDashboard(@PathVariable Long sellerId) {
        return ResponseEntity.ok(produitService.getSellerDashboardData(sellerId));
    }
}