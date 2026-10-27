package com.soa.validation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OWNER: Person C
 * TODO (C): @KafkaListener on Topics.EVENTS, processed_events(eventId UNIQUE), call certificate-service via lb://certificate-service, persist BlendValidation, GET /validations/blends/{id}, DLT, all tests.
 */
@SpringBootApplication(scanBasePackages = "com.soa")
public class BlendValidationApplication {
    public static void main(String[] args) { SpringApplication.run(BlendValidationApplication.class, args); }
}
