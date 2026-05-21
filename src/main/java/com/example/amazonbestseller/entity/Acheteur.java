package com.example.amazonbestseller.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.List;

@Entity
@Table(name = "acheteur")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Acheteur extends Utilisateur {

    @Column(name = "adresse_livraison", length = 255)
    private String adresseLivraison;

    @Column(name = "numero_telephone", length = 20)
    private String numeroTelephone;

    @OneToMany(mappedBy = "acheteur", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<Commande> commandes;

    @OneToMany(mappedBy = "acheteur", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<Vente> ventes;

    @OneToMany(mappedBy = "acheteur", cascade = CascadeType.ALL)
    @JsonIgnore
    @ToString.Exclude
    private List<Avis> avis;
}
