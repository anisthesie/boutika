package io.anisthesie.ui.panels;

import io.anisthesie.db.dto.VenteDTO;
import io.anisthesie.db.dto.VenteProduitsDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

public class ExportExcelUtil {
    public static void exporterVentesExcel(Component parent, List<VenteDTO> ventes, Function<Integer, List<VenteProduitsDTO>> produitsFetcher, String titreFeuille, String defaultFileName) {
        if (ventes == null || ventes.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Aucune vente à exporter.", "Export Excel", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        String userHome = System.getProperty("user.home");
        File desktop = new File(userHome, "Desktop");
        fileChooser.setCurrentDirectory(desktop);
        fileChooser.setSelectedFile(new File(desktop, defaultFileName));
        fileChooser.setDialogTitle("Enregistrer sous");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Fichier Excel (*.xlsx)", "xlsx"));
        if (fileChooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;
        String filePath = fileChooser.getSelectedFile().getAbsolutePath();
        if (!filePath.endsWith(".xlsx")) filePath += ".xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(titreFeuille);
            // Style pour l'en-tête
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Style pour les cellules normales
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setAlignment(HorizontalAlignment.LEFT);

            // Style pour les totaux
            CellStyle totalStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalFont.setColor(IndexedColors.DARK_GREEN.getIndex());
            totalStyle.setFont(totalFont);
            totalStyle.setBorderBottom(BorderStyle.THIN);
            totalStyle.setBorderTop(BorderStyle.THIN);
            totalStyle.setBorderLeft(BorderStyle.THIN);
            totalStyle.setBorderRight(BorderStyle.THIN);
            totalStyle.setAlignment(HorizontalAlignment.RIGHT);

            int rowIdx = 1;
            double total = 0;
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            // Afficher la date de la journée en haut
            Row dateRow = sheet.createRow(rowIdx++);
            Cell dateLabelCell = dateRow.createCell(0);
            dateLabelCell.setCellValue("Date de la journée :");
            dateLabelCell.setCellStyle(headerStyle);
            Cell dateValueCell = dateRow.createCell(1);
            if (!ventes.isEmpty()) {
                dateValueCell.setCellValue(ventes.get(0).getDate().toLocalDate().format(dateFmt));
            }
            dateValueCell.setCellStyle(headerStyle);
            rowIdx++; // Sauter une ligne après la date
            // En-tête
            Row header = sheet.createRow(rowIdx++);
            String[] headers = {"ID Vente", "Heure", "Total Vente (DA)", "Produit", "Quantité", "Prix Unitaire (DA)", "Total Produit (DA)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            for (VenteDTO vente : ventes) {
                List<VenteProduitsDTO> produits = produitsFetcher.apply(vente.getId());
                boolean first = true;
                for (VenteProduitsDTO produit : produits) {
                    Row row = sheet.createRow(rowIdx++);
                    Cell cell0 = row.createCell(0);
                    cell0.setCellValue(vente.getId());
                    cell0.setCellStyle(cellStyle);
                    Cell cell1 = row.createCell(1);
                    cell1.setCellValue(vente.getDate().toLocalTime().format(timeFmt));
                    cell1.setCellStyle(cellStyle);
                    Cell cell2 = row.createCell(2);
                    cell2.setCellValue(first ? vente.getTotal() : 0);
                    cell2.setCellStyle(cellStyle);
                    Cell cell3 = row.createCell(3);
                    cell3.setCellValue(produit.getNomProduit());
                    cell3.setCellStyle(cellStyle);
                    Cell cell4 = row.createCell(4);
                    cell4.setCellValue(produit.getQuantite());
                    cell4.setCellStyle(cellStyle);
                    Cell cell5 = row.createCell(5);
                    cell5.setCellValue(produit.getPrixUnitaire());
                    cell5.setCellStyle(cellStyle);
                    Cell cell6 = row.createCell(6);
                    cell6.setCellValue(produit.getQuantite() * produit.getPrixUnitaire());
                    cell6.setCellStyle(cellStyle);
                    first = false;
                }
                // Ligne grise avec le total de la vente
                Row sepRow = sheet.createRow(rowIdx++);
                for (int i = 0; i <= 6; i++) {
                    Cell sepCell = sepRow.createCell(i);
                    sepCell.setCellStyle(headerStyle);
                    sepCell.setCellValue("");
                }
                Cell totalVenteLabel = sepRow.createCell(5);
                totalVenteLabel.setCellValue("Total vente");
                totalVenteLabel.setCellStyle(headerStyle);
                Font boldFont = workbook.createFont();
                boldFont.setBold(true);
                CellStyle boldStyle = workbook.createCellStyle();
                boldStyle.cloneStyleFrom(headerStyle);
                boldStyle.setFont(boldFont);
                Cell totalVenteValue = sepRow.createCell(6);
                totalVenteValue.setCellValue(vente.getTotal());
                totalVenteValue.setCellStyle(boldStyle);
                total += vente.getTotal();
            }
            Row totalRow = sheet.createRow(rowIdx);
            Cell totalLabelCell = totalRow.createCell(1);
            totalLabelCell.setCellValue("Total ventes");
            totalLabelCell.setCellStyle(totalStyle);
            Cell totalValueCell = totalRow.createCell(2);
            totalValueCell.setCellValue(total);
            totalValueCell.setCellStyle(totalStyle);
            for (int i = 0; i <= 6; i++) sheet.autoSizeColumn(i);
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "Erreur d'écriture du fichier : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Erreur lors de l'export : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(parent, "Export Excel réussi !", "Export Excel", JOptionPane.INFORMATION_MESSAGE);
    }
}
