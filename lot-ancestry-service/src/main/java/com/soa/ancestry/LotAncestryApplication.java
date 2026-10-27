package com.soa.ancestry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OWNER: Person B
 * TODO (B): Lot, Blend, BlendInput; POST /lots, POST /blends (saves ancestry + publishes BlendCreatedEvent to Topics.EVENTS keyed by blendId), record-level access (owner only).
 */
@SpringBootApplication(scanBasePackages = "com.soa")
public class LotAncestryApplication {
    public static void main(String[] args) { SpringApplication.run(LotAncestryApplication.class, args); }
}
