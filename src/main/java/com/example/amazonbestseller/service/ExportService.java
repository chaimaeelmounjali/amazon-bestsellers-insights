// File: ExportService.java
package com.example.amazonbestseller.service;

import com.example.amazonbestseller.entity.*;
import com.example.amazonbestseller.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final ProduitRepository produitRepository;
    private final VenteRepository venteRepository;
    private final CommandeRepository commandeRepository;
    private final AvisRepository avisRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ========== EXPORT PRODUITS ==========

    public String exporterProduitsCSV() {
        StringBuilder csv = new StringBuilder();

        // En-têtes
        csv.append("ASIN,Nom,Catégorie,Prix,Note,Nombre d'Avis,Rang,Disponible,Date Ajout\n");

        // Données
        List<Object[]> produits = produitRepository.findAllForExport();
        for (Object[] produit : produits) {
            csv.append(escapeCsv(produit[0])).append(","); // ASIN
            csv.append(escapeCsv(produit[1])).append(","); // Nom
            csv.append(escapeCsv(produit[2])).append(","); // Catégorie
            csv.append(produit[3]).append(","); // Prix
            csv.append(produit[4]).append(","); // Note
            csv.append(produit[5]).append(","); // Nombre d'avis
            csv.append(produit[6]).append(","); // Rang
            csv.append(produit[7]).append(","); // Disponible
            csv.append(produit[8]).append("\n"); // Date ajout
        }

        return csv.toString();
    }

    public byte[] exporterProduitsExcel() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Produits");

            // Style pour l'en-tête
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // En-têtes
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ASIN", "Nom", "Catégorie", "Prix", "Note", "Nombre d'Avis", "Rang", "Disponible", "Date Ajout"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Données
            List<Object[]> produits = produitRepository.findAllForExport();
            int rowNum = 1;

            for (Object[] produit : produits) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue((String) produit[0]); // ASIN
                row.createCell(1).setCellValue((String) produit[1]); // Nom
                row.createCell(2).setCellValue((String) produit[2]); // Catégorie
                row.createCell(3).setCellValue(((BigDecimal) produit[3]).doubleValue()); // Prix
                row.createCell(4).setCellValue(((BigDecimal) produit[4]).doubleValue()); // Note
                row.createCell(5).setCellValue((Integer) produit[5]); // Nombre d'avis
                row.createCell(6).setCellValue((Integer) produit[6]); // Rang
                row.createCell(7).setCellValue((Boolean) produit[7] ? "Oui" : "Non"); // Disponible
                row.createCell(8).setCellValue(((LocalDateTime) produit[8]).format(DATE_FORMATTER)); // Date
            }

            // Auto-dimensionner les colonnes
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Écrire dans un ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // ========== EXPORT VENTES ==========

    public String exporterVentesCSV(LocalDateTime dateDebut, LocalDateTime dateFin) {
        StringBuilder csv = new StringBuilder();

        // En-têtes
        csv.append("ID,Date,Produit,Acheteur,Vendeur,Quantité,Prix Unitaire,Montant Total,Commission,Statut\n");

        // Données
        List<Vente> ventes = venteRepository.findByDateVenteBetween(dateDebut, dateFin);

        for (Vente vente : ventes) {
            csv.append(vente.getId()).append(",");
            csv.append(vente.getDateVente().format(DATE_FORMATTER)).append(",");
            csv.append(escapeCsv(vente.getProduit().getNom())).append(",");
            csv.append(escapeCsv(vente.getAcheteur() != null ? vente.getAcheteur().getNomUtilisateur() : "N/A")).append(",");
            csv.append(escapeCsv(vente.getVendeur() != null ? vente.getVendeur().getNomUtilisateur() : "N/A")).append(",");
            csv.append(vente.getQuantite()).append(",");
            csv.append(vente.getPrixUnitaire()).append(",");
            csv.append(vente.getMontantTotal()).append(",");
            csv.append(vente.getCommission()).append(",");
            csv.append(vente.getStatut()).append("\n");
        }

        return csv.toString();
    }

    public byte[] exporterVentesExcel(LocalDateTime dateDebut, LocalDateTime dateFin) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Ventes");

            // Style pour l'en-tête
            CellStyle headerStyle = createHeaderStyle(workbook);

            // En-têtes
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Date", "Produit", "Acheteur", "Vendeur", "Quantité",
                    "Prix Unitaire", "Montant Total", "Commission", "Statut"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Données
            List<Vente> ventes = venteRepository.findByDateVenteBetween(dateDebut, dateFin);
            int rowNum = 1;

            for (Vente vente : ventes) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(vente.getId());
                row.createCell(1).setCellValue(vente.getDateVente().format(DATE_FORMATTER));
                row.createCell(2).setCellValue(vente.getProduit().getNom());
                row.createCell(3).setCellValue(vente.getAcheteur() != null ? vente.getAcheteur().getNomUtilisateur() : "N/A");
                row.createCell(4).setCellValue(vente.getVendeur() != null ? vente.getVendeur().getNomUtilisateur() : "N/A");
                row.createCell(5).setCellValue(vente.getQuantite());
                row.createCell(6).setCellValue(vente.getPrixUnitaire().doubleValue());
                row.createCell(7).setCellValue(vente.getMontantTotal().doubleValue());
                row.createCell(8).setCellValue(vente.getCommission().doubleValue());
                row.createCell(9).setCellValue(vente.getStatut().toString());
            }

            // Auto-dimensionner les colonnes
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // ========== EXPORT COMMANDES ==========

    public String exporterCommandesCSV(LocalDateTime dateDebut, LocalDateTime dateFin) {
        StringBuilder csv = new StringBuilder();

        csv.append("ID,Date,Acheteur,Vendeur,Montant Total,Méthode Paiement,Statut\n");

        List<Commande> commandes = commandeRepository.findByDateCommandeBetween(dateDebut, dateFin);

        for (Commande commande : commandes) {
            csv.append(commande.getId()).append(",");
            csv.append(commande.getDateCommande().format(DATE_FORMATTER)).append(",");
            csv.append(escapeCsv(commande.getAcheteur().getNomUtilisateur())).append(",");
            csv.append(escapeCsv(commande.getVendeur() != null ? commande.getVendeur().getNomUtilisateur() : "N/A")).append(",");
            csv.append(commande.getMontantTotal()).append(",");
            csv.append(escapeCsv(commande.getMethodePaiement())).append(",");
            csv.append(commande.getStatut()).append("\n");
        }

        return csv.toString();
    }

    // ========== EXPORT AVIS ==========

    public String exporterAvisCSV(Long produitId) {
        StringBuilder csv = new StringBuilder();

        csv.append("ID,Produit,Acheteur,Note,Commentaire,Vérifié,Utiles,Date\n");

        List<Avis> avisList = produitId != null
                ? avisRepository.findByProduitId(produitId)
                : avisRepository.findAll();

        for (Avis avis : avisList) {
            csv.append(avis.getId()).append(",");
            csv.append(escapeCsv(avis.getProduit().getNom())).append(",");
            csv.append(escapeCsv(avis.getAcheteur().getNomUtilisateur())).append(",");
            csv.append(avis.getNote()).append(",");
            csv.append(escapeCsv(avis.getCommentaire())).append(",");
            csv.append(avis.getEstVerifie() ? "Oui" : "Non").append(",");
            csv.append(avis.getNombreUtiles()).append(",");
            csv.append(avis.getDateAvis().format(DATE_FORMATTER)).append("\n");
        }

        return csv.toString();
    }

    // ========== EXPORT RAPPORT PERSONNALISÉ ==========

    public byte[] exporterRapportVentesExcel(LocalDateTime dateDebut, LocalDateTime dateFin) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {

            // Feuille 1: Résumé
            Sheet summarySheet = workbook.createSheet("Résumé");
            createSummarySheet(summarySheet, workbook, dateDebut, dateFin);

            // Feuille 2: Ventes détaillées
            Sheet ventesSheet = workbook.createSheet("Ventes Détaillées");
            createVentesSheet(ventesSheet, workbook, dateDebut, dateFin);

            // Feuille 3: Top Produits
            Sheet topProduitsSheet = workbook.createSheet("Top Produits");
            createTopProduitsSheet(topProduitsSheet, workbook, dateDebut, dateFin);

            // Feuille 4: Performance Vendeurs
            Sheet vendeursSheet = workbook.createSheet("Performance Vendeurs");
            createVendeursSheet(vendeursSheet, workbook, dateDebut, dateFin);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private String escapeCsv(Object value) {
        if (value == null) return "";
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void createSummarySheet(Sheet sheet, Workbook workbook, LocalDateTime debut, LocalDateTime fin) {
        CellStyle headerStyle = createHeaderStyle(workbook);

        int rowNum = 0;
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Rapport de Ventes - Période: " + debut.format(DATE_FORMATTER) + " à " + fin.format(DATE_FORMATTER));

        rowNum++; // Ligne vide

        // Statistiques
        BigDecimal montantTotal = venteRepository.findTotalSalesBetweenDates(debut, fin);
        Long nombreVentes = venteRepository.countSalesBetweenDates(debut, fin);
        BigDecimal panierMoyen = venteRepository.findAverageSaleAmountBetweenDates(debut, fin);

        createStatRow(sheet, rowNum++, "Montant Total des Ventes", montantTotal != null ? montantTotal.toString() + " €" : "0 €");
        createStatRow(sheet, rowNum++, "Nombre de Ventes", nombreVentes != null ? nombreVentes.toString() : "0");
        createStatRow(sheet, rowNum++, "Panier Moyen", panierMoyen != null ? panierMoyen.toString() + " €" : "0 €");

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createStatRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private void createVentesSheet(Sheet sheet, Workbook workbook, LocalDateTime debut, LocalDateTime fin) {
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        String[] columns = {"Date", "Produit", "Quantité", "Montant"};

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Vente> ventes = venteRepository.findByDateVenteBetween(debut, fin);
        int rowNum = 1;

        for (Vente vente : ventes) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(vente.getDateVente().format(DATE_FORMATTER));
            row.createCell(1).setCellValue(vente.getProduit().getNom());
            row.createCell(2).setCellValue(vente.getQuantite());
            row.createCell(3).setCellValue(vente.getMontantTotal().doubleValue());
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createTopProduitsSheet(Sheet sheet, Workbook workbook, LocalDateTime debut, LocalDateTime fin) {
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        String[] columns = {"Produit", "Quantité Vendue", "Montant Total"};

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Object[]> topProduits = venteRepository.findTopSellingProductsBetweenDates(debut, fin);
        int rowNum = 1;

        for (Object[] data : topProduits) {
            Row row = sheet.createRow(rowNum++);
            Produit produit = (Produit) data[0];
            row.createCell(0).setCellValue(produit.getNom());
            row.createCell(1).setCellValue(((Number) data[1]).longValue());
            row.createCell(2).setCellValue(((BigDecimal) data[2]).doubleValue());
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createVendeursSheet(Sheet sheet, Workbook workbook, LocalDateTime debut, LocalDateTime fin) {
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        String[] columns = {"Vendeur", "Nombre de Ventes", "Montant Total", "Commission"};

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Object[]> vendeurs = venteRepository.findSellerPerformanceBetweenDates(debut, fin);
        int rowNum = 1;

        for (Object[] data : vendeurs) {
            Row row = sheet.createRow(rowNum++);
            Vendeur vendeur = (Vendeur) data[0];
            row.createCell(0).setCellValue(vendeur.getNomUtilisateur());
            row.createCell(2).setCellValue(((BigDecimal) data[1]).doubleValue());
            row.createCell(3).setCellValue(((BigDecimal) data[2]).doubleValue());
            row.createCell(1).setCellValue(((Long) data[3]).intValue());
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}