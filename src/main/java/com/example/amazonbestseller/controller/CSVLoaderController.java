// ========== 3. CSVLoaderController.java - Endpoint Manuel ==========
package com.example.amazonbestseller.controller;

import com.example.amazonbestseller.service.CSVLoaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/csv")
@RequiredArgsConstructor

public class CSVLoaderController {

    private final CSVLoaderService csvLoaderService;

    /**
     * Endpoint pour charger manuellement le CSV
     * GET http://localhost:8080/api/csv/charger
     */
    @PostMapping("/charger")
    public ResponseEntity<Map<String, String>> chargerCSV() {
        try {
            csvLoaderService.chargerDonneesCSV();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Fichier CSV chargé avec succès"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}
