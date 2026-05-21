// ========== 6. DatabaseInitializer.java - Chargement Données CSV ==========
package com.example.amazonbestseller.config;

import com.example.amazonbestseller.entity.Produit;
import com.example.amazonbestseller.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;

//@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final ProduitRepository produitRepository;

    @Override
    public void run(String... args) {
        if (produitRepository.count() > 0) {
            log.info("Base de données déjà initialisée. Chargement ignoré.");
            return;
        }

        log.info("Chargement des données depuis le fichier CSV...");
        chargerDonneesCSV();
    }

    private void chargerDonneesCSV() {
        String csvFile = "src/main/resources/uploads/Amazon_Best_Seller_2021_June 2.csv";
        String line;
        String csvSplitBy = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"; // Handle commas within quoted fields
        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Skip header
            br.readLine();

            while ((line = br.readLine()) != null) {
                try {
                    String[] data = line.split(csvSplitBy, -1);

                    if (data.length < 8) {
                        log.warn("Ligne ignorée - pas assez de colonnes: " + line);
                        continue;
                    }

                    // Clean and parse the data
                    String asin = cleanString(data[0]);
                    String categorie = cleanString(data[1]);
                    String urlProduit = cleanString(data[2]); // ✅ Parse URL from column 2
                    String sellers = cleanString(data[3]);
                    String rankStr = cleanString(data[4]).replace("#", "");
                    String ratingStr = cleanString(data[5]);
                    String reviewsStr = cleanString(data[6]).replaceAll("[^0-9]", "");
                    String priceStr = data.length > 7 ? cleanString(data[7]).replace("$", "") : "0";

                    // Generate a product name (since it's not in the CSV)
                    String nom = "Produit " + asin;

                    // Parse with proper error handling
                    BigDecimal prix = parseBigDecimal(priceStr, BigDecimal.ZERO);
                    BigDecimal note = parseBigDecimal(ratingStr, BigDecimal.ZERO);
                    int nombreAvis = parseInteger(reviewsStr, 0);
                    int rang = parseInteger(rankStr, 0);
                    int nombreVendeurs = parseInteger(sellers.replaceAll("[^0-9]", ""), 1);

                    // Create and save the product
                    Produit produit = new Produit();
                    produit.setAsin(asin);
                    produit.setNom(nom);
                    produit.setCategorie(categorie);
                    produit.setUrlProduit(urlProduit); // ✅ Set the URL
                    produit.setPrix(prix);
                    produit.setNote(note);
                    produit.setNombreAvis(nombreAvis);
                    produit.setNombreVendeurs(nombreVendeurs);
                    produit.setRang(rang);
                    produit.setEstDisponible(true);
                    produit.setDateAjout(LocalDateTime.now());

                    produitRepository.save(produit);
                    count++;

                    if (count % 50 == 0) {
                        log.info("✓ {} produits chargés...", count);
                    }

                } catch (Exception e) {
                    log.error("Erreur lors du traitement de la ligne: " + line, e);
                }
            }

            log.info("✅ {} produits chargés avec succès!", count);

        } catch (Exception e) {
            log.error("❌ Erreur lors du chargement du fichier CSV", e);
        }
    }

    private String cleanString(String value) {
        if (value == null)
            return "";
        return value.trim().replace("\"", "").replace("&amp;", "&");
    }

    private BigDecimal parseBigDecimal(String value, BigDecimal defaultValue) {
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Impossible de parser le nombre: " + value + ", utilisation de la valeur par défaut: "
                    + defaultValue);
            return defaultValue;
        }
    }

    private int parseInteger(String value, int defaultValue) {
        try {
            if (value == null || value.trim().isEmpty())
                return defaultValue;
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Impossible de parser l'entier: " + value + ", utilisation de la valeur par défaut: "
                    + defaultValue);
            return defaultValue;
        }
    }
}