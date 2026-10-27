package com.soa.certificate.controller;

import com.soa.certificate.domain.Certificate;
import com.soa.certificate.domain.CertificateStatus;
import com.soa.certificate.dto.CreateCertificateRequest;
import com.soa.certificate.repository.CertificateRepository;
import com.soa.common.dto.LotValidityResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/certificates")
public class CertificateController {

    private final CertificateRepository certificateRepository;

    public CertificateController(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('certificationReviewer')")
    @ResponseStatus(HttpStatus.CREATED)
    public Certificate createCertificate(@RequestBody CreateCertificateRequest request) {
        if (request.lotId() == null || request.lotId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lotId is required");
        }
        if (request.issuer() == null || request.issuer().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "issuer is required");
        }
        if (request.validFrom() == null || request.validTo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validFrom and validTo are required");
        }
        if (request.validFrom().isAfter(request.validTo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validFrom must not be after validTo");
        }

        CertificateStatus status = request.status() != null ? request.status() : CertificateStatus.ACTIVE;
        Certificate certificate = new Certificate(
                request.lotId().trim(),
                request.issuer().trim(),
                request.validFrom(),
                request.validTo(),
                status
        );
        return certificateRepository.save(certificate);
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasRole('certificationReviewer')")
    public ResponseEntity<Certificate> revokeCertificate(@PathVariable Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Certificate not found with id: " + id));

        certificate.setStatus(CertificateStatus.REVOKED);
        Certificate saved = certificateRepository.save(certificate);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<Certificate> getCertificates(@RequestParam(required = false) String lotId) {
        if (lotId != null && !lotId.isBlank()) {
            return certificateRepository.findByLotId(lotId.trim());
        }
        return certificateRepository.findAll();
    }

    @GetMapping("/validity")
    public List<LotValidityResponse> getValidity(
            @RequestParam List<String> lotIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate on) {

        List<LotValidityResponse> results = new ArrayList<>();

        for (String rawLotId : lotIds) {
            if (rawLotId == null || rawLotId.isBlank()) {
                continue;
            }
            String lotId = rawLotId.trim();
            List<Certificate> certs = certificateRepository.findByLotId(lotId);

            if (certs.isEmpty()) {
                results.add(new LotValidityResponse(lotId, false, "NO_CERTIFICATE"));
                continue;
            }

            // A lot is valid if at least one certificate is ACTIVE and covers the date (inclusive)
            boolean isValid = certs.stream().anyMatch(c ->
                    c.getStatus() == CertificateStatus.ACTIVE &&
                    !on.isBefore(c.getValidFrom()) &&
                    !on.isAfter(c.getValidTo())
            );

            if (isValid) {
                results.add(new LotValidityResponse(lotId, true, null));
            } else {
                // Determine reason among NO_CERTIFICATE, EXPIRED, NOT_YET_VALID, REVOKED
                String reason = determineInvalidReason(certs, on);
                results.add(new LotValidityResponse(lotId, false, reason));
            }
        }

        return results;
    }

    private String determineInvalidReason(List<Certificate> certs, LocalDate on) {
        // If a certificate covers the date range but was revoked, reason is REVOKED
        boolean hasRevokedCovering = certs.stream().anyMatch(c ->
                c.getStatus() == CertificateStatus.REVOKED &&
                !on.isBefore(c.getValidFrom()) &&
                !on.isAfter(c.getValidTo())
        );
        if (hasRevokedCovering) {
            return "REVOKED";
        }

        // If a certificate has expired (validTo < on)
        boolean hasExpired = certs.stream().anyMatch(c -> on.isAfter(c.getValidTo()));
        if (hasExpired) {
            return "EXPIRED";
        }

        // If a certificate is not yet valid (on < validFrom)
        boolean hasNotYetValid = certs.stream().anyMatch(c -> on.isBefore(c.getValidFrom()));
        if (hasNotYetValid) {
            return "NOT_YET_VALID";
        }

        // Otherwise any revoked certificate
        boolean hasRevoked = certs.stream().anyMatch(c -> c.getStatus() == CertificateStatus.REVOKED);
        if (hasRevoked) {
            return "REVOKED";
        }

        return "REVOKED";
    }
}
