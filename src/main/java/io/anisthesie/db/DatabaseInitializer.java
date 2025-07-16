package io.anisthesie.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {

            // Table ventes
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS ventes (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            date TEXT NOT NULL,
                            total REAL NOT NULL
                        );
                    """);

            // Table vente_produits
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS vente_produits (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            vente_id INTEGER NOT NULL,
                            produit_id INTEGER NOT NULL,
                            quantite INTEGER NOT NULL,
                            prix_unitaire REAL NOT NULL,
                            FOREIGN KEY (vente_id) REFERENCES ventes(id),
                            FOREIGN KEY (produit_id) REFERENCES produit(id)
                        );
                    """);

            // Table produits
            stmt.execute("CREATE TABLE IF NOT EXISTS produit (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nom TEXT, prix REAL, stock INTEGER)");

        }
    }
}

