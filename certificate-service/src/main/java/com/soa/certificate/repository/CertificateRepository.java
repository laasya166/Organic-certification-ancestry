package com.soa.certificate.repository;

import com.soa.certificate.domain.Certificate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByLotId(String lotId);
    List<Certificate> findByLotIdIn(List<String> lotIds);
}
