package io.anisthesie.db.dao;

import io.anisthesie.db.dto.ProduitDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitDAO {
    private final Connection conn;

    public ProduitDAO(Connection conn) {
        this.conn = conn;
    }

    public void insertProduit(ProduitDTO p) throws SQLException {
        String sql = "INSERT INTO produit (nom, prix, stock) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getPrix());
            ps.setInt(3, p.getStock());
            ps.executeUpdate();
        }
    }

    public List<ProduitDTO> getAllProduits() throws SQLException {
        List<ProduitDTO> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ProduitDTO p = new ProduitDTO();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setPrix(rs.getDouble("prix"));
                p.setStock(rs.getInt("stock"));
                produits.add(p);
            }
        }
        return produits;
    }


}
