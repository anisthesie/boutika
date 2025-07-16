package io.anisthesie.db.dto;

public class VenteProduitsDTO {
    private int venteId;
    private int produitId;
    private int quantite;
    private double prixUnitaire;

    public VenteProduitsDTO(int venteId, int produitId, int quantite, double prixUnitaire) {
        this.venteId = venteId;
        this.produitId = produitId;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public int getVenteId() {
        return venteId;
    }

    public int getProduitId() {
        return produitId;
    }

    public int getQuantite() {
        return quantite;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }
}

