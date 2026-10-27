package com.soa.ancestry.controller;

import com.soa.ancestry.domain.Lot;
import com.soa.ancestry.dto.CreateLotRequest;
import com.soa.ancestry.repository.LotRepository;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/lots")
public class LotController {

    private final LotRepository lotRepository;

    @Value("${app.security.enabled:false}")
    private boolean securityEnabled;

    public LotController(LotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('processor')")
    @ResponseStatus(HttpStatus.CREATED)
    public Lot createLot(@RequestBody CreateLotRequest request, Principal principal) {
        if (request.id() == null || request.id().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lot id is required");
        }
        if (request.product() == null || request.product().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is required");
        }
        if (request.quantityKg() == null || request.quantityKg() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity in kg must be positive");
        }

        String owner = resolveUser(request.ownerUsername(), principal);
        Lot lot = new Lot(
                request.id().trim(),
                request.product().trim(),
                request.quantityKg(),
                owner
        );
        return lotRepository.save(lot);
    }

    @GetMapping("/{id}")
    public Lot getLot(@PathVariable String id, Principal principal) {
        Lot lot = lotRepository.findById(id.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found with id: " + id));

        // Record-level access check: processor can only read its own lots
        if (securityEnabled && principal != null) {
            String currentUser = principal.getName();
            if (!lot.getOwnerUsername().equals(currentUser)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden: you do not own lot " + id);
            }
        }

        return lot;
    }

    @GetMapping
    public List<Lot> getLots(Principal principal) {
        if (securityEnabled && principal != null) {
            return lotRepository.findByOwnerUsername(principal.getName());
        }
        return lotRepository.findAll();
    }

    private String resolveUser(String requestedOwner, Principal principal) {
        if (principal != null && !principal.getName().isBlank()) {
            return principal.getName();
        }
        if (requestedOwner != null && !requestedOwner.isBlank()) {
            return requestedOwner.trim();
        }
        return "proc1";
    }
}
