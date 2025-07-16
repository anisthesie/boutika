package io.anisthesie.db.service;

import io.anisthesie.db.dao.VenteDAO;
import io.anisthesie.db.dao.VenteProduitsDAO;
import io.anisthesie.db.dto.ProduitDTO;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

public class VenteService {

    private final Connection conn;
    private final VenteDAO venteDAO;
    private final VenteProduitsDAO venteProduitDAO;

    public VenteService(Connection conn) {
        this.conn = conn;
        this.venteDAO = new VenteDAO(conn);
        this.venteProduitDAO = new VenteProduitsDAO(conn);
    }

    /**
     * Enregistre une vente complète (ligne vente + lignes vente_produits)
     *
     * @param panier Map des produits avec leur quantité
     * @return id de la vente enregistrée
     * @throws SQLException si une erreur survient pendant l'enregistrement
     */
    public int validerVente(Map<ProduitDTO, Integer> panier) throws SQLException {
        if (panier == null || panier.isEmpty()) {
            throw new IllegalArgumentException("Le panier est vide.");
        }

        try {
            conn.setAutoCommit(false); // Début de la transaction

            LocalDateTime now = LocalDateTime.now();
            double total = panier.entrySet().stream()
                    .mapToDouble(e -> e.getKey().getPrix() * e.getValue())
                    .sum();

            // Enregistrer la vente et obtenir l'ID
            int venteId = venteDAO.enregistrerVente(now, total);

            // Enregistrer les produits associés à la vente
            for (Map.Entry<ProduitDTO, Integer> entry : panier.entrySet()) {
                ProduitDTO produit = entry.getKey();
                int quantite = entry.getValue();
                venteProduitDAO.enregistrerVenteProduit(venteId, produit.getId(), quantite, produit.getPrix());
            }

            conn.commit(); // Tout s'est bien passé
            return venteId;

        } catch (SQLException e) {
            conn.rollback(); // Annule tout en cas d'erreur
            throw e;
        } finally {
            conn.setAutoCommit(true); // Revenir au comportement normal
        }
    }
}
