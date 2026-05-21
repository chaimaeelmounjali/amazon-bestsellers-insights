package com.example.amazonbestseller.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin", indexes = {
        @Index(name = "idx_derniere_connexion", columnList = "derniere_connexion")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Admin extends Utilisateur {

    @Column(columnDefinition = "TEXT")
    private String permissions;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;
}
