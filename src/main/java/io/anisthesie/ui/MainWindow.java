package io.anisthesie.ui;

import io.anisthesie.db.ProduitDAO;
import io.anisthesie.db.ProduitDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public class MainWindow extends JFrame{


    private final ProduitDAO produitDAO;
    private final DefaultListModel<ProduitDTO> model = new DefaultListModel<>();
    private final JList<ProduitDTO> list = new JList<>(model);

    public MainWindow(Connection conn) throws SQLException {
        this.produitDAO = new ProduitDAO(conn);
        setTitle("Gestion Commerciale");
        setSize(1200, 600);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout());
        initUI();
        loadProduits();
    }

    private void initUI() {
        JButton btnAdd = new JButton("Ajouter Produit");
        btnAdd.addActionListener(this::onAdd);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        panel.add(btnAdd, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    private void onAdd(ActionEvent e) {
        String nom = JOptionPane.showInputDialog(this, "Nom produit:");
        if (nom == null || nom.isBlank()) return;

        double prix = Double.parseDouble(JOptionPane.showInputDialog(this, "Prix:", 0));
        int stock = Integer.parseInt(JOptionPane.showInputDialog(this, "Stock:", 0));

        ProduitDTO p = new ProduitDTO(nom, prix, stock);
        try {
            produitDAO.insertProduit(p);
            model.addElement(p);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur d'insertion", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProduits() throws SQLException {
        List<ProduitDTO> produits = produitDAO.getAllProduits();
        produits.forEach(model::addElement);
    }


}
