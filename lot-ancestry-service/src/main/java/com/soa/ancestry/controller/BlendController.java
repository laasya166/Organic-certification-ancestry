package com.soa.ancestry.controller;

import com.soa.ancestry.dto.BlendResponse;
import com.soa.ancestry.dto.CreateBlendRequest;
import com.soa.ancestry.service.BlendService;
import java.security.Principal;
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
@RequestMapping("/blends")
public class BlendController {

    private final BlendService blendService;

    @Value("${app.security.enabled:false}")
    private boolean securityEnabled;

    public BlendController(BlendService blendService) {
        this.blendService = blendService;
    }

    @PostMapping
    @PreAuthorize("hasRole('processor')")
    @ResponseStatus(HttpStatus.CREATED)
    public BlendResponse createBlend(@RequestBody CreateBlendRequest request, Principal principal) {
        String currentUser = principal != null ? principal.getName() : "proc1";
        return blendService.createBlend(request, currentUser);
    }

    @GetMapping("/{id}")
    public BlendResponse getBlend(@PathVariable Long id, Principal principal) {
        BlendResponse response = blendService.getBlend(id);

        // Record-level access check: processor can only read its own blends
        if (securityEnabled && principal != null) {
            String currentUser = principal.getName();
            if (!response.createdBy().equals(currentUser)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden: you did not create blend " + id);
            }
        }

        return response;
    }
}
