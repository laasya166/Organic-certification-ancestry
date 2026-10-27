package com.soa.certificate.dto;

import com.soa.certificate.domain.CertificateStatus;
import java.time.LocalDate;

public record CreateCertificateRequest(
        String lotId,
        String issuer,
        LocalDate validFrom,
        LocalDate validTo,
        CertificateStatus status
) {}
