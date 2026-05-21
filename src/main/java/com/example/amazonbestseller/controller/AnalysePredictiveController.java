package com.example.amazonbestseller.controller;

import com.example.amazonbestseller.service.AnalysePredictiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analyse-predictive")
@RequiredArgsConstructor
public class AnalysePredictiveController {

    private final AnalysePredictiveService predictiveService;

    @GetMapping("/futurs-bestsellers")
    public ResponseEntity<List<Map<String, Object>>> getFutursBestsellers() {
        return ResponseEntity.ok(predictiveService.identifierFutursBestSellers());
    }

    @GetMapping("/position-future/{id}")
    public ResponseEntity<Map<String, Object>> getPredictionRang(@PathVariable Long id) {
        // Default to 30 days prediction
        return ResponseEntity.ok(predictiveService.predirePositionFuture(id, 30));
    }

    @GetMapping("/prix-ideal/{id}")
    public ResponseEntity<Map<String, Object>> getRecommandationPrix(@PathVariable Long id) {
        return ResponseEntity.ok(predictiveService.recommanderPrixIdeal(id));
    }
}
