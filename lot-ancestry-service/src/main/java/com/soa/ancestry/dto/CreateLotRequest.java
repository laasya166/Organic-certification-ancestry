package com.soa.ancestry.dto;

public record CreateLotRequest(
        String id,
        String product,
        Double quantityKg,
        String ownerUsername
) {}
