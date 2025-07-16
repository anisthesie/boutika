package io.anisthesie.db.dao;

import io.anisthesie.db.dto.VenteDTO;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VenteDAO {

    private final Connection conn;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public VenteDAO(Connection conn) {
        this.conn = conn;
    }

    public int enregistrerVente(LocalDateTime dateVente, double montantTotal) throws SQLException {
        String sql = "INSERT INTO ventes (date, total) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, dateVente.format(formatter));
            stmt.setDouble(2, montantTotal);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
                else throw new SQLException("Échec lors de la récupération de l'ID de la vente.");
            }
        }
    }

    public Map<LocalDate, List<VenteDTO>> getAllVentesGroupedByDate() throws SQLException {
        String sql = "SELECT id, date, total FROM ventes ORDER BY date DESC";
        Map<LocalDate, List<VenteDTO>> grouped = new LinkedHashMap<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
                double total = rs.getDouble("total");

                VenteDTO vente = new VenteDTO(id, date, total);
                grouped.computeIfAbsent(date.toLocalDate(), k -> new ArrayList<>()).add(vente);
            }
        }
        return grouped;
    }
}

