package io.anisthesie.ui.panels;

import io.anisthesie.db.dao.VenteDAO;
import io.anisthesie.db.dao.VenteProduitsDAO;
import io.anisthesie.db.dto.VenteDTO;
import io.anisthesie.db.dto.VenteProduitsDTO;
import io.anisthesie.ui.panels.components.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HistoriqueVentesPanel extends JPanel {

    private final VenteDAO venteDAO;
    private final VenteProduitsDAO venteProduitsDAO;

    private final JPanel contentPanel;
    private final JButton loadMoreButton;
    private final JButton closeButton;

    private List<LocalDate> allDates;
    private Map<LocalDate, List<VenteDTO>> ventesParJour;
    private int loadedDaysCount = 0;

    private final int DAYS_PER_BATCH = 7;

    public HistoriqueVentesPanel(VenteDAO venteDAO, VenteProduitsDAO venteProduitsDAO) {
        this.venteDAO = venteDAO;
        this.venteProduitsDAO = venteProduitsDAO;

        
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 45, 15));
        contentPanel.setBackground(Color.BLACK);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(Color.BLACK);
        wrapperPanel.add(contentPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.setBackground(Color.BLACK);
        scrollPane.getViewport().setBackground(Color.BLACK);

        add(scrollPane, BorderLayout.CENTER);


        
        loadMoreButton = new JButton("Voir plus de jours");
        loadMoreButton.setFont(new Font("SansSerif", Font.BOLD, 28));
        loadMoreButton.setFocusPainted(false);
        loadMoreButton.setBackground(new Color(46, 204, 113));
        loadMoreButton.setForeground(Color.WHITE);
        loadMoreButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        loadMoreButton.addActionListener(e -> loadMoreDays());

        
        closeButton = new JButton("Fermer l'historique");
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 22));
        closeButton.setFocusPainted(false);
        closeButton.setBackground(new Color(231, 76, 60));
        closeButton.setForeground(Color.WHITE);
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        closeButton.addActionListener(e -> closeSelf());


        
        refresh();
    }

    private void refresh() {
        
        contentPanel.removeAll();
        loadedDaysCount = 0;

        try {
            
            JLabel loadingLabel = new JLabel("Chargement de l'historique des ventes...");
            loadingLabel.setFont(new Font("SansSerif", Font.ITALIC, 16));
            loadingLabel.setForeground(Color.WHITE);
            loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(Box.createVerticalStrut(20));
            contentPanel.add(loadingLabel);
            contentPanel.add(Box.createVerticalStrut(20));

            
            revalidate();
            repaint();

            
            ventesParJour = venteDAO.getAllVentesGroupedByDate();
            allDates = new ArrayList<>(ventesParJour.keySet());
            allDates.sort(Comparator.reverseOrder());

            
            contentPanel.removeAll();

            
            if (allDates.isEmpty()) {
                JLabel emptyLabel = new JLabel("Aucune vente trouvée dans l'historique");
                emptyLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
                emptyLabel.setForeground(Color.WHITE);
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(Box.createVerticalStrut(30));
                contentPanel.add(emptyLabel);
                contentPanel.add(Box.createVerticalStrut(30));
            } else {
                
                loadMoreDays();
            }
        } catch (Exception e) {
            e.printStackTrace();

            
            contentPanel.removeAll();
            JLabel errorLabel = new JLabel("Erreur lors du chargement de l'historique");
            errorLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            errorLabel.setForeground(new Color(255, 100, 100));
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(Box.createVerticalStrut(30));
            contentPanel.add(errorLabel);

            JLabel detailLabel = new JLabel(e.getMessage());
            detailLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
            detailLabel.setForeground(new Color(255, 150, 150));
            detailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(Box.createVerticalStrut(10));
            contentPanel.add(detailLabel);
            contentPanel.add(Box.createVerticalStrut(30));

            
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement de l'historique: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }

        revalidate();
        repaint();
    }

    private void loadMoreDays() {
        int toIndex = Math.min(loadedDaysCount + DAYS_PER_BATCH, allDates.size());

        
        if (loadedDaysCount > 0)
            contentPanel.add(Box.createVerticalStrut(15));


        
        for (int i = loadedDaysCount; i < toIndex; i++) {
            LocalDate jour = allDates.get(i);
            List<VenteDTO> ventesDuJour = ventesParJour.get(jour);
            JPanel jourPanel = createJourPanel(jour, ventesDuJour);
            contentPanel.add(jourPanel);
        }

        loadedDaysCount = toIndex;


        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        
        contentPanel.add(Box.createVerticalStrut(20));

        
        if (loadedDaysCount < allDates.size()) {
            
            JLabel remainingLabel = new JLabel((allDates.size() - loadedDaysCount) + " jours supplémentaires disponibles");
            remainingLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
            remainingLabel.setForeground(Color.WHITE);
            remainingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonsPanel.add(remainingLabel);
            buttonsPanel.add(Box.createVerticalStrut(5));

            
            buttonsPanel.add(loadMoreButton);
            buttonsPanel.add(Box.createVerticalStrut(15));
        } else loadMoreButton.setVisible(false);

        
        buttonsPanel.add(closeButton);

        
        contentPanel.add(buttonsPanel);

        revalidate();
        repaint();
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

    private double getTotalJournee(LocalDate date) {
        List<VenteDTO> ventes = ventesParJour.get(date);
        if (ventes == null) return 0;
        return ventes.stream().mapToDouble(VenteDTO::getTotal).sum();
    }

    private JPanel createJourPanel(LocalDate date, List<VenteDTO> ventesDuJour) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(60, 60, 70)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        
        String formattedDate = date.getDayOfWeek().toString().substring(0, 1) +
                date.getDayOfWeek().toString().substring(1).toLowerCase() +
                " " + date.getDayOfMonth() + " " +
                date.getMonth().toString().substring(0, 1) +
                date.getMonth().toString().substring(1).toLowerCase() +
                " " + date.getYear();

        
        RoundedPanel headerPanel = new RoundedPanel(new BorderLayout(), 30, 30, new Color(25, 25, 25));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        
        JButton toggleBtn = new JButton("▶ " + formattedDate + " (" + ventesDuJour.size() + " ventes)");
        toggleBtn.setFont(new Font("SansSerif", Font.BOLD, 22));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBorderPainted(true);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setForeground(Color.WHITE);
        toggleBtn.setHorizontalAlignment(SwingConstants.LEFT);
        toggleBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        headerPanel.add(toggleBtn, BorderLayout.CENTER);

        JPanel ventesPanel = new JPanel();
        ventesPanel.setLayout(new BoxLayout(ventesPanel, BoxLayout.Y_AXIS));
        ventesPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        ventesPanel.setVisible(false);

        // Total panel (hidden by défaut)
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalPanel.setBackground(Color.BLACK);
        double totalJournee = getTotalJournee(date);
        JLabel totalLabel = new JLabel(String.format("Total de la journée : %.2f DA", totalJournee));
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        totalLabel.setForeground(new Color(39, 174, 96));
        totalPanel.add(totalLabel);
        totalPanel.setVisible(false);

        toggleBtn.addActionListener((ActionEvent e) -> {
            boolean isVisible = !ventesPanel.isVisible();
            ventesPanel.setVisible(isVisible);
            totalPanel.setVisible(isVisible);
            toggleBtn.setText((isVisible ? "▼ " : "▶ ") + formattedDate + " (" + ventesDuJour.size() + " ventes)");
            revalidate();
            repaint();
        });

        for (VenteDTO vente : ventesDuJour) {
            ventesPanel.add(createVentePanel(vente));
        }

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(ventesPanel, BorderLayout.CENTER);
        panel.add(totalPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createVentePanel(VenteDTO vente) {
        RoundedPanel panel = new RoundedPanel(new BorderLayout(), 30, 30, new Color(25, 25, 25));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        
        String time = vente.getDate().toLocalTime().toString();
        String formattedTotal = String.format("%.2f DA", vente.getTotal());

        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 25));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        
        String title = String.format("▶ Vente #%d - %s - Total: %s",
                vente.getId(), time, formattedTotal);

        JButton detailsBtn = new JButton(title);
        detailsBtn.setFont(new Font("SansSerif", Font.PLAIN, 18));
        detailsBtn.setFocusPainted(false);
        detailsBtn.setBorderPainted(true);
        detailsBtn.setContentAreaFilled(false);
        detailsBtn.setForeground(new Color(113, 198, 255));
        detailsBtn.setHorizontalAlignment(SwingConstants.LEFT);
        detailsBtn.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        

        headerPanel.add(detailsBtn, BorderLayout.CENTER);

        
        JPanel produitsPanel = new JPanel();
        produitsPanel.setLayout(new BoxLayout(produitsPanel, BoxLayout.Y_AXIS));
        produitsPanel.setBackground(new Color(35, 35, 35));
        produitsPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 5));
        produitsPanel.setVisible(false);

        
        detailsBtn.addActionListener((ActionEvent e) -> {
            if (!produitsPanel.isVisible()) {
                produitsPanel.removeAll();
                try {
                    List<VenteProduitsDTO> produits = venteProduitsDAO.getProduitsParVente(vente.getId());

                    
                    JLabel countLabel = new JLabel(produits.size() + " produit(s)");
                    countLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
                    countLabel.setForeground(new Color(200, 200, 220));
                    countLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 0));
                    produitsPanel.add(countLabel);

                    
                    for (VenteProduitsDTO vp : produits) {
                        produitsPanel.add(createProduitPanel(vp));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            boolean isVisible = !produitsPanel.isVisible();
            produitsPanel.setVisible(isVisible);

            
            String updatedTitle = String.format("%s Vente #%d - %s - Total: %s",
                    (isVisible ? "▼" : "▶"), vente.getId(), time, formattedTotal);
            detailsBtn.setText(updatedTitle);

            revalidate();
            repaint();
        });

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(produitsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProduitPanel(VenteProduitsDTO vp) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(70, 70, 80)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.setBackground(new Color(35, 35, 35));

        
        double totalPrice = vp.getQuantite() * vp.getPrixUnitaire();

        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(panel.getBackground());

        
        JLabel nameLabel = new JLabel(vp.getNomProduit());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLabel.setForeground(new Color(220, 220, 220));

        
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        detailsPanel.setBackground(panel.getBackground());

        
        JLabel quantityLabel = new JLabel("Quantité: " + vp.getQuantite());
        quantityLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        quantityLabel.setForeground(new Color(200, 200, 200));

        
        JLabel priceLabel = new JLabel("Prix unitaire: " + String.format("%.2f DA", vp.getPrixUnitaire()));
        priceLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        priceLabel.setForeground(new Color(200, 200, 200));

        
        JLabel totalLabel = new JLabel("Total: " + String.format("%.2f DA", totalPrice));
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        totalLabel.setForeground(new Color(39, 174, 96));

        
        detailsPanel.add(quantityLabel);
        detailsPanel.add(priceLabel);
        detailsPanel.add(totalLabel);

        contentPanel.add(nameLabel, BorderLayout.NORTH);
        contentPanel.add(detailsPanel, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }
}

