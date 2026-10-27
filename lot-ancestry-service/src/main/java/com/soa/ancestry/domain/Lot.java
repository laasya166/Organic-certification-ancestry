package com.soa.ancestry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lots")
public class Lot {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private String product;

    @Column(nullable = false)
    private Double quantityKg;

    @Column(nullable = false)
    private String ownerUsername;

    public Lot() {}

    public Lot(String id, String product, Double quantityKg, String ownerUsername) {
        this.id = id;
        this.product = product;
        this.quantityKg = quantityKg;
        this.ownerUsername = ownerUsername;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Double getQuantityKg() {
        return quantityKg;
    }

    public void setQuantityKg(Double quantityKg) {
        this.quantityKg = quantityKg;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }
}
