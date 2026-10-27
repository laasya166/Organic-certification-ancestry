package com.soa.ancestry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "blends")
public class Blend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String outputLotId;

    @Column(nullable = false)
    private LocalDate processedAt;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private Instant createdAt;

    public Blend() {}

    public Blend(String outputLotId, LocalDate processedAt, String createdBy) {
        this.outputLotId = outputLotId;
        this.processedAt = processedAt;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    public Blend(Long id, String outputLotId, LocalDate processedAt, String createdBy, Instant createdAt) {
        this.id = id;
        this.outputLotId = outputLotId;
        this.processedAt = processedAt;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOutputLotId() {
        return outputLotId;
    }

    public void setOutputLotId(String outputLotId) {
        this.outputLotId = outputLotId;
    }

    public LocalDate getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDate processedAt) {
        this.processedAt = processedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
