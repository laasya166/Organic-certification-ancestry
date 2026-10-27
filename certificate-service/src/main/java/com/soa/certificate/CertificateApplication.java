package com.soa.certificate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OWNER: Person B
 * TODO (B): Certificate(id, lotId, issuer, validFrom, validTo, status), POST /certificates, POST /certificates/{id}/revoke (certificationReviewer), GET /certificates/validity?lotIds=1,2&on=YYYY-MM-DD, seed data.sql.
 */
@SpringBootApplication(scanBasePackages = "com.soa")
public class CertificateApplication {
    public static void main(String[] args) { SpringApplication.run(CertificateApplication.class, args); }
}
