package com.example.amazonbestseller.service;

import com.example.amazonbestseller.entity.Utilisateur;
import com.example.amazonbestseller.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPredictionService {

    private final UtilisateurRepository utilisateurRepository;

    public Map<String, Object> getUserPredictions() {
        List<Utilisateur> allUsers = utilisateurRepository.findAll();

        // Grouper les utilisateurs par mois de création
        Map<String, Long> usersByMonth = allUsers.stream()
                .filter(u -> u.getDateInscription() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getDateInscription().toLocalDate().withDayOfMonth(1).toString(),
                        Collectors.counting()));

        // Calculer la tendance de croissance
        List<Map.Entry<String, Long>> sortedEntries = new ArrayList<>(usersByMonth.entrySet());
        sortedEntries.sort(Map.Entry.comparingByKey());

        // Calcul de régression linéaire simple
        double[] predictions = calculateLinearRegression(sortedEntries);

        Map<String, Object> result = new HashMap<>();
        result.put("historical", usersByMonth);
        result.put("predictions", Map.of(
                "1month", Math.max(0, (int) predictions[0]),
                "3months", Math.max(0, (int) predictions[1]),
                "6months", Math.max(0, (int) predictions[2]),
                "12months", Math.max(0, (int) predictions[3])));
        result.put("growthRate", calculateGrowthRate(sortedEntries));
        result.put("totalUsers", allUsers.size());

        return result;
    }

    private double[] calculateLinearRegression(List<Map.Entry<String, Long>> data) {
        if (data == null || data.size() < 2) {
            return new double[] { 0, 0, 0, 0 };
        }

        int n = data.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = i;
            double y = data.get(i).getValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        // Calcul de la pente (m) et de l'ordonnée à l'origine (b)
        // y = mx + b
        // Prévention de la division par zéro si tous les points ont la même abscisse
        // (pas possible ici car x = i)
        // Mais par sécurité si n * sumX2 - sumX * sumX approchait 0
        double denominator = n * sumX2 - sumX * sumX;
        if (Math.abs(denominator) < 1e-10) {
            return new double[] { 0, 0, 0, 0 };
        }

        double m = (n * sumXY - sumX * sumY) / denominator;
        double b = (sumY - m * sumX) / n;

        // Prédictions pour 1, 3, 6, et 12 mois
        double[] predictions = new double[4];
        predictions[0] = Math.max(0, m * (n + 1) + b); // 1 mois
        predictions[1] = Math.max(0, m * (n + 3) + b); // 3 mois
        predictions[2] = Math.max(0, m * (n + 6) + b); // 6 mois
        predictions[3] = Math.max(0, m * (n + 12) + b); // 12 mois

        return predictions;
    }

    private double calculateGrowthRate(List<Map.Entry<String, Long>> data) {
        if (data.size() < 2) {
            return 0.0;
        }

        // Calculer le taux de croissance moyen sur les 3 derniers mois
        int recentMonths = Math.min(3, data.size());
        double totalGrowth = 0;
        int count = 0;

        for (int i = data.size() - recentMonths; i < data.size() - 1; i++) {
            long current = data.get(i + 1).getValue();
            long previous = data.get(i).getValue();
            if (previous > 0) {
                totalGrowth += ((double) (current - previous) / previous) * 100;
                count++;
            }
        }

        return count > 0 ? totalGrowth / count : 0.0;
    }
}
