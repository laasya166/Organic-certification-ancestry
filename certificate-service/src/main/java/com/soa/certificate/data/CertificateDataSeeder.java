package com.soa.certificate.data;

import com.soa.certificate.domain.Certificate;
import com.soa.certificate.domain.CertificateStatus;
import com.soa.certificate.repository.CertificateRepository;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CertificateDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CertificateDataSeeder.class);
    private final CertificateRepository certificateRepository;

    public CertificateDataSeeder(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @Override
    public void run(String... args) {
        if (certificateRepository.count() == 0) {
            log.info("Seeding initial certificate data for demo date 2026-10-27...");

            Certificate certA = new Certificate(
                    "A",
                    "Ecocert",
                    LocalDate.parse("2026-01-01"),
                    LocalDate.parse("2026-12-31"),
                    CertificateStatus.ACTIVE
            );

            Certificate certB = new Certificate(
                    "B",
                    "Control Union",
                    LocalDate.parse("2026-03-01"),
                    LocalDate.parse("2027-02-28"),
                    CertificateStatus.ACTIVE
            );

            // Lot C has NO certificate

            Certificate certD = new Certificate(
                    "D",
                    "Soil Association",
                    LocalDate.parse("2026-01-01"),
                    LocalDate.parse("2026-06-30"),
                    CertificateStatus.ACTIVE
            );

            certificateRepository.saveAll(List.of(certA, certB, certD));
            log.info("Certificate data seeding complete. Seeded certificates for lots A, B, D.");
        }
    }
}
