package com.hiresphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * HireSphereApplication - Entry point for the HireSphere AI Spring Boot application.
 * Real-Time Talent & Placement Platform with Autonomous Agentic AI.
 */
@SpringBootApplication(scanBasePackages = "com.hiresphere")
public class HireSphereApplication {

    public static void main(String[] args) {
        SpringApplication.run(HireSphereApplication.class, args);
        System.out.println("==========================================================");
        System.out.println("  HireSphere AI Backend is UP and RUNNING!");
        System.out.println("  Server Port:      http://localhost:8085");
        System.out.println("  API Base:         http://localhost:8085/api");
        System.out.println("  Swagger UI:       http://localhost:8085/swagger-ui.html");
        System.out.println("  H2 Console:       http://localhost:8085/h2-console");
        System.out.println("  Real-time Stream: http://localhost:8085/api/realtime/stream");
        System.out.println("==========================================================");
    }
}
