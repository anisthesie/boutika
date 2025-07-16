package io.anisthesie.db.dao;

import java.sql.*;
import java.time.LocalDateTime;

public class VenteDAO {

    private final Connection conn;

    public VenteDAO(Connection conn) {
        this.conn = conn;
    }

    public int enregistrerVente(LocalDateTime dateVente, double montantTotal) throws SQLException {
        String sql = "INSERT INTO ventes (date, total) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, dateVente.toString()); // ISO-8601
            stmt.setDouble(2, montantTotal);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                else throw new SQLException("Échec lors de la récupération de l'ID de la vente.");
            }
        }
    }

    public Connection getConnection() {
        return conn;
    }
}

