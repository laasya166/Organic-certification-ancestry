package com.soa.ancestry.dto;

import java.time.LocalDate;
import java.util.List;

public record BlendResponse(
        Long id,
        String outputLotId,
        LocalDate processedAt,
        String createdBy,
        List<String> inputLotIds
) {}
