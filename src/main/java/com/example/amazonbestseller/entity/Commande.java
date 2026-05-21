package com.example.amazonbestseller.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "commande", indexes = {
        @Index(name = "idx_acheteur", columnList = "id_acheteur"),
        @Index(name = "idx_vendeur", columnList = "id_vendeur"),
        @Index(name = "idx_date", columnList = "date_commande"),
        @Index(name = "idx_statut", columnList = "statut")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_acheteur", nullable = false)
    @JsonIgnore
    private Acheteur acheteur;

    @ManyToOne
    @JoinColumn(name = "id_vendeur")
    @JsonIgnore
    private Vendeur vendeur;

    @Column(name = "date_commande")
    private LocalDateTime dateCommande = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column
    private Statut statut = Statut.EN_ATTENTE;

    @Column(name = "sous_total", precision = 10, scale = 2)
    private BigDecimal sousTotal = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal taxe = BigDecimal.ZERO;

    @Column(name = "frais_livraison", precision = 10, scale = 2)
    private BigDecimal fraisLivraison = BigDecimal.ZERO;

    @Column(name = "montant_total", precision = 10, scale = 2)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Column(name = "methode_paiement", length = 50)
    private String methodePaiement;

    @Column(name = "adresse_livraison", length = 255)
    private String adresseLivraison;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)

    @JsonIgnore
    private List<LigneCommande> lignesCommande;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Vente> ventes;

    public enum Statut {
        EN_ATTENTE("En attente"),
        CONFIRMEE("Confirmée"),
        EXPEDIEE("Expédiée"),
        LIVREE("Livrée"),
        ANNULEE("Annulée");

        private final String valeur;

        Statut(String valeur) {
            this.valeur = valeur;
        }

        public String getValeur() {
            return valeur;
        }

        @JsonCreator
        public static Statut fromString(String value) {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }

            // Normaliser la chaîne : enlever les accents, espaces, majuscules
            String normalized = value.trim()
                    .toUpperCase()
                    .replace("É", "E")
                    .replace("È", "E")
                    .replace("Ë", "E")
                    .replace("Ê", "E")
                    .replace(" ", "_");

            // Nettoyer encore plus
            normalized = normalized.replaceAll("[^A-Z_]", "");

            try {
                return Statut.valueOf(normalized);
            } catch (IllegalArgumentException e) {
                // Fallback manuel
                if (normalized.contains("EXPEDIE") || normalized.contains("EXPEDIEE")) {
                    return EXPEDIEE;
                } else if (normalized.contains("CONFIRME") || normalized.contains("CONFIRMEE")) {
                    return CONFIRMEE;
                } else if (normalized.contains("LIVRE") || normalized.contains("LIVREE")) {
                    return LIVREE;
                } else if (normalized.contains("ANNULE") || normalized.contains("ANNULEE")) {
                    return ANNULEE;
                } else if (normalized.contains("ATTENTE") || normalized.contains("EN_ATTENTE")) {
                    return EN_ATTENTE;
                }
                throw new IllegalArgumentException("Valeur de statut non reconnue: " + value);
            }
        }
    }
}