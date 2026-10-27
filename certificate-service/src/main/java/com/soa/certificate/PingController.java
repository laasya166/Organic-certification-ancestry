package com.soa.certificate;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Smoke test: GET /ping. Delete once real endpoints exist. */
@RestController
public class PingController {
    @GetMapping("/ping")
    public Map<String, String> ping() { return Map.of("service", "certificate-service", "status", "up"); }
}
