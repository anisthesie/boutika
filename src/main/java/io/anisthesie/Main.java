package io.anisthesie;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import io.anisthesie.db.DatabaseInitializer;
import io.anisthesie.ui.DashboardWindow;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        UIManager.put("defaultFont", new Font("SansSerif", Font.PLAIN, 18)); // police générale agrandie
        FlatMacDarkLaf.setup();

        SwingUtilities.invokeLater(() -> {
            try {
                Connection conn = DriverManager.getConnection("jdbc:sqlite:gestion.db");
                DatabaseInitializer.initTables(conn);
                new DashboardWindow(conn).setVisible(true);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Erreur SQL : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

    }

}
