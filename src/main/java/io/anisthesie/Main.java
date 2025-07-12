package io.anisthesie;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatSystemProperties;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import io.anisthesie.ui.MainWindow;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        FlatMacDarkLaf.setup();

        SwingUtilities.invokeLater(() -> {
            try {
                Connection conn = DriverManager.getConnection("jdbc:sqlite:gestion.db");
                new MainWindow(conn).setVisible(true);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Erreur SQL : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

    }

}
