// File: AvisService.java
package com.example.amazonbestseller.service;

import com.example.amazonbestseller.dto.AvisDTO;
import com.example.amazonbestseller.entity.Avis;
import com.example.amazonbestseller.entity.Acheteur;
import com.example.amazonbestseller.entity.Produit;
import com.example.amazonbestseller.repository.AvisRepository;
import com.example.amazonbestseller.repository.AcheteurRepository;
import com.example.amazonbestseller.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AvisService {

    private final AvisRepository avisRepository;
    private final ProduitRepository produitRepository;
    private final AcheteurRepository acheteurRepository;

    @Transactional
    public Avis ajouterAvis(AvisDTO avisDTO) {
        Produit produit = produitRepository.findById(avisDTO.getProduitId())
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        Acheteur acheteur = acheteurRepository.findById(avisDTO.getAcheteurId())
                .orElseThrow(() -> new RuntimeException("Acheteur non trouvé"));

        Avis avis = new Avis();
        avis.setProduit(produit);
        avis.setAcheteur(acheteur);
        avis.setNote(avisDTO.getNote());
        avis.setCommentaire(avisDTO.getCommentaire());
        avis.setEstVerifie(avisDTO.getEstVerifie() != null ? avisDTO.getEstVerifie() : false);
        avis.setNombreUtiles(0);
        avis.setDateAvis(LocalDateTime.now());

        return avisRepository.save(avis);
    }

    @Transactional
    public Avis modifierAvis(Long avisId, AvisDTO avisDTO) {
        Avis avis = avisRepository.findById(avisId)
                .orElseThrow(() -> new RuntimeException("Avis non trouvé"));

        if (avisDTO.getNote() != null) avis.setNote(avisDTO.getNote());
        if (avisDTO.getCommentaire() != null) avis.setCommentaire(avisDTO.getCommentaire());

        return avisRepository.save(avis);
    }

    @Transactional
    public void supprimerAvis(Long avisId) {
        avisRepository.deleteById(avisId);
    }

    @Transactional
    public Avis marquerUtile(Long avisId) {
        Avis avis = avisRepository.findById(avisId)
                .orElseThrow(() -> new RuntimeException("Avis non trouvé"));

        avis.setNombreUtiles(avis.getNombreUtiles() + 1);
        return avisRepository.save(avis);
    }

    public List<Avis> getAvisProduit(Long produitId) {
        return avisRepository.findByProduitId(produitId);
    }

    public List<Avis> getAvisAcheteur(Long acheteurId) {
        return avisRepository.findByAcheteurId(acheteurId);
    }

    public BigDecimal getNoteMoyenneProduit(Long produitId) {
        return avisRepository.findAverageRatingByProduitId(produitId);
    }

    public List<Avis> getAvisRecents(int limit) {
        return avisRepository.findRecentAvis(limit);
    }

    public List<Avis> getAvisPlusUtiles(int limit) {
        return avisRepository.findMostHelpfulAvis(limit);
    }
}