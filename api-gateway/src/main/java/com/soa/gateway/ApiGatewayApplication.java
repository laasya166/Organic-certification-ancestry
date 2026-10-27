package com.soa.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OWNER: Person A.
 * TODO (A): validate JWT signature + expiry here (spring-boot-starter-oauth2-resource-server, reactive),
 *           TLS keystore for the demo, explicit CORS origins, per-route rate/headers if time allows.
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) { SpringApplication.run(ApiGatewayApplication.class, args); }
}
