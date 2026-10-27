package com.soa.ancestry.repository;

import com.soa.ancestry.domain.Lot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LotRepository extends JpaRepository<Lot, String> {
    List<Lot> findByOwnerUsername(String ownerUsername);
}
