// ========== 2. VenteController.java ==========
package com.example.amazonbestseller.controller;

import com.example.amazonbestseller.dto.VenteDTO;
import com.example.amazonbestseller.entity.Vente;
import com.example.amazonbestseller.service.VenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventes")
@RequiredArgsConstructor

public class VenteController {

    private final VenteService venteService;

    @PostMapping
    public ResponseEntity<Vente> creerVente(@RequestBody VenteDTO venteDTO) {
        return ResponseEntity.ok(venteService.creerVente(venteDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vente> modifierVente(@PathVariable Long id, @RequestBody VenteDTO venteDTO) {
        return ResponseEntity.ok(venteService.modifierVente(id, venteDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annulerVente(@PathVariable Long id, @RequestParam String motif) {
        venteService.annulerVente(id, motif);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vente> getVente(@PathVariable Long id) {
        return ResponseEntity.ok(venteService.getVenteById(id));
    }

    @GetMapping("/produit/{produitId}")
    public ResponseEntity<List<Vente>> getVentesByProduit(@PathVariable Long produitId) {
        return ResponseEntity.ok(venteService.getVentesByProduit(produitId));
    }

    @GetMapping("/vendeur/{vendeurId}")
    public ResponseEntity<List<Vente>> getVentesByVendeur(@PathVariable Long vendeurId) {
        return ResponseEntity.ok(venteService.getVentesByVendeur(vendeurId));
    }

    @GetMapping("/vendeur/{vendeurId}/analyse")
    public ResponseEntity<Map<String, Object>> getAnalyseVendeur(
            @PathVariable Long vendeurId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {

        // Valeurs par défaut : 30 derniers jours si null
        if (dateDebut == null)
            dateDebut = LocalDateTime.now().minusDays(30);
        if (dateFin == null)
            dateFin = LocalDateTime.now();

        return ResponseEntity.ok(venteService.getAnalyseVendeur(vendeurId, dateDebut, dateFin));
    }

    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {
        return ResponseEntity.ok(venteService.getStatistiquesVentes(dateDebut, dateFin));
    }

    @GetMapping("/par-categorie")
    public ResponseEntity<Map<String, Object>> getVentesParCategorie(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {
        return ResponseEntity.ok(venteService.getVentesParCategorie(dateDebut, dateFin));
    }

    @GetMapping("/top-produits")
    public ResponseEntity<List<Object[]>> getTopProduitsVendus(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {
        return ResponseEntity.ok(venteService.getTopProduitsVendus(dateDebut, dateFin));
    }

    @GetMapping("/performance-vendeurs")
    public ResponseEntity<List<Object[]>> getPerformanceVendeurs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {
        return ResponseEntity.ok(venteService.getPerformanceVendeurs(dateDebut, dateFin));
    }
}
