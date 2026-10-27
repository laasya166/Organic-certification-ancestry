package com.soa.ancestry.service;

import com.soa.ancestry.domain.Blend;
import com.soa.ancestry.domain.BlendInput;
import com.soa.ancestry.domain.Lot;
import com.soa.ancestry.dto.BlendResponse;
import com.soa.ancestry.dto.CreateBlendRequest;
import com.soa.ancestry.repository.BlendInputRepository;
import com.soa.ancestry.repository.BlendRepository;
import com.soa.ancestry.repository.LotRepository;
import com.soa.common.event.BlendCreatedEvent;
import com.soa.common.event.Topics;
import com.soa.common.web.CorrelationIdFilter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BlendService {

    private static final Logger log = LoggerFactory.getLogger(BlendService.class);

    private final LotRepository lotRepository;
    private final BlendRepository blendRepository;
    private final BlendInputRepository blendInputRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BlendService(
            LotRepository lotRepository,
            BlendRepository blendRepository,
            BlendInputRepository blendInputRepository,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.lotRepository = lotRepository;
        this.blendRepository = blendRepository;
        this.blendInputRepository = blendInputRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public BlendResponse createBlend(CreateBlendRequest request, String currentUser) {
        // 1. Reject empty or null inputLotIds with 400 Bad Request
        if (request.inputLotIds() == null || request.inputLotIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input lot list cannot be empty");
        }

        List<String> cleanInputLotIds = new ArrayList<>();
        for (String rawId : request.inputLotIds()) {
            if (rawId == null || rawId.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input lot id cannot be empty");
            }
            cleanInputLotIds.add(rawId.trim());
        }

        // 2. Reject unknown lots with 404 Not Found
        for (String inputLotId : cleanInputLotIds) {
            if (!lotRepository.existsById(inputLotId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown input lot: " + inputLotId);
            }
        }

        // 3. Determine outputLotId and create output lot entity if provided
        String outputLotId = null;
        if (request.outputLot() != null && request.outputLot().id() != null && !request.outputLot().id().isBlank()) {
            outputLotId = request.outputLot().id().trim();
            if (!lotRepository.existsById(outputLotId)) {
                Lot outputLot = new Lot(
                        outputLotId,
                        request.outputLot().product() != null ? request.outputLot().product() : "Blend Output",
                        request.outputLot().quantityKg() != null ? request.outputLot().quantityKg() : 0.0,
                        currentUser != null ? currentUser : "proc1"
                );
                lotRepository.save(outputLot);
            }
        } else if (request.outputLotId() != null && !request.outputLotId().isBlank()) {
            outputLotId = request.outputLotId().trim();
        } else {
            // Generate output lot ID if not provided
            outputLotId = "OUT-" + UUID.randomUUID().toString().substring(0, 8);
        }

        LocalDate processedAt = request.processedAt() != null ? request.processedAt() : LocalDate.now();
        String createdBy = currentUser != null ? currentUser : "proc1";

        // 4. Save Blend entity
        Blend blend = new Blend(outputLotId, processedAt, createdBy);
        Blend savedBlend = blendRepository.save(blend);

        // 5. Save BlendInput edges
        for (String inputLotId : cleanInputLotIds) {
            BlendInput blendInput = new BlendInput(savedBlend.getId(), inputLotId);
            blendInputRepository.save(blendInput);
        }

        // 6. Build BlendCreatedEvent (contract schemaVersion 1)
        UUID eventId = UUID.randomUUID();
        String aggregateId = String.valueOf(savedBlend.getId());
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        BlendCreatedEvent event = new BlendCreatedEvent(
                eventId,
                aggregateId,
                BlendCreatedEvent.SCHEMA_VERSION,
                Instant.now(),
                correlationId,
                processedAt,
                outputLotId,
                cleanInputLotIds
        );

        // 7. Publish to Kafka topic soa.s03.t08.events, key = blendId
        log.info("Publishing BlendCreatedEvent [eventId={}, aggregateId={}, correlationId={}] to topic {}",
                eventId, aggregateId, correlationId, Topics.EVENTS);
        kafkaTemplate.send(Topics.EVENTS, aggregateId, event);

        return new BlendResponse(
                savedBlend.getId(),
                outputLotId,
                processedAt,
                createdBy,
                cleanInputLotIds
        );
    }

    @Transactional(readOnly = true)
    public BlendResponse getBlend(Long id) {
        Blend blend = blendRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blend not found with id: " + id));

        List<String> inputLotIds = blendInputRepository.findByBlendId(blend.getId())
                .stream()
                .map(BlendInput::getInputLotId)
                .toList();

        return new BlendResponse(
                blend.getId(),
                blend.getOutputLotId(),
                blend.getProcessedAt(),
                blend.getCreatedBy(),
                inputLotIds
        );
    }
}
