package io.anisthesie.ui;

import io.anisthesie.db.ProduitDAO;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;


public class DashboardWindow extends JFrame {

    private final ProduitDAO produitDAO;
    private final JLabel statusLabel = new JLabel("État : Initialisation...");
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private int transactionCounter = 1;

    public DashboardWindow(Connection conn) throws SQLException {
        this.produitDAO = new ProduitDAO(conn);
        setTitle("OptiGest - Gestion Commerciale");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        initUI();
        //loadProduits();
    }

    private void initUI()  {
        // === Créer les boutons ===

        JButton btnNew = createButton("Nouvelle Vente", new Color(46, 204, 113), 32, 240);
        JButton btnStock = createButton("Historique des ventes", new Color(52, 152, 219), 20, 60);
        JButton btnPrint = createButton("Imprimer Journée", new Color(241, 196, 15), 20, 60);
        JButton btnExport = createButton("Ajouter Stock", new Color(155, 89, 182), 20, 60);

        // === Actions ===
        btnNew.addActionListener(e -> openNewTransactionTab());
        btnStock.addActionListener(e -> JOptionPane.showMessageDialog(this, "TODO: Ajouter stock"));
        btnPrint.addActionListener(e -> JOptionPane.showMessageDialog(this, "TODO: Imprimer journée"));
        btnExport.addActionListener(e -> JOptionPane.showMessageDialog(this, "TODO: Exporter recettes"));

        // === Panel des boutons (à gauche) ===
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        buttonPanel.add(btnNew);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(btnStock);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnPrint);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(btnExport);

        // === Zone centrale avec onglets (à droite) ===
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 18));

        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buttonPanel, tabbedPane);
        horizontalSplit.setResizeWeight(0); // 30% / 70%
        horizontalSplit.setDividerSize(4);

        // === Layout principal ===
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(horizontalSplit, BorderLayout.CENTER);
    }

    private JButton createButton(String text, Color color, int fontSize, int height) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(0, height));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return btn;
    }

    private void openNewTransactionTab() {
        String tabName = "Client " + transactionCounter++;
        JPanel panel = new TransactionPanel(this.produitDAO); // classe à créer
        tabbedPane.addTab(tabName, panel);
        tabbedPane.setSelectedComponent(panel);
    }
}

