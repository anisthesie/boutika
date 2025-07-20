package io.anisthesie.ui.panels;

import io.anisthesie.db.dao.VenteDAO;
import io.anisthesie.db.dao.VenteProduitsDAO;
import io.anisthesie.db.dto.VenteDTO;
import io.anisthesie.db.dto.VenteProduitsDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;

public class VentesJourPanel extends JPanel {
    private final VenteDAO venteDAO;
    private final VenteProduitsDAO venteProduitsDAO;
    private final JButton excelButton;
    private final JButton closeButton;
    private final JPanel contentPanel;

    public VentesJourPanel(VenteDAO venteDAO, VenteProduitsDAO venteProduitsDAO) {
        this.venteDAO = venteDAO;
        this.venteProduitsDAO = venteProduitsDAO;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.BLACK);
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0)); // padding bas
        excelButton = new JButton("Générer Excel");
        excelButton.setPreferredSize(new Dimension(260, 64));
        excelButton.setBackground(new Color(46, 204, 113));
        excelButton.setForeground(Color.WHITE);
        excelButton.setFont(new Font("SansSerif", Font.BOLD, 26));
        closeButton = new JButton("Fermer");
        closeButton.setPreferredSize(new Dimension(200, 64));
        closeButton.setBackground(new Color(231, 76, 60));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 26));
        buttonPanel.add(excelButton);
        buttonPanel.add(Box.createHorizontalStrut(30));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        excelButton.addActionListener(this::genererExcel);
        closeButton.addActionListener(e -> closeSelf());

        chargerVentes();
    }

    private void chargerVentes() {
        contentPanel.removeAll();
        try {
            List<VenteDTO> ventes = venteDAO.getVentesDuJour();
            if (ventes.isEmpty()) {
                JLabel label = new JLabel("Aucune vente aujourd'hui.");
                label.setFont(new Font("SansSerif", Font.BOLD, 20));
                label.setForeground(new Color(180, 180, 180));
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(Box.createVerticalStrut(40));
                contentPanel.add(label);
            } else {
                DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
                double totalJour = 0;
                for (VenteDTO vente : ventes) {
                    JPanel ventePanel = new JPanel();
                    ventePanel.setLayout(new BoxLayout(ventePanel, BoxLayout.Y_AXIS));
                    ventePanel.setBackground(new Color(25, 25, 25));
                    ventePanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                        BorderFactory.createEmptyBorder(12, 18, 12, 18)));
                    ventePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    JLabel header = new JLabel(String.format("Vente #%d   |   %s   |   Total: %.2f DA",
                            vente.getId(),
                            vente.getDate().toLocalTime().format(timeFmt),
                            vente.getTotal()));
                    header.setFont(new Font("SansSerif", Font.BOLD, 17));
                    header.setForeground(new Color(41, 128, 185));
                    ventePanel.add(header);
                    ventePanel.add(Box.createVerticalStrut(6));
                    try {
                        List<VenteProduitsDTO> produits = venteProduitsDAO.getProduitsParVente(vente.getId());
                        for (VenteProduitsDTO produit : produits) {
                            JPanel prodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
                            prodPanel.setBackground(new Color(35, 35, 35));
                            prodPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                            JLabel nom = new JLabel(produit.getNomProduit());
                            nom.setFont(new Font("SansSerif", Font.PLAIN, 15));
                            nom.setForeground(new Color(220, 220, 220));
                            nom.setPreferredSize(new Dimension(180, 22));
                            JLabel qte = new JLabel("Qté: " + produit.getQuantite());
                            qte.setFont(new Font("SansSerif", Font.PLAIN, 14));
                            qte.setForeground(new Color(200, 200, 200));
                            JLabel pu = new JLabel(String.format("PU: %.2f DA", produit.getPrixUnitaire()));
                            pu.setFont(new Font("SansSerif", Font.PLAIN, 14));
                            pu.setForeground(new Color(200, 200, 200));
                            JLabel total = new JLabel(String.format("Total: %.2f DA", produit.getQuantite() * produit.getPrixUnitaire()));
                            total.setFont(new Font("SansSerif", Font.BOLD, 18));
                            total.setForeground(new Color(39, 174, 96));
                            prodPanel.add(nom);
                            prodPanel.add(qte);
                            prodPanel.add(pu);
                            prodPanel.add(total);
                            ventePanel.add(prodPanel);
                        }
                    } catch (Exception ex) {
                        JLabel err = new JLabel("Erreur chargement produits: " + ex.getMessage());
                        err.setForeground(Color.RED);
                        ventePanel.add(err);
                    }
                    contentPanel.add(ventePanel);
                    contentPanel.add(Box.createVerticalStrut(14));
                    totalJour += vente.getTotal();
                }
                JLabel totalLabel = new JLabel(String.format("Total de la journée : %.2f DA", totalJour));
                totalLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
                totalLabel.setForeground(new Color(39, 174, 96));
                totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(Box.createVerticalStrut(10));
                contentPanel.add(totalLabel);
            }
        } catch (Exception e) {
            JLabel label = new JLabel("Erreur lors du chargement des ventes: " + e.getMessage());
            label.setForeground(Color.RED);
            label.setFont(new Font("SansSerif", Font.BOLD, 16));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(label);
        }
        revalidate();
        repaint();
    }

    private void genererExcel(ActionEvent e) {
        try {
            List<VenteDTO> ventes = venteDAO.getVentesDuJour();
            if (ventes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucune vente à exporter.", "Export Excel", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Enregistrer sous");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Fichier Excel (*.xlsx)", "xlsx"));
            if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".xlsx")) filePath += ".xlsx";

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Ventes du jour");
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
                    List<VenteProduitsDTO> produits = venteProduitsDAO.getProduitsParVente(vente.getId());
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
            }
            JOptionPane.showMessageDialog(this, "Export Excel réussi !", "Export Excel", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erreur d'écriture du fichier : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'export : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void closeSelf() {
        Container parent = this.getParent();
        while (parent != null && !(parent instanceof JTabbedPane)) {
            parent = parent.getParent();
        }
        if (parent instanceof JTabbedPane tabbedPane) {
            int index = tabbedPane.indexOfComponent(this);
            if (index != -1) tabbedPane.remove(index);
        }
    }
}
