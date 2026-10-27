package com.soa.ancestry.dto;

import java.time.LocalDate;
import java.util.List;

public record CreateBlendRequest(
        List<String> inputLotIds,
        LocalDate processedAt,
        OutputLotDto outputLot,
        String outputLotId
) {
    public record OutputLotDto(
            String id,
            String product,
            Double quantityKg
    ) {}
}
