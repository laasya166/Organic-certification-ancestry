package com.soa.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OWNER: Person A
 * TODO (A): POST /auth/login -> signed HS256 JWT {sub, roles:[...], exp}. Seed users proc1 (processor), rev1 (certificationReviewer). Passwords are synthetic.
 */
@SpringBootApplication(scanBasePackages = "com.soa")
public class AuthApplication {
    public static void main(String[] args) { SpringApplication.run(AuthApplication.class, args); }
}
