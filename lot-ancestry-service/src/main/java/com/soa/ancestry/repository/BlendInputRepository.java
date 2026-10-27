package com.soa.ancestry.repository;

import com.soa.ancestry.domain.BlendInput;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlendInputRepository extends JpaRepository<BlendInput, Long> {
    List<BlendInput> findByBlendId(Long blendId);
}
