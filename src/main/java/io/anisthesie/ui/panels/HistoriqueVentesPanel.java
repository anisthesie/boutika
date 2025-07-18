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

    private final int DAYS_PER_BATCH = 10;
    private List<LocalDate> allDates;
    private Map<LocalDate, List<VenteDTO>> ventesParJour;
    private int loadedDaysCount = 0;

    public HistoriqueVentesPanel(VenteDAO venteDAO, VenteProduitsDAO venteProduitsDAO) {
        this.venteDAO = venteDAO;
        this.venteProduitsDAO = venteProduitsDAO;

        // Set the main panel layout and background (dark mode)
        setLayout(new BorderLayout());

        // Create the content panel with improved styling (dark mode)
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 45, 15));

        // Create a scroll pane with improved styling
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.setBackground(contentPanel.getBackground());
        scrollPane.getViewport().setBackground(contentPanel.getBackground());

        // Add components to the main panel
        add(scrollPane, BorderLayout.CENTER);

        // Create the "Load More" button with improved styling and rounded corners
        loadMoreButton = new JButton("Voir plus de jours");
        loadMoreButton.setFont(new Font("SansSerif", Font.BOLD, 28));
        loadMoreButton.setFocusPainted(false);
        loadMoreButton.setBackground(new Color(46, 204, 113));
        loadMoreButton.setForeground(Color.WHITE);
        loadMoreButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Make the button rounded
        loadMoreButton.addActionListener(e -> loadMoreDays());

        // Create the "Close" button with improved styling and rounded corners
        closeButton = new JButton("Fermer l'historique");
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 22));
        closeButton.setFocusPainted(false);
        closeButton.setBackground(new Color(204, 46, 51));
        closeButton.setForeground(Color.WHITE);
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Make the button rounded
        closeButton.addActionListener(e -> closeSelf());


        // Initialize the panel
        refresh();
    }

    private void refresh() {
        // Clear the content panel and reset the counter
        contentPanel.removeAll();
        loadedDaysCount = 0;

        try {
            // Show a loading message while fetching data (dark mode)
            JLabel loadingLabel = new JLabel("Chargement de l'historique des ventes...");
            loadingLabel.setFont(new Font("SansSerif", Font.ITALIC, 16));
            loadingLabel.setForeground(Color.WHITE);
            loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(Box.createVerticalStrut(20));
            contentPanel.add(loadingLabel);
            contentPanel.add(Box.createVerticalStrut(20));

            // Update the UI to show the loading message
            revalidate();
            repaint();

            // Fetch the sales data from the database
            ventesParJour = venteDAO.getAllVentesGroupedByDate();
            allDates = new ArrayList<>(ventesParJour.keySet());
            allDates.sort(Comparator.reverseOrder());

            // Clear the loading message
            contentPanel.removeAll();

            // If there are no sales, show a message (dark mode)
            if (allDates.isEmpty()) {
                JLabel emptyLabel = new JLabel("Aucune vente trouvée dans l'historique");
                emptyLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
                emptyLabel.setForeground(Color.WHITE);
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(Box.createVerticalStrut(30));
                contentPanel.add(emptyLabel);
                contentPanel.add(Box.createVerticalStrut(30));
            } else {
                // Load the first batch of days
                loadMoreDays();
            }
        } catch (Exception e) {
            e.printStackTrace();

            // Show an error message in the panel (dark mode)
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

            // Show a dialog with the error
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement de l'historique: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }

        revalidate();
        repaint();
    }

    private void loadMoreDays() {
        int toIndex = Math.min(loadedDaysCount + DAYS_PER_BATCH, allDates.size());

        // Add a separator before new days if not the first batch
        if (loadedDaysCount > 0)
            contentPanel.add(Box.createVerticalStrut(15));


        // Add each day panel
        for (int i = loadedDaysCount; i < toIndex; i++) {
            LocalDate jour = allDates.get(i);
            List<VenteDTO> ventesDuJour = ventesParJour.get(jour);
            JPanel jourPanel = createJourPanel(jour, ventesDuJour);
            contentPanel.add(jourPanel);
        }

        loadedDaysCount = toIndex;


        // Create a panel for the buttons with rounded corners (dark mode)
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add vertical spacing
        contentPanel.add(Box.createVerticalStrut(20));

        // Add "Load More" button if there are more days to load
        if (loadedDaysCount < allDates.size()) {
            // Add a label showing how many more days are available (dark mode)
            JLabel remainingLabel = new JLabel((allDates.size() - loadedDaysCount) + " jours supplémentaires disponibles");
            remainingLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
            remainingLabel.setForeground(Color.WHITE);
            remainingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttonsPanel.add(remainingLabel);
            buttonsPanel.add(Box.createVerticalStrut(5));

            // Add the "Load More" button
            buttonsPanel.add(loadMoreButton);
            buttonsPanel.add(Box.createVerticalStrut(15));
        }
        else loadMoreButton.setVisible(false);

        // Add the "Close" button
        buttonsPanel.add(closeButton);

        // Add the buttons panel to the content panel
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

    private JPanel createJourPanel(LocalDate date, List<VenteDTO> ventesDuJour) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(60, 60, 70)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Format the date in a more readable format
        String formattedDate = date.getDayOfWeek().toString().substring(0, 1) +
                date.getDayOfWeek().toString().substring(1).toLowerCase() +
                " " + date.getDayOfMonth() + " " +
                date.getMonth().toString().substring(0, 1) +
                date.getMonth().toString().substring(1).toLowerCase() +
                " " + date.getYear();

        // Create a panel for the header with rounded corners (dark mode)
        RoundedPanel headerPanel = new RoundedPanel(new BorderLayout(), 30, 30, new Color(52, 152, 219));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create the toggle button with an expandable indicator and rounded corners
        JButton toggleBtn = new JButton("▶ " + formattedDate + " (" + ventesDuJour.size() + " ventes)");
        toggleBtn.setFont(new Font("SansSerif", Font.BOLD, 22));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBorderPainted(true);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setForeground(Color.WHITE);
        toggleBtn.setHorizontalAlignment(SwingConstants.LEFT);
        toggleBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        // Make the button rounded

        // Add the header panel to the main panel
        headerPanel.add(toggleBtn, BorderLayout.CENTER);

        // Create a panel for the sales with a dark background
        JPanel ventesPanel = new JPanel();
        ventesPanel.setLayout(new BoxLayout(ventesPanel, BoxLayout.Y_AXIS));
        // ventesPanel.setBackground(new Color(45, 45, 55));
        ventesPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        ventesPanel.setVisible(false);

        // Add action listener to toggle visibility and change the indicator
        toggleBtn.addActionListener((ActionEvent e) -> {
            boolean isVisible = !ventesPanel.isVisible();
            ventesPanel.setVisible(isVisible);
            // Change the arrow icon based on expanded/collapsed state
            toggleBtn.setText((isVisible ? "▼ " : "▶ ") + formattedDate + " (" + ventesDuJour.size() + " ventes)");
            revalidate();
            repaint();
        });

        // Add sales to the panel
        for (VenteDTO vente : ventesDuJour) {
            ventesPanel.add(createVentePanel(vente));
        }

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(ventesPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createVentePanel(VenteDTO vente) {
        RoundedPanel panel = new RoundedPanel(new BorderLayout(), 30, 30,new Color(45, 45, 55));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Format the time and total for better readability
        String time = vente.getDate().toLocalTime().toString();
        String formattedTotal = String.format("%.2f DA", vente.getTotal());

        // Create a panel for the sale header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(74, 74, 90, 255));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        // Create the title with an expandable indicator
        String title = String.format("▶ Vente #%d - %s - Total: %s",
                vente.getId(), time, formattedTotal);

        JButton detailsBtn = new JButton(title);
        detailsBtn.setFont(new Font("SansSerif", Font.PLAIN, 18));
        detailsBtn.setFocusPainted(false);
        detailsBtn.setBorderPainted(true);
        detailsBtn.setContentAreaFilled(false);
        detailsBtn.setForeground(Color.WHITE);
        detailsBtn.setHorizontalAlignment(SwingConstants.LEFT);
        detailsBtn.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        // Make the button rounded

        headerPanel.add(detailsBtn, BorderLayout.CENTER);

        // Create a panel for the products with a dark background
        JPanel produitsPanel = new JPanel();
        produitsPanel.setLayout(new BoxLayout(produitsPanel, BoxLayout.Y_AXIS));
        produitsPanel.setBackground(new Color(45, 45, 55));
        produitsPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 5));
        produitsPanel.setVisible(false);

        // Add action listener to toggle visibility and change the indicator
        detailsBtn.addActionListener((ActionEvent e) -> {
            if (!produitsPanel.isVisible()) {
                produitsPanel.removeAll();
                try {
                    List<VenteProduitsDTO> produits = venteProduitsDAO.getProduitsParVente(vente.getId());

                    // Add a label showing the number of products (dark mode)
                    JLabel countLabel = new JLabel(produits.size() + " produit(s)");
                    countLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
                    countLabel.setForeground(new Color(200, 200, 220));
                    countLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 0));
                    produitsPanel.add(countLabel);

                    // Add each product
                    for (VenteProduitsDTO vp : produits) {
                        produitsPanel.add(createProduitPanel(vp));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            boolean isVisible = !produitsPanel.isVisible();
            produitsPanel.setVisible(isVisible);

            // Change the arrow icon based on expanded/collapsed state
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
        panel.setBackground(new Color(55, 55, 65));

        // Calculate the total price for this product
        double totalPrice = vp.getQuantite() * vp.getPrixUnitaire();

        // Create a main panel with FlowLayout for better spacing
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(panel.getBackground());

        // Create the product name label
        JLabel nameLabel = new JLabel(vp.getNomProduit());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLabel.setForeground(new Color(220, 220, 230));

        // Create a panel for the details (quantity and price)
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        detailsPanel.setBackground(panel.getBackground());

        // Add quantity information
        JLabel quantityLabel = new JLabel("Quantité: " + vp.getQuantite());
        quantityLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        quantityLabel.setForeground(Color.WHITE);

        // Add unit price information
        JLabel priceLabel = new JLabel("Prix unitaire: " + String.format("%.2f DA", vp.getPrixUnitaire()));
        priceLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        priceLabel.setForeground(Color.WHITE);

        // Add total price information
        JLabel totalLabel = new JLabel("Total: " + String.format("%.2f DA", totalPrice));
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        totalLabel.setForeground(Color.WHITE);

        // Add all components to the panels
        detailsPanel.add(quantityLabel);
        detailsPanel.add(priceLabel);
        detailsPanel.add(totalLabel);

        contentPanel.add(nameLabel, BorderLayout.NORTH);
        contentPanel.add(detailsPanel, BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }
}