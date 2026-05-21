// File: StockService.java
package com.example.amazonbestseller.service;

import com.example.amazonbestseller.dto.StockDTO;
import com.example.amazonbestseller.entity.Stock;
import com.example.amazonbestseller.entity.Produit;
import com.example.amazonbestseller.repository.StockRepository;
import com.example.amazonbestseller.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final ProduitRepository produitRepository;

    @Transactional
    public Stock ajouterStock(StockDTO stockDTO) {
        Produit produit = produitRepository.findById(stockDTO.getProduitId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        Stock stock = new Stock();
        stock.setProduit(produit);
        stock.setQuantite(stockDTO.getQuantite());
        stock.setSeuilMin(stockDTO.getSeuilMin());
        stock.setEmplacement(stockDTO.getEmplacement());
        stock.setDerniereMaj(LocalDateTime.now());

        return stockRepository.save(stock);
    }

    @Transactional
    public Stock mettreAJourStock(Long produitId, Integer nouvelleQuantite) {
        Stock stock = stockRepository.findByProduitId(produitId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));

        stock.setQuantite(nouvelleQuantite);
        stock.setDerniereMaj(LocalDateTime.now());

        return stockRepository.save(stock);
    }

    public Stock getStockByProduit(Long produitId) {
        return stockRepository.findByProduitId(produitId)
                .orElseThrow(() -> new RuntimeException("Stock non trouvé"));
    }

    public List<Stock> getStockFaible() {
        return stockRepository.findLowStock();
    }

    public List<Stock> getStockEpuise() {
        return stockRepository.findOutOfStock();
    }

    public List<Stock> getStockAReapprovisionner() {
        return stockRepository.findStockToReplenish();
    }

    public Map<String, Object> getStatistiquesStock() {
        Long total = stockRepository.findTotalStockQuantity();
        Double moyenne = stockRepository.findAverageStockQuantity();
        Long epuises = stockRepository.countOutOfStock();
        Long faibles = stockRepository.countLowStock();

        return Map.of(
                "quantiteTotale", total != null ? total : 0L,
                "quantiteMoyenne", moyenne != null ? moyenne : 0.0,
                "produitsEpuises", epuises,
                "produitsStockFaible", faibles
        );
    }
}
