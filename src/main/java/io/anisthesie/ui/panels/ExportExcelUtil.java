package io.anisthesie.ui.panels;

import io.anisthesie.db.dto.VenteDTO;
import io.anisthesie.db.dto.VenteProduitsDTO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID Vente");
            header.createCell(1).setCellValue("Heure");
            header.createCell(2).setCellValue("Total Vente (DA)");
            header.createCell(3).setCellValue("Produit");
            header.createCell(4).setCellValue("Quantité");
            header.createCell(5).setCellValue("Prix Unitaire (DA)");
            header.createCell(6).setCellValue("Total Produit (DA)");
            int rowIdx = 1;
            double total = 0;
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            for (VenteDTO vente : ventes) {
                List<VenteProduitsDTO> produits = produitsFetcher.apply(vente.getId());
                boolean first = true;
                for (VenteProduitsDTO produit : produits) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(vente.getId());
                    row.createCell(1).setCellValue(vente.getDate().toLocalTime().format(timeFmt));
                    row.createCell(2).setCellValue(first ? vente.getTotal() : 0);
                    row.createCell(3).setCellValue(produit.getNomProduit());
                    row.createCell(4).setCellValue(produit.getQuantite());
                    row.createCell(5).setCellValue(produit.getPrixUnitaire());
                    row.createCell(6).setCellValue(produit.getQuantite() * produit.getPrixUnitaire());
                    first = false;
                }
                total += vente.getTotal();
            }
            Row totalRow = sheet.createRow(rowIdx);
            totalRow.createCell(1).setCellValue("Total ventes");
            totalRow.createCell(2).setCellValue(total);
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

