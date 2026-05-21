// ========== 8. FiltrageTriController.java ==========
package com.example.amazonbestseller.controller;

import com.example.amazonbestseller.dto.FiltreProduitsDTO;
import com.example.amazonbestseller.entity.Produit;
import com.example.amazonbestseller.service.FiltrageTriService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/filtrage")
@RequiredArgsConstructor

public class FiltrageTriController {

    private final FiltrageTriService filtrageService;

    @PostMapping("/produits")
    public ResponseEntity<List<com.example.amazonbestseller.dto.ProduitDTO>> filtrerEtTrierProduits(
            @RequestBody FiltreProduitsDTO filtres) {
        return ResponseEntity.ok(filtrageService.filtrerEtTrierProduits(filtres));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getToutesCategories() {
        return ResponseEntity.ok(filtrageService.getToutesCategories());
    }

    @GetMapping("/prix-range")
    public ResponseEntity<FiltrageTriService.PrixRange> getPrixRange() {
        return ResponseEntity.ok(filtrageService.getPrixRange());
    }

    @GetMapping("/recherche-avancee")
    public ResponseEntity<List<com.example.amazonbestseller.dto.ProduitDTO>> rechercheAvancee(
            @RequestParam String keyword,
            @RequestBody FiltreProduitsDTO filtres) {
        return ResponseEntity.ok(filtrageService.rechercheAvancee(keyword, filtres));
    }
}
