package com.soa.ancestry.data;

import com.soa.ancestry.domain.Lot;
import com.soa.ancestry.repository.LotRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LotDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LotDataSeeder.class);
    private final LotRepository lotRepository;

    public LotDataSeeder(LotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    @Override
    public void run(String... args) {
        if (lotRepository.count() == 0) {
            log.info("Seeding initial lot ancestry data (lots A, B, C, D)...");

            Lot lotA = new Lot("A", "oats", 500.0, "proc1");
            Lot lotB = new Lot("B", "almonds", 200.0, "proc1");
            Lot lotC = new Lot("C", "raisins", 100.0, "proc1");
            Lot lotD = new Lot("D", "hazelnuts", 150.0, "proc1");

            lotRepository.saveAll(List.of(lotA, lotB, lotC, lotD));
            log.info("Lot data seeding complete. Seeded lots A, B, C, D owned by proc1.");
        }
    }
}
