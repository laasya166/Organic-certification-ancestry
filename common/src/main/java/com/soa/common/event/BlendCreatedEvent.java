package com.soa.common.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * CONTRACT: BlendCreated v1. Published by lot-ancestry-service, consumed by blend-validation-service.
 * Kafka key = aggregateId (the blendId). Changing a field name breaks the contract tests ON PURPOSE.
 */
public record BlendCreatedEvent(
        UUID eventId,
        String aggregateId,
        int schemaVersion,
        Instant occurredAt,
        String correlationId,
        LocalDate processedAt,
        String outputLotId,
        List<String> inputLotIds
) {
    public static final int SCHEMA_VERSION = 1;
}
