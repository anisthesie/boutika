package io.anisthesie.ui;

import io.anisthesie.db.ProduitDAO;
import io.anisthesie.db.ProduitDTO;
import io.anisthesie.ui.layout.WrapLayout;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class TransactionPanel extends JPanel {

    private final ProduitDAO produitDAO;
    private final JPanel panierPanel = new JPanel();
    private final JLabel totalLabel = new JLabel("Total : 0 DA");
    private final Map<ProduitDTO, Integer> panier = new LinkedHashMap<>();

    public TransactionPanel(ProduitDAO produitDAO) {
        this.produitDAO = produitDAO;
        setLayout(new BorderLayout());
        initUI();
    }

    private void initUI() {
        // ========== Zone gauche : Produits groupés par catégorie ==========
        JPanel produitsPanel = new JPanel();
        produitsPanel.setLayout(new BoxLayout(produitsPanel, BoxLayout.Y_AXIS));
        JScrollPane produitsScroll = new JScrollPane(produitsPanel);
        produitsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        produitsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


        try {
            List<ProduitDTO> produits = produitDAO.getAllProduits();
            Map<String, List<ProduitDTO>> produitsParCategorie = new TreeMap<>();
            for (ProduitDTO p : produits) {
                String cat = p.getCategorie() != null ? p.getCategorie() : "Divers";
                produitsParCategorie.computeIfAbsent(cat, k -> new ArrayList<>()).add(p);
            }

            for (Map.Entry<String, List<ProduitDTO>> entry : produitsParCategorie.entrySet()) {
                JLabel lblCat = new JLabel(entry.getKey());
                lblCat.setFont(new Font("SansSerif", Font.BOLD, 18));
                produitsPanel.add(lblCat);

                JPanel catPanel = new JPanel(new WrapLayout());
                for (ProduitDTO produit : entry.getValue()) {
                    JButton btn = new JButton("<html><center>" + produit.getNom() + "<br/>" + produit.getPrix() + " DA");
                    btn.setPreferredSize(new Dimension(150, 80));
                    btn.setBackground(new Color(52, 152, 219));
                    btn.setForeground(Color.WHITE);
                    btn.setFont(new Font("SansSerif", Font.BOLD, 14));
                    btn.addActionListener(e -> ajouterAuPanier(produit));
                    catPanel.add(btn);
                }
                produitsPanel.add(catPanel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // ========== Zone droite : Panier avec actions ==========
        JPanel droitePanel = new JPanel(new BorderLayout());

        panierPanel.setLayout(new BoxLayout(panierPanel, BoxLayout.Y_AXIS));
        JScrollPane panierScroll = new JScrollPane(panierPanel);
        panierScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panierScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton btnValider = new JButton("💰 Valider la vente");
        btnValider.setBackground(new Color(46, 204, 113));
        btnValider.setForeground(Color.WHITE);
        btnValider.setFont(new Font("SansSerif", Font.BOLD, 20));
        btnValider.setFocusPainted(false);
        btnValider.setPreferredSize(new Dimension(380, 60));
        btnValider.addActionListener(e -> validerVente());

        JButton btnAnnuler = new JButton("❌ Annuler la vente");
        btnAnnuler.setBackground(new Color(204, 46, 51));
        btnAnnuler.setForeground(Color.WHITE);
        btnAnnuler.setFont(new Font("SansSerif", Font.BOLD, 20));
        btnAnnuler.setFocusPainted(false);
        btnAnnuler.setPreferredSize(new Dimension(380, 60));
        btnAnnuler.addActionListener(e -> annulerVente());


        JPanel basPanier = new JPanel(new BorderLayout());
        basPanier.add(totalLabel, BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        actionsPanel.add(btnValider);
        actionsPanel.add(btnAnnuler);
        basPanier.add(actionsPanel, BorderLayout.SOUTH);

        droitePanel.add(panierScroll, BorderLayout.CENTER);
        droitePanel.add(basPanier, BorderLayout.SOUTH);

        // ========== Division avec JSplitPane ==========
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, produitsScroll, droitePanel);
        splitPane.setResizeWeight(0.7); // 70% pour la gauche
        splitPane.setDividerSize(3);    // taille de la bordure de séparation
        splitPane.setContinuousLayout(true);

        add(splitPane, BorderLayout.CENTER);
    }

    private void annulerVente() {
    }

    private void ajouterAuPanier(ProduitDTO produit) {
        panier.put(produit, panier.getOrDefault(produit, 0) + 1);
        rafraichirPanier();
    }

    private void rafraichirPanier() {
        panierPanel.removeAll();

        for (Map.Entry<ProduitDTO, Integer> entry : panier.entrySet()) {
            ProduitDTO produit = entry.getKey();
            int quantite = entry.getValue();

            JPanel ligne = new JPanel();
            ligne.setLayout(new BoxLayout(ligne, BoxLayout.X_AXIS));
            ligne.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            ligne.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            // === Nom du produit (gauche)
            JLabel lblNom = new JLabel(tronquerTexte(produit.getNom(), 40));
            lblNom.setToolTipText(produit.getNom());
            lblNom.setFont(new Font("SansSerif", Font.PLAIN, 16));
            lblNom.setPreferredSize(new Dimension(200, 30));
            lblNom.setAlignmentY(Component.CENTER_ALIGNMENT);

            // === Spinner quantité
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(quantite, 1, 1000, 1));
            spinner.setPreferredSize(new Dimension(80, 30));
            spinner.setMaximumSize(new Dimension(80, 30));
            spinner.setAlignmentY(Component.CENTER_ALIGNMENT);
            spinner.addChangeListener(e -> {
                int q = (Integer) spinner.getValue();
                panier.put(produit, q);
                mettreAJourTotal();
            });

            // === Bouton supprimer
            JButton btnSupprimer = new JButton("❌");
            btnSupprimer.setPreferredSize(new Dimension(50, 30));
            btnSupprimer.setMaximumSize(new Dimension(50, 30));
            btnSupprimer.setFont(new Font("SansSerif", Font.PLAIN, 14));
            btnSupprimer.setAlignmentY(Component.CENTER_ALIGNMENT);
            btnSupprimer.addActionListener(e -> {
                panier.remove(produit);
                rafraichirPanier();
            });

            // === Espace extensible entre le label et la zone de droite
            ligne.add(lblNom);
            ligne.add(Box.createHorizontalGlue());
            ligne.add(spinner);
            ligne.add(Box.createRigidArea(new Dimension(5, 0)));
            ligne.add(btnSupprimer);

            panierPanel.add(ligne);
        }

        panierPanel.revalidate();
        panierPanel.repaint();
        mettreAJourTotal();
    }

    private String tronquerTexte(String texte, int max) {
        return texte.length() > max ? texte.substring(0, max - 3) + "..." : texte;
    }


    private void mettreAJourTotal() {
        double total = panier.entrySet().stream().mapToDouble(e -> e.getKey().getPrix() * e.getValue()).sum();
        totalLabel.setText("Total : " + String.format("%.2f", total) + " DA");
    }

    private void validerVente() {
        if (panier.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucun produit dans le panier.", "Erreur", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // TODO: enregistrer la vente dans la base de données (vente + vente_produits)
        JOptionPane.showMessageDialog(this, "Vente enregistrée avec succès !");
        panier.clear();
        rafraichirPanier();
    }
}
