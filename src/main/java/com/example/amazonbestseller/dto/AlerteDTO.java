// File: AlerteDTO.java
package com.example.amazonbestseller.dto;

import com.example.amazonbestseller.entity.Alerte.Priorite;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlerteDTO {
    private Long utilisateurId;
    private String type;
    private String message;
    private Priorite priorite;
}