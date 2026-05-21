// File: PaginationDTO.java
package com.example.amazonbestseller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationDTO<T> {
    private List<T> contenu;
    private Integer pageActuelle;
    private Integer taillePagee;
    private Long totalElements;
    private Integer totalPages;
    private Boolean dernierePage;
}