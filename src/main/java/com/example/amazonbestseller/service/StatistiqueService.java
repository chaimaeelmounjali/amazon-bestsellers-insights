// File: StatistiqueService.java
package com.example.amazonbestseller.service;

import com.example.amazonbestseller.entity.Statistique;
import com.example.amazonbestseller.repository.StatistiqueRepository;
import com.example.amazonbestseller.repository.VenteRepository;
import com.example.amazonbestseller.repository.CommandeRepository;
import com.example.amazonbestseller.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatistiqueService {

    private final StatistiqueRepository statistiqueRepository;
    private final VenteRepository venteRepository;
    private final CommandeRepository commandeRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional
    public Statistique genererStatistiquesPeriode(String periode, LocalDate debut, LocalDate fin) {
        LocalDateTime debutDateTime = debut.atStartOfDay();
        LocalDateTime finDateTime = fin.atTime(23, 59, 59);

        Statistique stat = new Statistique();
        stat.setTypeStatistique("VENTES");
        stat.setPeriode(periode);
        stat.setDateDebut(debut);
        stat.setDateFin(fin);
        stat.setDateCalcul(LocalDateTime.now());

        // Calculer les métriques
        BigDecimal revenu = venteRepository.findTotalSalesBetweenDates(debutDateTime, finDateTime);
        Long nbVentes = venteRepository.countSalesBetweenDates(debutDateTime, finDateTime);
        BigDecimal panierMoyen = venteRepository.findAverageSaleAmountBetweenDates(debutDateTime, finDateTime);

        stat.setRevenuTotal(revenu != null ? revenu : BigDecimal.ZERO);
        stat.setProduitsVendus(nbVentes != null ? nbVentes.intValue() : 0);
        stat.setPanierMoyen(panierMoyen != null ? panierMoyen : BigDecimal.ZERO);
        stat.setEstValide(true);

        return statistiqueRepository.save(stat);
    }

    public Optional<Statistique> getDernieresStatistiquesDashboard() {
        return statistiqueRepository.findLatestDashboardStatistics();
    }

    public List<Statistique> getStatistiquesPeriode(String type, LocalDate debut, LocalDate fin) {
        return statistiqueRepository.findValidStatisticsBetweenDates(type, debut, fin);
    }

    public List<Object[]> getTendancesRevenu() {
        return statistiqueRepository.findRevenueTrends();
    }
}