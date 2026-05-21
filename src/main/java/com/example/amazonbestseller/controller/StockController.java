// ========== 11. StockController.java ==========
package com.example.amazonbestseller.controller;

import com.example.amazonbestseller.dto.StockDTO;
import com.example.amazonbestseller.entity.Stock;
import com.example.amazonbestseller.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor

public class StockController {

    private final StockService stockService;

    @PostMapping
    public ResponseEntity<Stock> ajouterStock(@RequestBody StockDTO stockDTO) {
        return ResponseEntity.ok(stockService.ajouterStock(stockDTO));
    }

    @PutMapping("/produit/{produitId}")
    public ResponseEntity<Stock> mettreAJourStock(
            @PathVariable Long produitId,
            @RequestParam Integer quantite) {
        return ResponseEntity.ok(stockService.mettreAJourStock(produitId, quantite));
    }

    @GetMapping("/produit/{produitId}")
    public ResponseEntity<Stock> getStockByProduit(@PathVariable Long produitId) {
        return ResponseEntity.ok(stockService.getStockByProduit(produitId));
    }

    @GetMapping("/faible")
    public ResponseEntity<List<Stock>> getStockFaible() {
        return ResponseEntity.ok(stockService.getStockFaible());
    }

    @GetMapping("/epuise")
    public ResponseEntity<List<Stock>> getStockEpuise() {
        return ResponseEntity.ok(stockService.getStockEpuise());
    }

    @GetMapping("/a-reapprovisionner")
    public ResponseEntity<List<Stock>> getStockAReapprovisionner() {
        return ResponseEntity.ok(stockService.getStockAReapprovisionner());
    }

    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        return ResponseEntity.ok(stockService.getStatistiquesStock());
    }
}