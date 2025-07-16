package io.anisthesie.db.dao;


import io.anisthesie.db.dto.ProduitDTO;
import io.anisthesie.db.dto.VenteProduitsDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class VenteProduitsDAO {

    private final Connection conn;

    public VenteProduitsDAO(Connection conn) {
        this.conn = conn;
    }



    public void enregistrerVenteProduit(int venteId, int produitId, int quantite, double prixUnitaire) throws SQLException {
        String sql = "INSERT INTO vente_produits (vente_id, produit_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, venteId);
            stmt.setInt(2, produitId);
            stmt.setInt(3, quantite);
            stmt.setDouble(4, prixUnitaire);
            stmt.executeUpdate();
        }
    }

    public Connection getConnection() {
        return conn;
    }
}

