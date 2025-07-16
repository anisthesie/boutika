package io.anisthesie.db.dto;

public class VenteProduitsDTO {

    private String nomProduit;
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

    public VenteProduitsDTO() {
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

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public void setProduitId(int produitId) {
        this.produitId = produitId;
    }

    public void setQuantite(int quantite) {

        this.quantite = quantite;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }
}

