package io.anisthesie.db.dao;


import io.anisthesie.db.dto.VenteProduitsDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public List<VenteProduitsDTO> getProduitsParVente(int venteId) throws SQLException {
        String sql = """
        SELECT vp.produit_id, vp.quantite, vp.prix_unitaire, p.nom AS nom_produit
        FROM vente_produits vp
        JOIN produit p ON vp.produit_id = p.id
        WHERE vp.vente_id = ?
    """;

        List<VenteProduitsDTO> produits = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, venteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    VenteProduitsDTO vp = new VenteProduitsDTO();
                    vp.setProduitId(rs.getInt("produit_id"));
                    vp.setQuantite(rs.getInt("quantite"));
                    vp.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                    vp.setNomProduit(rs.getString("nom_produit"));
                    produits.add(vp);
                }
            }
        }

        return produits;
    }



}

