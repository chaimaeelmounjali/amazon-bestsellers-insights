package com.example.amazonbestseller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVendeurDTO {
    private PerformanceDTO performance;
    private List<ProduitDTO> topProduitsVendus;
    private List<CommandeSummaryDTO> commandesEnAttente;
    private Map<String, BigDecimal> evolutionVentes;
    private Map<String, BigDecimal> ventesParCategorie;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceDTO {
        private BigDecimal revenuTotal;
        private BigDecimal chiffreAffaireMois; // Both for frontend consistency
        private BigDecimal commissionMois;
        private Integer totalVentes;
        private Integer nombreVentesMois;
        private BigDecimal objectifVentes;
        private Double progressionObjectif;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommandeSummaryDTO {
        private Long id;
        private String dateCommande;
        private BigDecimal montantTotal;
        private String statut;
        private String acheteurNom;
    }
}
