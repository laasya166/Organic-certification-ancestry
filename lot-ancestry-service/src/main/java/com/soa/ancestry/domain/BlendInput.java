package com.soa.ancestry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "blend_inputs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_blend_input", columnNames = {"blend_id", "input_lot_id"})
        }
)
public class BlendInput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blend_id", nullable = false)
    private Long blendId;

    @Column(name = "input_lot_id", nullable = false)
    private String inputLotId;

    public BlendInput() {}

    public BlendInput(Long blendId, String inputLotId) {
        this.blendId = blendId;
        this.inputLotId = inputLotId;
    }

    public BlendInput(Long id, Long blendId, String inputLotId) {
        this.id = id;
        this.blendId = blendId;
        this.inputLotId = inputLotId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBlendId() {
        return blendId;
    }

    public void setBlendId(Long blendId) {
        this.blendId = blendId;
    }

    public String getInputLotId() {
        return inputLotId;
    }

    public void setInputLotId(String inputLotId) {
        this.inputLotId = inputLotId;
    }
}
