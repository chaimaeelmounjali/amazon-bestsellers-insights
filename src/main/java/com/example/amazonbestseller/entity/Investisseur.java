package com.example.amazonbestseller.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "investisseur", indexes = {
        @Index(name = "idx_montant_investissement", columnList = "montant_investissement"),
        @Index(name = "idx_roi", columnList = "roi")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Investisseur extends Utilisateur {

    @Column(name = "montant_investissement", precision = 15, scale = 2)
    private BigDecimal montantInvestissement = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal roi = BigDecimal.ZERO;
}
