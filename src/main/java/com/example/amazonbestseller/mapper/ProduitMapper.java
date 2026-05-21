package com.example.amazonbestseller.mapper;

import com.example.amazonbestseller.dto.ProduitDTO;
import com.example.amazonbestseller.entity.Produit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProduitMapper {

    ProduitMapper INSTANCE = Mappers.getMapper(ProduitMapper.class);

    @Mapping(target = "quantiteStock", source = "stock.quantite")
    @Mapping(target = "seuilMin", source = "stock.seuilMin")
    @Mapping(target = "emplacement", source = "stock.emplacement")
    @Mapping(target = "vendeurId", source = "magasin.idVendeur")
    ProduitDTO toDTO(Produit produit);

    @Mapping(target = "stock", ignore = true) // Stock is handled separately or in logic
    @Mapping(target = "id", ignore = true) // ID is auto-generated
    @Mapping(target = "dateAjout", ignore = true)
    @Mapping(target = "magasin", ignore = true)
    @Mapping(target = "lignesCommande", ignore = true)
    @Mapping(target = "ventes", ignore = true)
    @Mapping(target = "avis", ignore = true)
    @Mapping(target = "nombreVendeurs", ignore = true)
    // Removed ignore for urlImage and urlProduit to allow mapping
    Produit toEntity(ProduitDTO produitDTO);
}
