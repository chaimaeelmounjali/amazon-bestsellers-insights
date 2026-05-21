package com.example.amazonbestseller.service;

import com.example.amazonbestseller.dto.ProduitDTO;
import com.example.amazonbestseller.entity.Produit;
import com.example.amazonbestseller.entity.Stock;
import com.example.amazonbestseller.exception.ResourceNotFoundException;
import com.example.amazonbestseller.mapper.ProduitMapper;
import com.example.amazonbestseller.repository.ProduitRepository;
import com.example.amazonbestseller.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final StockRepository stockRepository;
    private final com.example.amazonbestseller.repository.MagasinRepository magasinRepository;
    private final com.example.amazonbestseller.repository.CommandeRepository commandeRepository;
    private final ProduitMapper produitMapper;

    // ========== GESTION DES PRODUITS ==========

    @Transactional
    public ProduitDTO ajouterProduit(ProduitDTO produitDTO) {
        if (produitDTO.getAsin() == null || produitDTO.getAsin().trim().isEmpty()) {
            throw new IllegalArgumentException("L'ASIN est obligatoire");
        }
        // Vérifier si l'ASIN existe déjà
        if (produitRepository.findByAsin(produitDTO.getAsin()).isPresent()) {
            throw new IllegalArgumentException(
                    "Un produit avec cet ASIN existe déjà (ASIN: " + produitDTO.getAsin() + ")");
        }

        Produit produit = produitMapper.toEntity(produitDTO);
        produit.setEstDisponible(true);
        produit.setDateAjout(LocalDateTime.now());

        // Garantir un urlProduit non nul
        if (produit.getUrlProduit() == null || produit.getUrlProduit().isEmpty()) {
            produit.setUrlProduit("https://www.amazon.com/dp/" + produit.getAsin());
        }

        // Lier au magasin du vendeur si spécifié
        if (produitDTO.getVendeurId() != null) {
            java.util.Optional<com.example.amazonbestseller.entity.Magasin> magasinOpt = magasinRepository
                    .findByIdVendeur(produitDTO.getVendeurId())
                    .stream().findFirst();

            if (magasinOpt.isPresent()) {
                produit.setMagasin(magasinOpt.get());
            } else {
                // FALLBACK: Créer automatiquement un magasin si le vendeur n'en a pas
                com.example.amazonbestseller.entity.Magasin nouveauMagasin = new com.example.amazonbestseller.entity.Magasin();
                nouveauMagasin.setNom("Boutique de l'utilisateur " + produitDTO.getVendeurId());
                nouveauMagasin.setIdVendeur(produitDTO.getVendeurId());
                nouveauMagasin.setAdresse("Adresse à définir");
                nouveauMagasin.setEmail("store" + produitDTO.getVendeurId() + "@amazon.com");
                nouveauMagasin.setNote(BigDecimal.valueOf(5.0)); // Encourageant pour le début

                magasinRepository.save(nouveauMagasin);
                produit.setMagasin(nouveauMagasin);

                // Mettre à jour le vendeur pour lier ce magasin (ref circulaire)
                // Note: Idéalement, on utiliserait le VendeurRepository ici, mais pour éviter
                // d'injecter trop de dépendances circulaires, on laisse la BDD gérer ou on le
                // fera plus tard.
                // Dans ce schéma, c'est Magasin qui a id_vendeur, et Vendeur qui a id_magasin.
                // La mise à jour de Vendeur est importante pour la cohérence parfaite, mais
                // pour l'instant, lier le produit au magasin suffit pour qu'il apparaisse.
            }
        }

        // Valeurs par défaut si null
        if (produit.getNote() == null)
            produit.setNote(BigDecimal.ZERO);
        if (produit.getNombreAvis() == null)
            produit.setNombreAvis(0);
        if (produit.getRang() == null)
            produit.setRang(0);
        if (produit.getNombreVendeurs() == null)
            produit.setNombreVendeurs(1);

        Produit savedProduit = produitRepository.save(produit);

        // Créer un stock par défaut si spécifié
        if (produitDTO.getQuantiteStock() != null) {
            Stock stock = new Stock();
            stock.setProduit(savedProduit);
            stock.setQuantite(produitDTO.getQuantiteStock());
            stock.setSeuilMin(produitDTO.getSeuilMin() != null ? produitDTO.getSeuilMin() : 10);
            stock.setEmplacement(produitDTO.getEmplacement());
            stock.setDerniereMaj(LocalDateTime.now());
            stockRepository.save(stock);

            // Re-fetch to have stock link
            // Or manually set strictly for DTO returning if needed,
            // but mapstruct handles lazy loading carefully.
            // Here we rely on mapper.
            savedProduit.setStock(stock);
        }

        return produitMapper.toDTO(savedProduit);
    }

    @Transactional
    public ProduitDTO modifierProduit(Long id, ProduitDTO produitDTO) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id));

        // Mise à jour manuelle des champs modifiables
        if (produitDTO.getNom() != null)
            produit.setNom(produitDTO.getNom());
        if (produitDTO.getDescription() != null)
            produit.setDescription(produitDTO.getDescription());
        if (produitDTO.getCategorie() != null)
            produit.setCategorie(produitDTO.getCategorie());
        if (produitDTO.getPrix() != null)
            produit.setPrix(produitDTO.getPrix());
        if (produitDTO.getNote() != null)
            produit.setNote(produitDTO.getNote());
        if (produitDTO.getNombreAvis() != null)
            produit.setNombreAvis(produitDTO.getNombreAvis());
        if (produitDTO.getRang() != null)
            produit.setRang(produitDTO.getRang());
        if (produitDTO.getEstDisponible() != null)
            produit.setEstDisponible(produitDTO.getEstDisponible());
        if (produitDTO.getUrlImage() != null)
            produit.setUrlImage(produitDTO.getUrlImage());

        // Mise à jour du stock
        if (produitDTO.getQuantiteStock() != null) {
            Stock stock = produit.getStock();
            if (stock == null) {
                stock = new Stock();
                stock.setProduit(produit);
                produit.setStock(stock);
            }
            stock.setQuantite(produitDTO.getQuantiteStock());
            if (produitDTO.getSeuilMin() != null) {
                stock.setSeuilMin(produitDTO.getSeuilMin());
            }
            if (produitDTO.getEmplacement() != null) {
                stock.setEmplacement(produitDTO.getEmplacement());
            }
            stock.setDerniereMaj(LocalDateTime.now());
            stockRepository.save(stock);
        }

        Produit updatedProduit = produitRepository.save(produit);
        return produitMapper.toDTO(updatedProduit);
    }

    @Transactional
    public void supprimerProduit(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id));

        // Soft delete
        produit.setEstDisponible(false);
        produitRepository.save(produit);
    }

    @Transactional
    public void supprimerProduitDefinitivement(Long id) {
        if (!produitRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id);
        }
        produitRepository.deleteById(id);
    }

    // ========== RECHERCHE ET FILTRAGE ==========

    @Transactional(readOnly = true)
    public ProduitDTO getProduitById(Long id) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + id));
        return produitMapper.toDTO(produit);
    }

    public ProduitDTO getProduitByAsin(String asin) {
        Produit produit = produitRepository.findByAsin(asin)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ASIN: " + asin));
        return produitMapper.toDTO(produit);
    }

    @Transactional(readOnly = true)
    public List<ProduitDTO> getTousProduits() {
        return produitRepository.findAll().stream()
                .map(produitMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProduitDTO> getProduitsDisponibles() {
        return produitRepository.findByEstDisponibleTrue().stream()
                .map(produitMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProduitDTO> rechercherProduits(String keyword) {
        return produitRepository.searchByKeyword(keyword).stream()
                .map(produitMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProduitDTO> filtrerProduits(String categorie, BigDecimal minPrix,
            BigDecimal maxPrix, BigDecimal minNote,
            Integer minAvis) {
        return produitRepository.findWithFilters(categorie, minPrix, maxPrix, minNote, minAvis, null).stream()
                .map(produitMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProduitDTO> getProduitsByCategorie(String categorie) {
        return produitRepository.findByCategorie(categorie).stream()
                .map(produitMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProduitDTO> getProduitsByVendeur(Long idVendeur) {
        System.out.println(">>> [DEBUG] Fetching products for seller ID: " + idVendeur);

        // Try fallback if traditional repository query fails
        List<com.example.amazonbestseller.entity.Produit> produits = produitRepository
                .findByMagasinIdVendeur(idVendeur);

        if (produits.isEmpty()) {
            System.out.println(">>> [DEBUG] Traditional query empty, trying via Magasin search...");
            magasinRepository.findByIdVendeur(idVendeur).stream().findFirst().ifPresent(mag -> {
                System.out.println(">>> [DEBUG] Found Magasin: " + mag.getNom() + " (ID: " + mag.getId() + ")");
                List<Produit> fallbackProds = produitRepository.findByMagasinId(mag.getId());
                produits.addAll(fallbackProds);
            });
        }

        System.out.println(">>> [DEBUG] Found total " + produits.size() + " products for seller " + idVendeur);
        return produits.stream()
                .map(produitMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ========== TOP PRODUITS ==========

    @Transactional(readOnly = true)
    public List<ProduitDTO> getTopProduits(int limit) {
        return produitRepository.findTopProduits(org.springframework.data.domain.PageRequest.of(0, limit)).stream()
                .map(produitMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProduitDTO> getTopProduitsParNote(BigDecimal minNote, int limit) {
        return produitRepository
                .findTopRatedProduits(minNote, org.springframework.data.domain.PageRequest.of(0, limit)).stream()
                .map(produitMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProduitDTO> getProduitsPlusCommentes(int minReviews, int limit) {
        return produitRepository
                .findMostReviewedProduits(minReviews, org.springframework.data.domain.PageRequest.of(0, limit)).stream()
                .map(produitMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ========== STATISTIQUES DASHBOARD ==========

    public Map<String, Object> getStatistiquesDashboard() {
        Long totalProduits = produitRepository.count();
        Long produitsDisponibles = produitRepository.countAvailableProduits();
        Long nombreCategories = produitRepository.countDistinctCategories();
        BigDecimal prixMoyen = produitRepository.findAveragePrice();
        BigDecimal noteMoyenne = produitRepository.findAverageRating();

        System.out.println("DEBUG: getStatistiquesDashboard called");
        System.out.println("DEBUG: totalProduits = " + totalProduits);
        System.out.println("DEBUG: produitsDisponibles = " + produitsDisponibles);
        System.out.println("DEBUG: nombreCategories = " + nombreCategories);

        return Map.of(
                "totalProduits", totalProduits,
                "produitsDisponibles", produitsDisponibles,
                "nombreCategories", nombreCategories,
                "prixMoyen", prixMoyen != null ? prixMoyen : BigDecimal.ZERO,
                "noteMoyenne", noteMoyenne != null ? noteMoyenne : BigDecimal.ZERO);
    }

    // ========== ANALYSE PAR CATÉGORIE ==========

    @Transactional(readOnly = true)
    public Map<String, Object> getAnalyseCategorie(String categorie) {
        System.out.println("DEBUG: getAnalyseCategorie called with categorie = '" + categorie + "'");

        if (categorie == null || categorie.trim().isEmpty()) {
            System.out.println("DEBUG: Category is null or empty, returning empty map");
            return createEmptyAnalyseResponse();
        }

        List<Produit> produits = produitRepository.findByCategorieIgnoreCase(categorie.trim());
        System.out.println("DEBUG: Found " + produits.size() + " products for category '" + categorie + "'");

        if (produits.isEmpty()) {
            System.out.println("DEBUG: No products found for category '" + categorie + "', returning empty response");
            return createEmptyAnalyseResponse();
        }

        System.out.println(
                "DEBUG: Calculating prixMoyen for category '" + categorie + "' with " + produits.size() + " products");

        BigDecimal totalPrix = BigDecimal.ZERO;
        int countWithPrice = 0;

        for (Produit p : produits) {
            if (p.getPrix() != null) {
                totalPrix = totalPrix.add(p.getPrix());
                countWithPrice++;
            }
        }

        BigDecimal prixMoyen = countWithPrice > 0
                ? totalPrix.divide(BigDecimal.valueOf(countWithPrice), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        System.out
                .println("DEBUG: prixMoyen = " + prixMoyen + " (based on " + countWithPrice + " products with price)");

        Produit produitMieuxNote = produits.stream()
                .filter(p -> p.getNote() != null)
                .max((p1, p2) -> p1.getNote().compareTo(p2.getNote()))
                .orElse(null);

        Produit produitPlusVendu = produits.stream()
                .filter(p -> p.getRang() != null)
                .min((p1, p2) -> Integer.compare(p1.getRang(), p2.getRang()))
                .orElse(null);

        System.out.println("DEBUG: Mieux note: " + (produitMieuxNote != null ? produitMieuxNote.getNom() : "aucun"));
        System.out.println("DEBUG: Plus vendu: " + (produitPlusVendu != null ? produitPlusVendu.getNom() : "aucun"));

        long[] distributionNotes = new long[5];
        for (Produit p : produits) {
            if (p.getNote() != null) {
                int star = p.getNote().intValue();
                if (star >= 1 && star <= 5) {
                    distributionNotes[star - 1]++;
                } else if (star > 5) {
                    distributionNotes[4]++;
                }
            }
        }

        System.out.println("DEBUG: Note distribution calculated");

        List<ProduitDTO> top5Produits = produits.stream()
                .filter(p -> p.getRang() != null)
                .sorted(java.util.Comparator.comparingInt(Produit::getRang))
                .limit(5)
                .map(produitMapper::toDTO)
                .collect(Collectors.toList());

        System.out.println("DEBUG: Returning final analysis with " + resultSize(top5Produits) + " top products");

        // Use HashMap instead of Map.of() to allow null values
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("nombreProduits", produits.size());
        result.put("prixMoyen", prixMoyen);
        result.put("produitMieuxNote", produitMieuxNote != null ? produitMapper.toDTO(produitMieuxNote) : null);
        result.put("produitPlusVendu", produitPlusVendu != null ? produitMapper.toDTO(produitPlusVendu) : null);
        result.put("distributionNotes", distributionNotes);
        result.put("top5Produits", top5Produits);

        return result;
    }

    private int resultSize(List<?> list) {
        return list != null ? list.size() : 0;
    }

    private Map<String, Object> createEmptyAnalyseResponse() {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("nombreProduits", 0);
        result.put("prixMoyen", BigDecimal.ZERO);
        result.put("produitMieuxNote", null);
        result.put("produitPlusVendu", null);
        result.put("distributionNotes", new long[] { 0, 0, 0, 0, 0 });
        result.put("top5Produits", java.util.Collections.emptyList());
        return result;
    }

    // ========== SELLER DASHBOARD DATA ==========

    public Map<String, Object> getSellerDashboardData(Long sellerId) {
        Map<String, Object> dashboard = new java.util.HashMap<>();

        // Get seller's store
        com.example.amazonbestseller.entity.Magasin magasin = magasinRepository
                .findByIdVendeur(sellerId)
                .stream().findFirst()
                .orElse(null);

        if (magasin == null) {
            // Return empty dashboard if no store found
            return Map.of(
                    "performance", Map.of(
                            "chiffreAffaireMois", BigDecimal.ZERO,
                            "nombreVentesMois", 0),
                    "commandesEnAttente", java.util.List.of(),
                    "evolutionVentes", java.util.Map.of());
        }

        // Calculate date range (current month)
        LocalDateTime debutMois = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime maintenant = LocalDateTime.now();

        // Get all orders for products in this store
        java.util.List<com.example.amazonbestseller.entity.Produit> produitsMagasin = produitRepository
                .findByMagasinId(magasin.getId());
        java.util.List<Long> produitIds = produitsMagasin.stream()
                .map(com.example.amazonbestseller.entity.Produit::getId).collect(Collectors.toList());

        // Query commandes that contain products from this store
        java.util.List<com.example.amazonbestseller.entity.Commande> toutesCommandes = new java.util.ArrayList<>();
        if (!produitIds.isEmpty()) {
            // Get all commandes using injected repository
            java.util.List<com.example.amazonbestseller.entity.Commande> allCommandes = commandeRepository.findAll();

            // Filter commandes that have products from this magasin
            for (com.example.amazonbestseller.entity.Commande cmd : allCommandes) {
                if (cmd.getLignesCommande() != null) {
                    boolean hasProductFromStore = cmd.getLignesCommande().stream()
                            .anyMatch(ligne -> produitIds.contains(ligne.getProduit().getId()));
                    if (hasProductFromStore) {
                        toutesCommandes.add(cmd);
                    }
                }
            }
        }

        // Filter for current month - more inclusive check
        java.util.List<com.example.amazonbestseller.entity.Commande> commandesMois = toutesCommandes.stream()
                .filter(c -> !c.getDateCommande().isBefore(debutMois) && !c.getDateCommande().isAfter(maintenant))
                .collect(Collectors.toList());

        // Calculate revenue
        BigDecimal chiffreAffaireMois = commandesMois.stream()
                .map(com.example.amazonbestseller.entity.Commande::getMontantTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Performance stats
        dashboard.put("performance", Map.of(
                "chiffreAffaireMois", chiffreAffaireMois,
                "nombreVentesMois", commandesMois.size()));

        // Pending orders
        java.util.List<com.example.amazonbestseller.entity.Commande> commandesEnAttente = toutesCommandes.stream()
                .filter(c -> c.getStatut() == com.example.amazonbestseller.entity.Commande.Statut.EN_ATTENTE ||
                        c.getStatut() == com.example.amazonbestseller.entity.Commande.Statut.CONFIRMEE)
                .sorted((c1, c2) -> c1.getDateCommande().compareTo(c2.getDateCommande()))
                .limit(10)
                .collect(Collectors.toList());

        dashboard.put("commandesEnAttente", commandesEnAttente);

        // Sales evolution (last 7 days)
        java.util.Map<String, BigDecimal> evolutionVentes = new java.util.LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime debut = maintenant.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime fin = maintenant.minusDays(i).withHour(23).withMinute(59).withSecond(59);

            BigDecimal totalJour = toutesCommandes.stream()
                    .filter(c -> c.getDateCommande().isAfter(debut) && c.getDateCommande().isBefore(fin))
                    .map(com.example.amazonbestseller.entity.Commande::getMontantTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            evolutionVentes.put(debut.toLocalDate().toString(), totalJour);
        }

        dashboard.put("evolutionVentes", evolutionVentes);

        return dashboard;
    }

    // ========== GESTION DU STOCK ==========

    public List<ProduitDTO> getProduitsRuptureStock() {
        return produitRepository.findOutOfStockProduits().stream()
                .map(produitMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProduitDTO> getProduitsStockFaible() {
        return produitRepository.findLowStockProduits().stream()
                .map(produitMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void mettreAJourStock(Long produitId, Integer nouvelleQuantite) {
        Stock stock = stockRepository.findByProduitId(produitId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock non trouvé pour le produit ID: " + produitId));

        stock.setQuantite(nouvelleQuantite);
        stock.setDerniereMaj(LocalDateTime.now());
        stockRepository.save(stock);

        // Mettre à jour la disponibilité du produit
        Produit produit = stock.getProduit();
        produit.setEstDisponible(nouvelleQuantite > 0);
        produitRepository.save(produit);
    }

    // ========== EXPORT DES DONNÉES ==========

    public List<Object[]> exporterDonnees() {
        return produitRepository.findAllForExport();
    }
}