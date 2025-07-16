package io.anisthesie.db.dto;

import java.time.LocalDateTime;

public class VenteDTO {
    private int id;
    private LocalDateTime date;
    private double total;

    public VenteDTO(int id, LocalDateTime date, double total) {
        this.id = id;
        this.date = date;
        this.total = total;
    }

    public VenteDTO(LocalDateTime date, double total) {
        this.date = date;
        this.total = total;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public double getTotal() {
        return total;
    }

    public void setId(int id) {
        this.id = id;
    }
}
