package io.anisthesie.ui.panels;

import io.anisthesie.db.dao.VenteDAO;
import io.anisthesie.db.dao.VenteProduitsDAO;
import io.anisthesie.db.dto.VenteDTO;
import io.anisthesie.db.dto.VenteProduitsDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class HistoriqueVentesPanel extends JPanel {

    private final VenteDAO venteDAO;
    private final VenteProduitsDAO venteProduitsDAO;

    private final JPanel contentPanel;
    private final JButton loadMoreButton;
    private final JButton closeButton;

    private final int DAYS_PER_BATCH = 7;
    private List<LocalDate> allDates;
    private Map<LocalDate, List<VenteDTO>> ventesParJour;
    private int loadedDaysCount = 0;

    public HistoriqueVentesPanel(VenteDAO venteDAO, VenteProduitsDAO venteProduitsDAO) {
        this.venteDAO = venteDAO;
        this.venteProduitsDAO = venteProduitsDAO;

        setLayout(new BorderLayout());

        JButton refreshBtn = new JButton("🔄 Actualiser");
        refreshBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBackground(new Color(0x2D2D3A));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
        refreshBtn.addActionListener(e -> refresh());

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        add(refreshBtn, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Bouton "Voir plus"
        loadMoreButton = new JButton("📅 Voir plus");
        loadMoreButton.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        loadMoreButton.setFocusPainted(false);
        loadMoreButton.setBackground(new Color(0x2D2D3A));
        loadMoreButton.setForeground(Color.WHITE);
        loadMoreButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadMoreButton.addActionListener(e -> loadMoreDays());

        // Bouton "Fermer"
        closeButton = new JButton("❌ Fermer l'historique");
        closeButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        closeButton.setFocusPainted(false);
        closeButton.setBackground(new Color(0x552222));
        closeButton.setForeground(Color.WHITE);
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> closeSelf());

        refresh();
    }

    private void refresh() {
        contentPanel.removeAll();
        loadedDaysCount = 0;

        try {
            ventesParJour = venteDAO.getAllVentesGroupedByDate();
            allDates = new ArrayList<>(ventesParJour.keySet());
            allDates.sort(Comparator.reverseOrder());

            loadMoreDays();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement de l'historique.");
        }

        revalidate();
        repaint();
    }

    private void loadMoreDays() {
        int toIndex = Math.min(loadedDaysCount + DAYS_PER_BATCH, allDates.size());

        for (int i = loadedDaysCount; i < toIndex; i++) {
            LocalDate jour = allDates.get(i);
            List<VenteDTO> ventesDuJour = ventesParJour.get(jour);
            JPanel jourPanel = createJourPanel(jour, ventesDuJour);
            contentPanel.add(jourPanel);
        }

        loadedDaysCount = toIndex;

        contentPanel.remove(loadMoreButton);
        contentPanel.remove(closeButton);

        contentPanel.add(Box.createVerticalStrut(10));
        if (loadedDaysCount < allDates.size()) {
            contentPanel.add(loadMoreButton);
        }

        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(closeButton);

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
            if (index != -1)
                tabbedPane.remove(index);

        }
    }


    private JPanel createJourPanel(LocalDate date, List<VenteDTO> ventesDuJour) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton toggleBtn = new JButton("📅 " + date.toString());
        toggleBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setBackground(new Color(0x1E1E2E));
        toggleBtn.setForeground(Color.WHITE);
        toggleBtn.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel ventesPanel = new JPanel();
        ventesPanel.setLayout(new BoxLayout(ventesPanel, BoxLayout.Y_AXIS));
        ventesPanel.setVisible(false);

        toggleBtn.addActionListener((ActionEvent e) -> {
            ventesPanel.setVisible(!ventesPanel.isVisible());
            revalidate();
        });

        for (VenteDTO vente : ventesDuJour) {
            ventesPanel.add(createVentePanel(vente));
        }

        panel.add(toggleBtn, BorderLayout.NORTH);
        panel.add(ventesPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createVentePanel(VenteDTO vente) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));

        String title = String.format("🧾 Vente %d - %s - Total: %.2f DA",
                vente.getId(), vente.getDate().toLocalTime(), vente.getTotal());

        JButton detailsBtn = new JButton(title);
        detailsBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        detailsBtn.setFocusPainted(false);
        detailsBtn.setBackground(new Color(60, 60, 80));
        detailsBtn.setForeground(Color.WHITE);
        detailsBtn.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel produitsPanel = new JPanel();
        produitsPanel.setLayout(new BoxLayout(produitsPanel, BoxLayout.Y_AXIS));
        produitsPanel.setVisible(false);

        detailsBtn.addActionListener((ActionEvent e) -> {
            if (!produitsPanel.isVisible()) {
                produitsPanel.removeAll();
                try {
                    List<VenteProduitsDTO> produits = venteProduitsDAO.getProduitsParVente(vente.getId());
                    for (VenteProduitsDTO vp : produits) {
                        produitsPanel.add(createProduitPanel(vp));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            produitsPanel.setVisible(!produitsPanel.isVisible());
            revalidate();
        });

        panel.add(detailsBtn, BorderLayout.NORTH);
        panel.add(produitsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createProduitPanel(VenteProduitsDTO vp) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 40, 5, 10));

        String label = String.format("📦 %s | Quantité: %d | Prix: %.2f DA",
                vp.getNomProduit(), vp.getQuantite(), vp.getPrixUnitaire());

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        lbl.setForeground(Color.WHITE);

        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }
}
