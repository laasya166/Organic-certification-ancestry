package com.soa.ancestry.repository;

import com.soa.ancestry.domain.Blend;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlendRepository extends JpaRepository<Blend, Long> {
    List<Blend> findByCreatedBy(String createdBy);
}
